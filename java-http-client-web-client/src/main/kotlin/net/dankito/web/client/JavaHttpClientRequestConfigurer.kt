package net.dankito.web.client

import net.dankito.web.client.auth.Authentication
import net.dankito.web.client.auth.BasicAuthAuthentication
import net.dankito.web.client.auth.BearerAuthentication
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpRequest
import java.time.Duration
import java.util.*
import java.util.zip.GZIPOutputStream

open class JavaHttpClientRequestConfigurer {

    companion object {
        val Default = JavaHttpClientRequestConfigurer()
    }


    open fun <T : Any> configureRequest(requestBuilder: HttpRequest.Builder, method: String, parameters: RequestParameters<T>, config: ClientConfig): HttpRequest.Builder = requestBuilder.apply {
        uri(URI(buildUrl(config.baseUrl, parameters)))

        parameters.requestTimeoutMillis?.let { timeout(Duration.ofMillis(it)) }

        setHeaders(this, parameters, config)

        method(method, getRequestBody(parameters, config))

        applyAuthentication(this, parameters.authentication)
    }

    open fun <T : Any> buildUrl(baseUrl: String?, parameters: RequestParameters<T>): String {
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

    protected open fun <T : Any> setHeaders(requestBuilder: HttpRequest.Builder, parameters: RequestParameters<T>, config: ClientConfig) {
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

    open fun applyAuthentication(request: HttpRequest.Builder, authentication: Authentication?) {
        (authentication as? BasicAuthAuthentication)?.let { basicAuth ->
            val authHeader = "Basic " + Base64.getEncoder()
                .encodeToString("${basicAuth.username}:${basicAuth.password}".toByteArray())
            request.header("Authorization", authHeader)
        }

        (authentication as? BearerAuthentication)?.let { bearerAuth ->
            request.header("Authorization", "Bearer ${bearerAuth.bearerToken}")
        }
    }


    protected open fun <T : Any> getRequestBody(parameters: RequestParameters<T>, config: ClientConfig): HttpRequest.BodyPublisher {
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

}