package net.dankito.web.client

import net.dankito.web.client.auth.Authentication
import kotlin.reflect.KClass

open class RequestParameters<T : Any>(
    open val url: String,
    open val responseClass: KClass<T>? = null,
    open val body: Any? = null,
    open val contentType: String? = null,
    open val accept: String? = null,
    open val headers: Map<String, String> = mutableMapOf(),
    open val queryParameters: Map<String, Any> = mapOf(),
    open val authentication: Authentication? = null,
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

        const val DefaultUserAgent = UserAgent.Chrome_Windows_138

        const val DefaultMobileUserAgent = UserAgent.Android
    }

}