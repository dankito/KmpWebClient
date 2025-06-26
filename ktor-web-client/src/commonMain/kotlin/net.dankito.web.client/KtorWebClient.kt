package net.dankito.web.client

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.*
import io.ktor.util.date.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import net.codinux.log.logger
import net.dankito.web.client.auth.*
import net.dankito.web.client.sse.KtorSseClient
import net.dankito.web.client.sse.SseClient

open class KtorWebClient(
    protected val config: ClientConfig = ClientConfig(),
    /**
     * If you want to create the HttpClient instance by yourself, e.g. you want to use Curl on Windows,
     * CIO on Windows or Linux, then provide a custom HttpClient creator function here.
     */
    customClientCreator: ((ClientConfig, HttpClientConfig<*>.() -> Unit) -> HttpClient)? = null,
) : WebClient {

    constructor(
        baseUrl: String? = null,
        authentication: Authentication? = null,
        /**
         * Be aware the following engines do not support disabling certificate check:
         * - JavaScript HttpClient
         * - CIO on native platforms
         * - WinHttp
         */
        ignoreCertificateErrors: Boolean = false,
        customClientConfig: ((HttpClientConfig<*>, config: ClientConfig) -> Unit)? = null,
        defaultUserAgent: String? = RequestParameters.DefaultMobileUserAgent,
        defaultContentType: String = ContentTypes.JSON,
        defaultAccept: String = ContentTypes.JSON,
        enableBodyCompression: Boolean = false,
        customClientCreator: ((ClientConfig, HttpClientConfig<*>.() -> Unit) -> HttpClient)? = null,
    ) : this(ClientConfig(baseUrl, authentication, ignoreCertificateErrors, customClientConfig, defaultUserAgent, defaultContentType, defaultAccept, enableBodyCompression), customClientCreator)


    companion object {
        /**
         * If you call the HttpClient constructor without an argument, the client will choose an engine
         * automatically depending on the artifacts added in a build script.
         */
        fun createDefaultHttpClient(config: HttpClientConfig<*>.() -> Unit) =
            HttpClient(config)
    }


    open val sse: SseClient by lazy { KtorSseClient(client) }


    protected open val json = Json {
        ignoreUnknownKeys = true
    }

    protected val log by logger()


    protected open val client = customClientCreator?.invoke(config) { configureClient(this, config) }
        ?: Platform.createPlatformSpecificHttpClient(config.ignoreCertificateErrors) { configureClient(this, config) }
        ?: HttpClient { configureClient(this, config) }


    private fun configureClient(clientConfig: HttpClientConfig<*>, config: ClientConfig) {
        clientConfig.apply {
            install(ContentNegotiation) {
                json()
            }

            if (config.enableBodyCompression) {
                install(ContentEncoding) {
                    gzip()
                    deflate()
                }
            }

            install(HttpTimeout) {
                config.connectTimeoutMillis?.let { connectTimeoutMillis = it }
                config.socketTimeoutMillis?.let { socketTimeoutMillis = it }
                config.requestTimeoutMillis?.let { requestTimeoutMillis = it }
            }

            if (config.authentication != null) {
                install(Auth) {
                    (config.authentication as? BasicAuthAuthentication)?.let { basicAuth ->
                        basic {
                            sendWithoutRequest { true }
                            credentials {
                                BasicAuthCredentials(basicAuth.username, basicAuth.password)
                            }
                        }
                    }
                }
            }

            /**
             * SSEPlugin in Ktor (if installed but unused):
             * - Adds routing capability for event streams
             * - Doesn’t do anything unless a request matches an SSE route
             * - Minimal cost unless actively used
             */
            install(SSE)

            defaultRequest {
                config.baseUrl?.let { baseUrl ->
                    if (baseUrl.endsWith("/")) { // add trailing slash, otherwise last path segment gets cut off when appending to relative url
                        url(baseUrl)
                    } else {
                        url(baseUrl + "/")
                    }
                }

                config.defaultUserAgent?.let {
                    userAgent(it)
                }
            }
        }

        // call at end so that it can overwrite settings we made
        config.customClientConfig?.invoke(clientConfig, config)
    }


    override suspend fun <T : Any> get(parameters: RequestParameters<T>) =
        makeRequest(HttpMethod.Get, parameters)

    override suspend fun head(parameters: RequestParameters<Unit>) =
        makeRequest(HttpMethod.Head, parameters)

    override suspend fun <T : Any> post(parameters: RequestParameters<T>) =
        makeRequest(HttpMethod.Post, parameters)

    override suspend fun <T : Any> put(parameters: RequestParameters<T>) =
        makeRequest(HttpMethod.Put, parameters)

    override suspend fun <T : Any> delete(parameters: RequestParameters<T>) =
        makeRequest(HttpMethod.Delete, parameters)

    override suspend fun <T : Any> custom(httpMethod: String, parameters: RequestParameters<T>) =
        custom(HttpMethod(httpMethod), parameters)

    open suspend fun <T : Any> custom(method: HttpMethod, parameters: RequestParameters<T>) =
        makeRequest(method, parameters)


    protected open suspend fun <T : Any> makeRequest(method: HttpMethod, parameters: RequestParameters<T>): WebClientResult<T> {
        return try {
            val httpResponse = client.request {
                configureRequest(this, method, parameters)
            }

            mapHttResponse(method, parameters, httpResponse)
        } catch (e: Throwable) {
            log.error(e) { "Error during request to ${method.value} ${parameters.url}" }
            val (url, errorType) = getErrorTypeAndRequestedUrl(e)
            // be aware this might not be the absolute url but only the relative url the user has passed to WebClient
            WebClientResult(url ?: parameters.url, false, null, errorType, WebClientException(e.message, e))
        }
    }

    protected open suspend fun <T : Any> configureRequest(builder: HttpRequestBuilder, method: HttpMethod, parameters: RequestParameters<T>) {
        builder.apply {
            this.method = method

            url {
                val url = parameters.url.replace(" ", "%20") // is not a real encoding, but at least encodes white spaces
                if (url.startsWith("http", true)) { // absolute url
                    takeFrom(url)
                } else { // relative url
                    appendPathSegments(url)
                }

                parameters.queryParameters.forEach { (name, value) -> this.parameters.append(name, value.toString()) }
            }

            parameters.headers.forEach { (name, value) ->
                this.headers.append(name, value)
            }

            parameters.cookies.forEach { cookie ->
                this.cookie(cookie.name, cookie.value, 0, cookie.expiresAt?.let { GMTDate(it) }, cookie.domain, cookie.path, cookie.secure, cookie.httpOnly)
            }

            parameters.userAgent?.let {
                this.userAgent(it)
            }

            this.accept(ContentType.parse(parameters.accept ?: config.defaultAccept))

            timeout {
                // JS doesn't support connectTimeout and socketTimeout
                parameters.connectTimeoutMillis?.let { connectTimeoutMillis = it }
                parameters.socketTimeoutMillis?.let { socketTimeoutMillis = it }
                parameters.requestTimeoutMillis?.let { requestTimeoutMillis = it }
            }

            parameters.body?.let {
                contentType((parameters.contentType ?: config.defaultContentType).let { ContentType.parse(it) })

                setBody(it)
            }
        }
    }

    protected open suspend fun <T : Any> mapHttResponse(method: HttpMethod, parameters: RequestParameters<T>, response: HttpResponse): WebClientResult<T> {
        val url = getUrl(response)

        val responseDetails = KtorResponseDetails(response)

        return if (response.status.isSuccess()) {
            try {
                WebClientResult(url, true, responseDetails, body = decodeResponse(parameters, response))
            } catch (e: Throwable) {
                log.error(e) { "Error while mapping response of: ${method.value} $url, ${response.headers.toMap()}" }
                WebClientResult(url, false, responseDetails, ClientErrorType.DeserializationError, WebClientException(e.message, e, responseDetails))
            }
        } else {
            val responseBody = response.bodyAsText()
            val errorType = if (responseDetails.isServerErrorResponse) ClientErrorType.ServerError else ClientErrorType.ClientError

            WebClientResult(url, false, responseDetails, errorType, WebClientException("The HTTP response indicated an error: " +
                    "${response.status.value} ${response.status.description}", null, responseDetails, responseBody))
        }
    }

    @Suppress("UNCHECKED_CAST")
    @OptIn(InternalSerializationApi::class)
    protected open suspend fun <T : Any> decodeResponse(parameters: RequestParameters<T>, clientResponse: HttpResponse): T {
        val responseClass = parameters.responseClass

        return if (responseClass == null || responseClass == Unit::class) {
            Unit as T
        } else if(responseClass == String::class) {
            clientResponse.bodyAsText() as T
        } else if (responseClass == ByteArray::class) {
            val bytes: ByteArray = clientResponse.body()
            bytes as T
        } else {
            // TODO: add cache for Serializers
            // TODO: stream response (at least on JVM)
            json.decodeFromString(responseClass.serializer(), clientResponse.body())
        }
    }

    protected open fun getErrorTypeAndRequestedUrl(e: Throwable): Pair<String?, ClientErrorType> = when (e) {
        is ConnectTimeoutException, is SocketTimeoutException, is HttpRequestTimeoutException
            -> tryToExtractRequestedUrl(e) to ClientErrorType.Timeout
        is ClientRequestException -> getUrl(e.response) to ClientErrorType.ClientError
        is ServerResponseException -> getUrl(e.response) to ClientErrorType.ServerError
        is ResponseException -> getUrl(e.response) to ClientErrorType.Unknown
        is URLParserException -> null to ClientErrorType.ClientError
        else -> {
            val message = e.message ?: ""
            if (isNetworkError(message)) {
                null to ClientErrorType.NetworkError
            } else if (e::class.simpleName == "InterruptedIOException") { // on JVM io.ktor.client.network.sockets.InterruptedIOException is an internal class (looks like a bug to me)
                tryToExtractRequestedUrl(e) to ClientErrorType.Timeout
            } else {
                null to ClientErrorType.Unknown
            }
        }
    }

    protected open fun isNetworkError(message: String) =
        message.contains("Connection failed", true)
                || message.contains("Connection refused", true)
                || message.contains("Fail to fetch", true) // JS
                || message.contains("Could not connect", true) // Apple systems

    protected open fun tryToExtractRequestedUrl(e: Throwable): String? {
        val startIndex = e.message?.indexOf("[url=")?.plus("[url=".length) ?: -1
        if (startIndex > 5) {
            val endIndex = e.message?.indexOf(", ", startIndex + 1) ?: -1
            if (endIndex > startIndex) {
                return e.message?.substring(startIndex, endIndex)
            }
        }

        return null
    }

    protected open fun getUrl(response: HttpResponse): String = response.request.url.toString()
}