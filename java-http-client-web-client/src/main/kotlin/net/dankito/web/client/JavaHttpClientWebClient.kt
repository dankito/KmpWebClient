package net.dankito.web.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import net.codinux.log.logger
import net.dankito.datetime.Instant
import net.dankito.web.client.auth.Authentication
import net.dankito.web.client.websocket.JavaHttpClientWebSocket
import net.dankito.web.client.websocket.WebSocket
import net.dankito.web.client.websocket.WebSocketConfig
import java.io.InputStream
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Suppress("UNCHECKED_CAST")
open class JavaHttpClientWebClient(
    protected val config: ClientConfig = ClientConfig(),
) : WebClient {

    protected val log by logger()

    protected val client = HttpClient.newBuilder().apply {
        followRedirects(HttpClient.Redirect.NORMAL)

        if (config.ignoreCertificateErrors) {
            sslContext(SslSettings.trustAllCertificatesSslContext)
        }

        config.connectTimeoutMillis?.let {
            connectTimeout(Duration.ofMillis(it))
        }

        config.dispatcher?.let { dispatcher ->
            executor(dispatcher.asExecutor())
        }
    }.build()

    protected open val requestConfigurer: JavaHttpClientRequestConfigurer = JavaHttpClientRequestConfigurer.Default

    protected val requestBuilder = HttpRequest
        .newBuilder()
        .apply {
            config.defaultUserAgent?.let { header("User-Agent", it) }

            header("Content-Type", config.defaultContentType)
            header("Accept", config.defaultAccept)

            requestConfigurer.createAuthorizationHeaderValue(config.authentication)?.let {
                header("Authorization", it)
            }

            config.requestTimeoutMillis?.let {
                timeout(Duration.ofMillis(it))
            }

            if (config.enableBodyCompression) {
                // TODO
            }
        }


    override fun webSocket(config: WebSocketConfig): WebSocket =
        JavaHttpClientWebSocket(config, client)


    override suspend fun head(parameters: RequestParameters<Unit>): WebClientResult<Unit> = makeRequest("HEAD", parameters)

    override suspend fun <T : Any> get(parameters: RequestParameters<T>): WebClientResult<T> = makeRequest("GET", parameters)

    override suspend fun <T : Any> post(parameters: RequestParameters<T>): WebClientResult<T> = makeRequest("POST", parameters)

    override suspend fun <T : Any> put(parameters: RequestParameters<T>): WebClientResult<T> = makeRequest("PUT", parameters)

    override suspend fun <T : Any> delete(parameters: RequestParameters<T>): WebClientResult<T> = makeRequest("DELETE", parameters)

    override suspend fun <T : Any> custom(httpMethod: String, parameters: RequestParameters<T>): WebClientResult<T> = makeRequest(httpMethod, parameters)


    protected open suspend fun <T : Any> makeRequest(method: String, parameters: RequestParameters<T>): WebClientResult<T> = withContext(config.dispatcher ?: Dispatchers.IO) {
        try {
            val request = configureRequest(method, parameters)

            val requestTime = Instant.now()

            if (config.logOutgoingRequests) {
                log.info { "Sending request to $method ${request.uri()} ..." }
            }

//            val bodyHandler = JavaHttpResponseBodyHandler()

            val bodyHandler: HttpResponse.BodyHandler<out Any> = when (parameters.responseClass) {
                ByteArray::class -> HttpResponse.BodyHandlers.ofByteArray()
                InputStream::class -> HttpResponse.BodyHandlers.ofInputStream()
                else -> HttpResponse.BodyHandlers.ofString()
            }  as HttpResponse.BodyHandler<T>

            val response = client.sendAsync(request, bodyHandler).await() as HttpResponse<T>

            mapResponse(method, parameters, response, requestTime)
        } catch (e: Throwable) {
            log.error(e) { "Error during request to $method ${parameters.url}" }
            val (url, errorType) = null to null // TODO: implement getErrorTypeAndRequestedUrl(e) like in KtorWebClient
            // be aware this might not be the absolute url but only the relative url the user has passed to WebClient
            WebClientResult(url ?: parameters.url, false, null, errorType, WebClientException(e.message, e))
        }
    }

    protected open fun <T : Any> configureRequest(method: String, parameters: RequestParameters<T>): HttpRequest = requestBuilder.copy().apply {
        requestConfigurer.configureRequest(this, method, parameters, config)
    }.build()


    protected open fun <T : Any> mapResponse(method: String, parameters: RequestParameters<T>, response: HttpResponse<T>,
                                             requestTime: Instant): WebClientResult<T> {
        val url = response.uri().toString()
        val statusCode = response.statusCode()
        val reasonPhrase = HttpStatus.getReasonPhrase(statusCode) ?: ""
        val responseDetails = ResponseDetails(method, parameters, statusCode, reasonPhrase, requestTime, Instant.now(), response.version().toString(),
            response.headers().map(), emptyList(), // TODO: map cookies
            response.headers().firstValue("Content-Type").orElse(null), response.headers().firstValue("Content-Length").orElse(null)?.toLongOrNull() // TODO: extract Charset from Content-Type
        )

        return if (statusCode in 200..299) {
            try {
                if (config.logSuccessfulResponses) {
                    log.info { "Successful response retrieved from $method $url: $statusCode $reasonPhrase" }
                }

                val responseBody = decodeResponseBody(parameters, response)

                WebClientResult(url, true, responseDetails, body = responseBody)
            } catch (e: Throwable) {
                log.error(e) { "Error while mapping response of: $method $url, ${responseDetails.headersFirstValue}" }
                WebClientResult(url, false, responseDetails, ClientErrorType.DeserializationError,
                    WebClientException(e.message, e, responseDetails, response.body() as? String))
            }
        } else {
            val responseBody = response.body() as? String
            val errorType = if (responseDetails.isServerErrorResponse) ClientErrorType.ServerError else ClientErrorType.ClientError

            if (config.logErroneousResponses) {
                log.info { "Erroneous response retrieved from $method $url: $statusCode $reasonPhrase. Body:\n${responseBody?.take(250)}" +
                            if (responseBody != null && responseBody.length > 250) "..." else "" }
            }

            WebClientResult(url, false, responseDetails, errorType, WebClientException("The HTTP response indicated an error: " +
                    "$statusCode $reasonPhrase", null, responseDetails, responseBody))
        }
    }

    protected open fun <T : Any> decodeResponseBody(parameters: RequestParameters<T>, response: HttpResponse<T>): T {
        val responseClass = parameters.responseClass

        return if (responseClass == null || responseClass == Unit::class) {
            Unit as T
        } else if(responseClass == String::class) {
            response.body() as T
        } else if (responseClass == ByteArray::class) {
            response.body() as T
        }  else if (responseClass == InputStream::class) {
            response.body() as T
        } else {
            val bodyString = response.body() as String
            (parameters.serializer ?: config.serializer).deserialize(bodyString, responseClass,
                parameters.responseGenericType1, parameters.responseGenericType2)
        }
    }

}