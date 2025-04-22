package net.dankito.web.client

import io.ktor.client.*

expect object Platform {

    val availableEngines: LinkedHashSet<KtorEngine>

    fun createPlatformSpecificHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit = {}): HttpClient?

}