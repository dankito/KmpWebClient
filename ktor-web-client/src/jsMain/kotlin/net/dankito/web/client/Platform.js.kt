package net.dankito.web.client

import io.ktor.client.*
import io.ktor.client.engine.js.*

actual object Platform {

    actual val availableEngines: LinkedHashSet<KtorEngine> = linkedSetOf(KtorEngine.Js)

    actual val preferredEngines: List<KtorEngine> = listOf(KtorEngine.Js)


    actual fun createPlatformSpecificHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient? =
        HttpClient(Js) {
            config(this)

            // JS Http Client has no option to disable certificate check
        }

}