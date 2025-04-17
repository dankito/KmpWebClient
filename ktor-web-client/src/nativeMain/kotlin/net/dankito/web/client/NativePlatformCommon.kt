package net.dankito.web.client

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.util.*

object NativePlatformCommon {

    @OptIn(InternalAPI::class)
    val availableEngines: LinkedHashSet<KtorEngine> = engines.mapNotNull { container ->
        KtorEngine.entries.firstOrNull { name -> container.toString() == name.engineName }
    }.toCollection(LinkedHashSet())


    fun getFirstOfSupportedHttpClient(vararg supportedEngines: KtorEngine): KtorEngine? =
        availableEngines.firstOrNull { supportedEngines.contains(it) }

    /**
     * If you call the HttpClient constructor without an argument, the client will choose an engine
     * automatically depending on the artifacts added in a build script.
     */
    fun createDefaultHttpClient(engine: KtorEngine?, ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit) =
        HttpClient(config)

}