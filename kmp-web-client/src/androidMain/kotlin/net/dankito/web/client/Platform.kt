package net.dankito.web.client

import io.ktor.client.*

actual object Platform {

    actual fun createPlatformSpecificHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient? =
        JavaPlatformCommon.createHttpClient(ignoreCertificateErrors, config, KtorEngine.OkHttp, KtorEngine.Android, KtorEngine.CIO)

}