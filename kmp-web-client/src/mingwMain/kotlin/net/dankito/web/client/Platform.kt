package net.dankito.web.client

import io.ktor.client.*
import io.ktor.client.engine.curl.*
import io.ktor.client.engine.winhttp.*

actual object Platform {

    actual fun createPlatformSpecificHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient? {
        val engine = NativePlatformCommon.getFirstOfSupportedHttpClient(KtorEngine.WinHttp, KtorEngine.CIO, KtorEngine.Curl)

        return when (engine) {
            KtorEngine.WinHttp -> createWinHttpClient(ignoreCertificateErrors, config)
            // iOS, ... crashes when ktor-curl is added to dependencies so we need a special handling here
            KtorEngine.Curl -> createCurlHttpClient(ignoreCertificateErrors, config)
            else -> NativePlatformCommon.createHttpClient(engine, ignoreCertificateErrors, config)
        }
    }

    fun createWinHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient =
        HttpClient(WinHttp) {
            config(this)

            // WinHttp does not have an option to disable certificate check
        }

    fun createCurlHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient =
        HttpClient(Curl) {
            config(this)

            engine {
                this.sslVerify = ignoreCertificateErrors == false
            }
        }

}