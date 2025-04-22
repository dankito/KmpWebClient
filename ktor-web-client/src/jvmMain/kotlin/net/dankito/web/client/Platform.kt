package net.dankito.web.client

import io.ktor.client.*

actual object Platform {

    actual val availableEngines: LinkedHashSet<KtorEngine> = JavaPlatformCommon.availableEngines

    actual fun createPlatformSpecificHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient? =
        JavaPlatformCommon.createPlatformSpecificHttpClient(ignoreCertificateErrors, config)

}