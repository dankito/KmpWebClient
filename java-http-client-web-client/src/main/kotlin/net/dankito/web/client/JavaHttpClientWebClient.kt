package net.dankito.web.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import net.dankito.datetime.Instant
import net.dankito.web.client.auth.BasicAuthAuthentication
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*
import java.util.zip.GZIPOutputStream

open class JavaHttpClientWebClient(
    protected val config: ClientConfig = ClientConfig(),
) : WebClient {

    protected val client = HttpClient.newBuilder().apply {
        if (config.ignoreCertificateErrors) {
            sslContext(SslSettings.trustAllCertificatesSslContext)
        }

        config.connectTimeoutMillis?.let {
            connectTimeout(Duration.ofMillis(it))
        }
    }.build()

    protected val requestBuilder = HttpRequest
        .newBuilder()
        .apply {
            config.baseUrl?.let { baseUrl ->
                uri(URI(baseUrl))
            }

            config.defaultUserAgent?.let { header("User-Agent", it) }

            header("Content-Type", config.defaultContentType)
            header("Accept", config.defaultAccept)

            config.requestTimeoutMillis?.let {
                timeout(Duration.ofMillis(it))
            }

            if (config.enableBodyCompression) {
                // TODO
            }

            config.authentication?.let { authentication ->
                (config.authentication as? BasicAuthAuthentication)?.let { basicAuth ->
                    val authHeader = "Basic " + Base64.getEncoder().encodeToString("${basicAuth.username}:${basicAuth.password}".toByteArray())
                    header("Authorization", authHeader)
                }
            }
        }

    // for now use kotlinx-serialization so that JavaHttpClientWebClient can be used as a plug-in replacement for KtorWebClient
    protected open val json = Json {
        ignoreUnknownKeys = true
    }


    override suspend fun head(parameters: RequestParameters<Unit>): WebClientResult<Unit> = makeRequest("HEAD", parameters)

    override suspend fun <T : Any> get(parameters: RequestParameters<T>): WebClientResult<T> = makeRequest("GET", parameters)

    override suspend fun <T : Any> post(parameters: RequestParameters<T>): WebClientResult<T> = makeRequest("POST", parameters)

    override suspend fun <T : Any> put(parameters: RequestParameters<T>): WebClientResult<T> = makeRequest("PUT", parameters)

    override suspend fun <T : Any> delete(parameters: RequestParameters<T>): WebClientResult<T> = makeRequest("DELETE", parameters)

    override suspend fun <T : Any> custom(httpMethod: String, parameters: RequestParameters<T>): WebClientResult<T> = makeRequest(httpMethod, parameters)


    protected open suspend fun <T : Any> makeRequest(method: String, parameters: RequestParameters<T>): WebClientResult<T> = withContext(Dispatchers.IO) {
        val builder = requestBuilder.copy()

        builder.uri(URI(parameters.url)) // TODO: append baseUrl and queryParameters if set

        parameters.requestTimeoutMillis?.let { builder.timeout(Duration.ofMillis(it)) }

        setHeaders(builder, parameters)

        val request = builder.method(method, getRequestBody(parameters)).build()

        val requestTime = Instant.now()

//        val bodyHandler = JavaHttpResponseBodyHandler()
        val bodyHandler = HttpResponse.BodyHandlers.ofString()
        val response = client.sendAsync(request, bodyHandler).await()

        mapResponse(method, parameters, response, requestTime)
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

    protected open fun <T : Any> getRequestBody(parameters: RequestParameters<T>): HttpRequest.BodyPublisher {
        val body = parameters.body

        return if (body == null) {
            HttpRequest.BodyPublishers.noBody()
        } else {
            val bodyAsString = if (body is String) body else json.encodeToString(body)

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


    protected open fun <T : Any> mapResponse(method: String, parameters: RequestParameters<T>, response: HttpResponse<String>,
                                             requestTime: Instant): WebClientResult<T> {
        val status = response.statusCode()
        val responseDetails = ResponseDetails(status, "", requestTime, Instant.now(), response.version().toString(),
            response.headers().map(), emptyList(), // TODO: map cookies
            response.headers().firstValue("Content-Type").orElse(null), response.headers().firstValue("Content-Length").orElse(null)?.toLongOrNull() // TODO: extract Charset from Content-Type
        )
        val responseBody = decodeResponseBody(parameters, response)

        // TODO: catch and map errors

        return WebClientResult(response.uri().toString(), status < 300, responseDetails, body = responseBody)
    }

    @Suppress("UNCHECKED_CAST")
    @OptIn(InternalSerializationApi::class)
    protected open fun <T : Any> decodeResponseBody(parameters: RequestParameters<T>, response: HttpResponse<String>): T {
        val bodyString = response.body()
        val responseClass = parameters.responseClass

        return if (responseClass == null || responseClass == Unit::class) {
            Unit as T
        } else if(responseClass == String::class) {
            bodyString as T
        } else if (responseClass == ByteArray::class) {
            val bytes: ByteArray = bodyString.encodeToByteArray()
            bytes as T
        } else {
            json.decodeFromString(responseClass.serializer(), bodyString)
        }
    }

}