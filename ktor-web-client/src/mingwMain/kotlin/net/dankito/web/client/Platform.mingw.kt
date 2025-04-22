package net.dankito.web.client

import io.ktor.client.*
import io.ktor.client.engine.winhttp.*

actual object Platform {

    actual val availableEngines: LinkedHashSet<KtorEngine> = NativePlatformCommon.availableEngines

    actual val preferredEngines: List<KtorEngine> = listOf(KtorEngine.WinHttp)


    actual fun createPlatformSpecificHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient? {
        val engine = KtorClientConfiguration.getFirstOfSupportedHttpClient()

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