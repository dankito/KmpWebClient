package net.dankito.web.client

import net.dankito.datetime.Instant
import net.dankito.web.client.header.LinkHeader
import net.dankito.web.client.header.LinkHeaderParser

open class ResponseDetails(
    val method: String,
    val parameters: RequestParameters<*>,

    val statusCode: Int,
    val reasonPhrase: String,

    // TODO: map to platform independent Instance objects
    requestTime: Instant? = null,
    responseTime: Instant = Instant.now(),

    httpProtocolVersion: String? = null,

    headers: Map<String, List<String>> = emptyMap(),
    cookies: List<Cookie> = emptyList(),

    // TODO: can be removed now, are just here to keep binary compatiblity
    contentType: String? = null,
    contentLength: Long? = null,
    charset: String? = null,
) {

    open val requestTime: Instant? = requestTime
    open val responseTime: Instant = responseTime

    open val httpProtocolVersion: String? = httpProtocolVersion

    open val headers: Map<String, List<String>> = headers

    /**
     * Maps header names to first header value.
     *
     * For some headers there may be more than one value, e.g. Set-Cookie.
     * In this case the first value is returned.
     *
     * But for most headers there's only one value, e.g. Content-Type.
     * In this case the value itself is returned.
     *
     * Key of returned Map<String, String> is the header name and value is the first header value.
     */
    open val headersFirstValue: Map<String, String> by lazy { this.headers.mapValues { it.value.first() } }

    open val cookies: List<Cookie> = cookies


    open fun getHeaderValue(headerName: String): String? {
        val headerNameLowerCased = headerName.lowercase() // header names are case insensitive, so compare them lower cased

        headers.let { headers ->
            headers.keys.forEach {
                if(it.lowercase() == headerNameLowerCased) {
                    return headers[it]?.firstOrNull()
                }
            }
        }

        return null
    }

    // parsed headers:
    open val contentType: String? by lazy { contentType ?: getHeaderValue("Content-Type") }
    open val contentLength: Long? by lazy { contentLength ?: getHeaderValue("Content-Length")?.toLongOrNull() }
    open val charset: String? = charset // TODO: extract Charset from Content-Type

    open val redirectLocation: String? by lazy { getHeaderValue("Location") }

    open val linkHeader: List<LinkHeader>? by lazy {
        getHeaderValue("Link")?.let { LinkHeaderParser.Instance.parse(it) }
    }

    open fun getLinkHeaderNextUrl(): String? =
        linkHeader?.firstOrNull { it.parameters["rel"] == "next" }?.url


    open val isInformationalResponse: Boolean
        get() = statusCode >= 100 && statusCode < 200

    open val isSuccessResponse: Boolean
        get() = statusCode >= 200 && statusCode < 300

    open val isRedirectionResponse: Boolean
        get() = statusCode >= 300 && statusCode < 400

    open val isClientErrorResponse: Boolean
        get() = statusCode >= 400 && statusCode < 500

    open val isServerErrorResponse: Boolean
        get() = statusCode >= 500 && statusCode < 600

    open fun containsCookie(cookieName: String): Boolean {
        return getCookie(cookieName) != null
    }

    open fun getCookie(cookieName: String): Cookie? {
        return cookies.firstOrNull { cookieName == it.name }
    }

    open val retryAfterSeconds: Int? by lazy {
        getHeaderValue("Retry-After")?.toIntOrNull()
    }


    override fun toString() = "$statusCode $reasonPhrase${if (contentType != null) " $contentType" else ""}"
}