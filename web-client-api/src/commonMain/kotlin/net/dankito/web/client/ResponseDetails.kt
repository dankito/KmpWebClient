package net.dankito.web.client

import net.dankito.datetime.Instant

open class ResponseDetails(
    val statusCode: Int,
    val reasonPhrase: String,

    // TODO: map to platform independent Instance objects
    requestTime: Instant? = null,
    responseTime: Instant = Instant.now(),

    httpProtocolVersion: String? = null,

    headers: Map<String, List<String>> = emptyMap(),
    cookies: List<Cookie> = emptyList(),

    contentType: String? = null,
    contentLength: Long? = null,
    charset: String? = null,
) {

    open val requestTime: Instant? = requestTime
    open val responseTime: Instant = responseTime

    open val httpProtocolVersion: String? = httpProtocolVersion

    open val headers: Map<String, List<String>> = headers
    open val cookies: List<Cookie> = cookies

    // parsed headers:
    open val contentType: String? = contentType
    open val contentLength: Long? = contentLength
    open val charset: String? = charset


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