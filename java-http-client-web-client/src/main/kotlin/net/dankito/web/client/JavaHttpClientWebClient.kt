package net.dankito.web.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import net.codinux.log.logger
import net.dankito.datetime.Instant
import net.dankito.web.client.auth.Authentication
import net.dankito.web.client.auth.BasicAuthAuthentication
import net.dankito.web.client.auth.BearerAuthentication
import net.dankito.web.client.websocket.JavaHttpClientWebSocket
import net.dankito.web.client.websocket.WebSocket
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*
import java.util.zip.GZIPOutputStream
import kotlin.text.replace

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

    protected val requestBuilder = HttpRequest
        .newBuilder()
        .apply {
            config.defaultUserAgent?.let { header("User-Agent", it) }

            header("Content-Type", config.defaultContentType)
            header("Accept", config.defaultAccept)

            config.requestTimeoutMillis?.let {
                timeout(Duration.ofMillis(it))
            }

            if (config.enableBodyCompression) {
                // TODO
            }

            applyAuthentication(this, config.authentication)
        }


    fun webSocket(url: String, authentication: Authentication? = null): WebSocket =
        JavaHttpClientWebSocket(url, authentication, client)


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
        uri(URI(buildUrl(config.baseUrl, parameters)))

        parameters.requestTimeoutMillis?.let { timeout(Duration.ofMillis(it)) }

        setHeaders(this, parameters)

        method(method, getRequestBody(parameters))

        applyAuthentication(this, parameters.authentication)
    }.build()

    protected open fun <T : Any> buildUrl(baseUrl: String?, parameters: RequestParameters<T>): String {
        val relOrAbsUrl = parameters.url

        val withoutQueryParameters = if (baseUrl != null && relOrAbsUrl.startsWith("http://", true) == false && relOrAbsUrl.startsWith("https://", true) == false) {
            // URI(baseUrl).resolve(url) is too stupid to add '/' if necessary or merge two '/', so do it manually
            "${baseUrl.removeSuffix("/")}/${relOrAbsUrl.removePrefix("/")}"
        } else {
            relOrAbsUrl
        }

        return if (parameters.queryParameters.isEmpty()) {
            withoutQueryParameters
        } else {
            val query = parameters.queryParameters.entries.joinToString("&", "?") { (name, value) ->
                "${URLEncoder.encode(name, Charsets.UTF_8)}=${URLEncoder.encode(value.toString(), Charsets.UTF_8)}"
            }
            withoutQueryParameters + query
        }
            .replace(" ", "%20") // is not a real encoding, but at least encodes white spaces
    }

    protected open fun <T : Any> setHeaders(requestBuilder: HttpRequest.Builder, parameters: RequestParameters<T>) {
        val headers = mutableMapOf<String, String>()

        parameters.headers.forEach { (name, value) -> headers.put(name, value) }
        parameters.cookies.forEach { cookie -> headers.put("Cookie", "${cookie.name}=${cookie.value}") } // TODO: build Cookie header

        parameters.userAgent?.let { requestBuilder.header("User-Agent", it) }

        headers.put("Accept", parameters.accept ?: config.defaultAccept)
        if (parameters.body != null) {
            headers.put("Content-Type", parameters.contentType ?: config.defaultContentType)
        }

        headers.forEach { (name, value) -> requestBuilder.setHeader(name, value) }
    }

    protected open fun applyAuthentication(request: HttpRequest.Builder, authentication: Authentication?) {
        (authentication as? BasicAuthAuthentication)?.let { basicAuth ->
            val authHeader = "Basic " + Base64.getEncoder()
                .encodeToString("${basicAuth.username}:${basicAuth.password}".toByteArray())
            request.header("Authorization", authHeader)
        }

        (authentication as? BearerAuthentication)?.let { bearerAuth ->
            request.header("Authorization", "Bearer ${bearerAuth.bearerToken}")
        }
    }


    protected open fun <T : Any> getRequestBody(parameters: RequestParameters<T>): HttpRequest.BodyPublisher {
        val body = parameters.body

        return if (body == null) {
            HttpRequest.BodyPublishers.noBody()
        } else {
            val bodyAsString = if (body is String) body else (parameters.serializer ?: config.serializer).serialize(body)

            if (parameters.compressBodyIfSupported) {
                // TODO
            }

            HttpRequest.BodyPublishers.ofString(bodyAsString)
        }
    }

    protected open fun gzip(body: String): ByteArray {
        val outputStream = ByteArrayOutputStream()
        GZIPOutputStream(outputStream).use { it.write(body.toByteArray()) }
        return outputStream.toByteArray()
    }


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