package net.dankito.web.client

import io.ktor.client.*

actual object Platform {

    actual val availableEngines: LinkedHashSet<KtorEngine> = JavaPlatformCommon.availableEngines

    actual val preferredEngines: List<KtorEngine> =
        // list default engine last so that including a different engine dependency overwrites default engine
        listOf(KtorEngine.Java, KtorEngine.OkHttp, KtorEngine.Apache, KtorEngine.Jetty, KtorEngine.CIO)


    actual fun createPlatformSpecificHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient? =
        JavaPlatformCommon.createPlatformSpecificHttpClient(ignoreCertificateErrors, config)

}