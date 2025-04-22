package net.dankito.web.client

import io.ktor.client.*
import io.ktor.client.engine.curl.*

actual object Platform {

    actual val availableEngines: LinkedHashSet<KtorEngine> = NativePlatformCommon.availableEngines

    actual fun createPlatformSpecificHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient? {
        val engine = NativePlatformCommon.getFirstOfSupportedHttpClient(KtorEngine.Curl)

        return when (engine) {
            // iOS, ... crashes when ktor-curl is added to dependencies so we need a special handling here
            KtorEngine.Curl -> createCurlHttpClient(ignoreCertificateErrors, config)
            else -> KtorWebClient.createDefaultHttpClient(config)
        }
    }

    fun createCurlHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient =
        HttpClient(Curl) {
            config(this)

            engine {
                this.sslVerify = ignoreCertificateErrors == false
            }
        }

}