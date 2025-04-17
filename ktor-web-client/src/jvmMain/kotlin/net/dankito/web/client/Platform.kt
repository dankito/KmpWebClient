package net.dankito.web.client

import io.ktor.client.*
import io.ktor.client.engine.java.*

actual object Platform {

    actual fun createPlatformSpecificHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient? =
        JavaPlatformCommon.createPlatformSpecificHttpClient(ignoreCertificateErrors, config)

}