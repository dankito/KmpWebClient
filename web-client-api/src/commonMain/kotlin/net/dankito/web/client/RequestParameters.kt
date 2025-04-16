package net.dankito.web.client

import kotlin.reflect.KClass

open class RequestParameters<T : Any>(
    open val url: String,
    open val responseClass: KClass<T>? = null,
    open val body: Any? = null,
    open val contentType: String? = null,
    open val accept: String? = null,
    open val headers: Map<String, String> = mutableMapOf(),
    open val queryParameters: Map<String, Any> = mapOf(),
    open val cookies: List<Cookie> = mutableListOf(),
    open val userAgent: String? = DefaultUserAgent,
    open val connectTimeoutMillis: Long = 5_000, // to have a faster response / result when connecting is not possible
    open val socketTimeoutMillis: Long? = null,
    open val requestTimeoutMillis: Long = 15_000 // in slow environments give request some time to complete (but shouldn't be necessary, we only have small response bodies)
) {

    companion object {
        const val DefaultContentType = ContentTypes.JSON

        const val DefaultUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"

        const val DefaultMobileUserAgent = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"
    }

}