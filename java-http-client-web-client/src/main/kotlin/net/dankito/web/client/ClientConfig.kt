package net.dankito.web.client

import kotlinx.coroutines.CoroutineDispatcher
import net.dankito.web.client.auth.Authentication

open class ClientConfig(
    open val baseUrl: String? = null,
    open val authentication: Authentication? = null,

    open val ignoreCertificateErrors: Boolean = false,

//    open val customClientConfig: ((HttpClientConfig<*>, config: ClientConfig) -> Unit)? = null,

    open val defaultUserAgent: String? = RequestParameters.DefaultMobileUserAgent,
    open val defaultContentType: String = ContentTypes.JSON,
    open val defaultAccept: String = ContentTypes.JSON,

    open val dispatcher: CoroutineDispatcher? = null,

    open val enableBodyCompression: Boolean = false,

    open val connectTimeoutMillis: Long? = 5_000, // to have a faster response / result when connecting is not possible
//    open val socketTimeoutMillis: Long? = null,
    open val requestTimeoutMillis: Long? = 15_000, // in slow environments give request some time to complete (but shouldn't be necessary, we only have small response bodies)

    open val logOutgoingRequests: Boolean = false,

    open val logSuccessfulResponses: Boolean = false,

    open val logErroneousResponses: Boolean = false,
) {
    override fun toString() = "baseUrl = $baseUrl, authentication = $authentication, ignoreCertificateErrors = $ignoreCertificateErrors"
}