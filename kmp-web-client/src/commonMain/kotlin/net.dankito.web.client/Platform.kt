package net.dankito.web.client

import io.ktor.client.*

expect object Platform {

    fun createPlatformSpecificHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit = {}): HttpClient?

}