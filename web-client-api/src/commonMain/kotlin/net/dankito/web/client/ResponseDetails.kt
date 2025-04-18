package net.dankito.web.client

open class ResponseDetails(
    val statusCode: Int,
    val reasonPhrase: String,

    // TODO: map to platform independent Instance objects
    val requestTime: String,
    val responseTime: String,

    val httpProtocolVersion: String,

    val headers: Map<String, List<String>>,
    val cookies: List<Cookie> = emptyList(),

    // parsed headers:
    val contentType: String? = null,
    val contentLength: Long? = null,
    val charset: String? = null,
) {

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