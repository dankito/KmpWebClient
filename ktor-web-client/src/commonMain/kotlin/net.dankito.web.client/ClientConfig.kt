package net.dankito.web.client

import net.dankito.web.client.auth.Authentication

open class ClientConfig(
    open val baseUrl: String? = null,
    open val authentication: Authentication? = null,

    /**
     * Be aware the following engines do not support disabling certificate check:
     * - JavaScript HttpClient
     * - CIO on native platforms
     * - WinHttp
     */
    open val ignoreCertificateErrors: Boolean = false,

    open val defaultUserAgent: String? = RequestParameters.DefaultMobileUserAgent,
    open val defaultContentType: String = ContentTypes.JSON,


    // JS doesn't support connectTimeout and socketTimeout
    open val connectTimeoutMillis: Long? = 5_000, // to have a faster response / result when connecting is not possible
    open val socketTimeoutMillis: Long? = null,
    open val requestTimeoutMillis: Long? = 15_000, // in slow environments give request some time to complete (but shouldn't be necessary, we only have small response bodies)
) {
    override fun toString() = "baseUrl = $baseUrl, authentication = $authentication, ignoreCertificateErrors = $ignoreCertificateErrors"
}