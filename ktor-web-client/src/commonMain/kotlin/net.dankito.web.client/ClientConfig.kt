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
) {
    override fun toString() = "baseUrl = $baseUrl, authentication = $authentication, ignoreCertificateErrors = $ignoreCertificateErrors"
}