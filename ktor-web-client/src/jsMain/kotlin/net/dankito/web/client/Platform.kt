package net.dankito.web.client

import io.ktor.client.*
import io.ktor.client.engine.js.*

actual object Platform {

    actual fun createPlatformSpecificHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient? =
        HttpClient(Js) {
            config(this)

            // JS Http Client has no option to disable certificate check
        }

}