package net.dankito.web.client

import io.ktor.client.*
import io.ktor.client.engine.winhttp.*

actual object Platform {

    actual fun createPlatformSpecificHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient? {
        val engine = NativePlatformCommon.getFirstOfSupportedHttpClient(KtorEngine.WinHttp)

        return when (engine) {
            KtorEngine.WinHttp -> createWinHttpClient(ignoreCertificateErrors, config)
            else -> KtorWebClient.createDefaultHttpClient(config)
        }
    }

    fun createWinHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient =
        HttpClient(WinHttp) {
            config(this)

            // WinHttp does not have an option to disable certificate check
        }

}