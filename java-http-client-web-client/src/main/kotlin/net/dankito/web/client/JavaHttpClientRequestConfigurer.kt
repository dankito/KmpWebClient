package net.dankito.web.client

import net.dankito.web.client.auth.Authentication
import net.dankito.web.client.auth.BasicAuthAuthentication
import net.dankito.web.client.auth.BearerAuthentication
import net.dankito.web.client.websocket.WebSocketConfig
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpRequest
import java.net.http.WebSocket
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

        val headers = createHeaderMap(parameters.headers, parameters.authentication, parameters.userAgent, parameters.accept ?: config.defaultAccept, parameters.cookies)
        if (parameters.body != null) {
            headers.put("Content-Type", parameters.contentType ?: config.defaultContentType)
        }
        headers.forEach { (name, value) -> requestBuilder.setHeader(name, value) }

        method(method, getRequestBody(parameters, config))
    }


    open fun configureRequest(builder: WebSocket.Builder, config: WebSocketConfig) {
        val headers = createHeaderMap(config.headers, config.authentication, config.userAgent, null, config.cookies)

        headers.forEach { (name, value) -> builder.header(name, value) }
    }


    open fun <T : Any> buildUrl(baseUrl: String?, parameters: RequestParameters<T>) =
        buildUrl(baseUrl, parameters.url, parameters.queryParameters)

    open fun buildUrl(config: WebSocketConfig) =
        URI(buildUrl(null, config.url, config.queryParameters))

    open fun buildUrl(baseUrl: String?, url: String, queryParameters: Map<String, Any> = emptyMap()): String {
        val relOrAbsUrl = url

        val withoutQueryParameters = if (baseUrl != null && relOrAbsUrl.startsWith("http://", true) == false && relOrAbsUrl.startsWith("https://", true) == false) {
            // URI(baseUrl).resolve(url) is too stupid to add '/' if necessary or merge two '/', so do it manually
            "${baseUrl.removeSuffix("/")}/${relOrAbsUrl.removePrefix("/")}"
        } else {
            relOrAbsUrl
        }

        return if (queryParameters.isEmpty()) {
            withoutQueryParameters
        } else {
            val query = queryParameters.entries.joinToString("&", "?") { (name, value) ->
                "${URLEncoder.encode(name, Charsets.UTF_8)}=${URLEncoder.encode(value.toString(), Charsets.UTF_8)}"
            }
            withoutQueryParameters + query
        }
            .replace(" ", "%20") // is not a real encoding, but at least encodes white spaces
    }

    protected open fun createHeaderMap(headers: Map<String, String>, authentication: Authentication?, userAgent: String?,
                                       accept: String? = null, cookies: List<Cookie> = emptyList()): MutableMap<String, String> {
        val headers = mutableMapOf<String, String>()

        headers.forEach { (name, value) -> headers.put(name, value) }
        cookies.forEach { cookie -> headers.put("Cookie", "${cookie.name}=${cookie.value}") } // TODO: build Cookie header

        userAgent?.let { headers.put("User-Agent", it) }

        accept?.let {
            headers.put("Accept", accept)
        }

        createAuthorizationHeaderValue(authentication)?.let { authHeaderValue ->
            headers["Authorization"] = authHeaderValue
        }

        return headers
    }

    open fun createAuthorizationHeaderValue(auth: Authentication?): String? = when (auth) {
        is BearerAuthentication -> "Bearer ${auth.bearerToken}"
        is BasicAuthAuthentication -> "Basic " + Base64.getEncoder()
            .encodeToString("${auth.username}:${auth.password}".toByteArray())
        else -> null
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