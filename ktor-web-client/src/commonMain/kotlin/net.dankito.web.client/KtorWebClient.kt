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
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.*
import kotlinx.serialization.InternalSerializationApi
import net.codinux.log.logger
import net.dankito.web.client.auth.*
import net.dankito.web.client.serialization.KotlinxJsonSerializer
import net.dankito.web.client.serialization.Serializer
import net.dankito.web.client.sse.KtorSseClient
import net.dankito.web.client.sse.SseClient
import net.dankito.web.client.websocket.KtorWebSocket
import net.dankito.web.client.websocket.WebSocket
import net.dankito.web.client.websocket.WebSocketConfig

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
        serializer: Serializer = KotlinxJsonSerializer.Instance,
        enableBodyCompression: Boolean = false,
        enableWebSocket: Boolean = false,
        enableSSE: Boolean = false,
        customClientCreator: ((ClientConfig, HttpClientConfig<*>.() -> Unit) -> HttpClient)? = null,
    ) : this(ClientConfig(baseUrl, authentication, ignoreCertificateErrors, customClientConfig, defaultUserAgent,
        defaultContentType, defaultAccept, serializer, enableBodyCompression, enableWebSocket = enableWebSocket, enableSSE = enableSSE), customClientCreator)


    companion object {
        /**
         * If you call the HttpClient constructor without an argument, the client will choose an engine
         * automatically depending on the artifacts added in a build script.
         */
        fun createDefaultHttpClient(config: HttpClientConfig<*>.() -> Unit) =
            HttpClient(config)
    }


    open val sse: SseClient by lazy { KtorSseClient(client) }

    override fun webSocket(config: WebSocketConfig): WebSocket =
        KtorWebSocket(config, client, this.config.serializer)


    protected open val requestConfigurer: KtorRequestConfigurer = KtorRequestConfigurer.Default

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

            install(Auth) { // install Auth plugin if config.authentication is set or not as authentication can also be set on a per request
                config.authentication?.let { authentication ->
                    (authentication as? BasicAuthAuthentication)?.let { basicAuth ->
                        basic {
                            sendWithoutRequest { true }
                            credentials {
                                BasicAuthCredentials(basicAuth.username, basicAuth.password)
                            }
                        }
                    }


                    (authentication as? BearerAuthentication)?.let { bearerAuth ->
                        bearer {
                            sendWithoutRequest { true }
                            loadTokens { BearerTokens(bearerAuth.bearerToken, null) }
                        }
                    }
                }
            }

            if (config.enableWebSocket) {
                install(WebSockets)
            }

            /**
             * SSEPlugin in Ktor (if installed but unused):
             * - Adds routing capability for event streams
             * - Doesn’t do anything unless a request matches an SSE route
             * - Minimal cost unless actively used
             */
            if (config.enableSSE) {
                install(SSE)
            }

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

                if (config.logOutgoingRequests) {
                    log.info { "Sending request to ${method.value} ${this.url} ..." }
                }
            }

            mapHttResponse(method, parameters, httpResponse)
        } catch (e: Throwable) {
            log.error(e) { "Error during request to ${method.value} ${parameters.url}" }
            val (url, errorType) = getErrorTypeAndRequestedUrl(e)
            // be aware this might not be the absolute url but only the relative url the user has passed to WebClient
            WebClientResult(url ?: parameters.url, false, null, errorType, WebClientException(e.message, e))
        }
    }

    protected open suspend fun <T : Any> configureRequest(builder: HttpRequestBuilder, method: HttpMethod, parameters: RequestParameters<T>) =
        requestConfigurer.configureRequest(builder, method, parameters, config)

    protected open suspend fun <T : Any> mapHttResponse(method: HttpMethod, parameters: RequestParameters<T>, response: HttpResponse): WebClientResult<T> {
        val url = getUrl(response)

        val details = KtorResponseDetails(method.value, parameters, response)

        return if (response.status.isSuccess()) {
            try {
                if (config.logSuccessfulResponses) {
                    log.info { "Successful response retrieved from ${method.value} $url: ${details.statusCode} ${details.reasonPhrase}" }
                }

                WebClientResult(url, true, details, body = decodeResponse(parameters, response))
            } catch (e: Throwable) {
                log.error(e) { "Error while mapping response of: ${method.value} $url, ${response.headers.toMap()}" }
                WebClientResult(url, false, details, ClientErrorType.DeserializationError, WebClientException(e.message, e, details, response.bodyAsText()))
            }
        } else {
            val responseBody = response.bodyAsText()
            val errorType = if (details.isServerErrorResponse) ClientErrorType.ServerError else ClientErrorType.ClientError

            if (config.logErroneousResponses) {
                log.info { "Erroneous response retrieved from ${method.value} $url: ${details.statusCode} ${details.reasonPhrase}. Body:\n${responseBody.take(250)}" +
                        if (responseBody.length > 250) "..." else "" }
            }

            WebClientResult(url, false, details, errorType, WebClientException("The HTTP response indicated an error: " +
                    "${response.status.value} ${response.status.description}", null, details, responseBody))
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
            (parameters.serializer ?: config.serializer).deserialize(clientResponse.bodyAsText(),
                responseClass, parameters.responseGenericType1, parameters.responseGenericType2)
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