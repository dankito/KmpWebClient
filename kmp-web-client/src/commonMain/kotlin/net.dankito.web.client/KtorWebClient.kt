package net.dankito.web.client

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.*
import io.ktor.util.date.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import net.dankito.web.client.auth.*

open class KtorWebClient(
    baseUrl: String? = null,
    authentication: Authentication? = null,
    /**
     * Be aware the following engines do not support disabling certificate check:
     * - JavaScript HttpClient
     * - CIO on native platforms
     * - WinHttp
     */
    ignoreCertificateErrors: Boolean = false,
    defaultUserAgent: String? = RequestParameters.DefaultMobileUserAgent,
    protected val defaultContentType: String = ContentTypes.JSON,
) : WebClient {

    protected open val json = Json {
        ignoreUnknownKeys = true
    }

    protected open val client = Platform.createPlatformSpecificHttpClient(ignoreCertificateErrors) { configureClient(this, baseUrl, defaultUserAgent) }
        ?: HttpClient { configureClient(this, baseUrl, defaultUserAgent) }

    protected open val client = Platform.createPlatformSpecificHttpClient(ignoreCertificateErrors) { configureClient(this, baseUrl, authentication, defaultUserAgent) }
        ?: HttpClient { configureClient(this, baseUrl, authentication, defaultUserAgent) }

    private fun configureClient(config: HttpClientConfig<*>, baseUrl: String?, authentication: Authentication?, defaultUserAgent: String?) {
        config.apply {
            install(HttpTimeout)
            install(ContentNegotiation) {
                json()
            }

            if (authentication != null) {
                install(Auth) {
                    (authentication as? BasicAuthAuthentication)?.let { basicAuth ->
                        basic {
                            sendWithoutRequest { true }
                            credentials {
                                BasicAuthCredentials(basicAuth.username, basicAuth.password)
                            }
                        }
                    }
                }
            }

            defaultRequest {
                baseUrl?.let {
                    if (baseUrl.endsWith("/")) { // add trailing slash, otherwise last path segment gets cut off when appending to relative url
                        url(baseUrl)
                    } else {
                        url(baseUrl + "/")
                    }
                }

                defaultUserAgent?.let {
                    userAgent(it)
                }
            }
        }
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


    protected open suspend fun <T : Any> makeRequest(method: HttpMethod, parameters: RequestParameters<T>): WebClientResponse<T> {
        return try {
            val httpResponse = client.request {
                configureRequest(this, method, parameters)
            }

            mapHttResponse(method, parameters, httpResponse)
        } catch (e: Throwable) {
            WebClientResponse(false, error = e)
        }
    }

    protected open suspend fun <T : Any> configureRequest(builder: HttpRequestBuilder, method: HttpMethod, parameters: RequestParameters<T>) {
        builder.apply {
            this.method = method

            url {
                val url = parameters.url
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

            parameters.accept?.let {
                this.accept(ContentType.parse(it))
            }

            timeout {
                // JS doesn't support connectTimeout and socketTimeout
                connectTimeoutMillis = parameters.connectTimeoutMillis
                socketTimeoutMillis = parameters.socketTimeoutMillis
                requestTimeoutMillis = parameters.requestTimeoutMillis
            }

            parameters.body?.let {
                contentType((parameters.contentType ?: defaultContentType).let { ContentType.parse(it) })

                setBody(it)
            }
        }
    }

    protected open suspend fun <T : Any> mapHttResponse(method: HttpMethod, parameters: RequestParameters<T>, httpResponse: HttpResponse): WebClientResponse<T> {
        val statusCode = httpResponse.status.value
        val headers = httpResponse.headers.toMap()
        val cookies = httpResponse.setCookie().map { mapCookie(it) }

        return if (httpResponse.status.isSuccess()) {
            WebClientResponse(true, statusCode, headers, cookies, body = decodeResponse(parameters, httpResponse))
        } else {
            val responseBody = httpResponse.bodyAsText()

            WebClientResponse(false, statusCode, headers, cookies, WebClientException(statusCode, responseBody))
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

    protected open fun mapCookie(cookie: io.ktor.http.Cookie) = Cookie(
        cookie.name,
        cookie.value,
        cookie.domain,
        cookie.path,
        cookie.expires?.timestamp,
        cookie.secure,
        cookie.httpOnly
    )
}