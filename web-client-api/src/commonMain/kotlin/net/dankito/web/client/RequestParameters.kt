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
    /**
     * If set to `true` or [ClientConfig.enableBodyCompression] is set to `true` compresses request body if supported.
     *
     * Currently only Ktor 3 supports request body compression.
     */
    open val compressBodyIfSupported: Boolean = false,
    open val cookies: List<Cookie> = mutableListOf(),
    open val userAgent: String? = DefaultUserAgent,
    open val connectTimeoutMillis: Long? = null,
    open val socketTimeoutMillis: Long? = null,
    open val requestTimeoutMillis: Long? = null,
) {

    companion object {
        const val DefaultContentType = ContentTypes.JSON

        const val DefaultUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"

        const val DefaultMobileUserAgent = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"
    }

}