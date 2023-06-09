package net.dankito.web.client

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.util.*

object NativePlatformCommon {

    @OptIn(InternalAPI::class)
    val availableEngines: LinkedHashSet<KtorEngine> = engines.mapNotNull { container ->
        KtorEngine.values().firstOrNull { name -> container.toString() == name.engineName }
    }.toCollection(LinkedHashSet())


    fun getFirstOfSupportedHttpClient(vararg supportedEngines: KtorEngine): KtorEngine? =
        availableEngines.firstOrNull { supportedEngines.contains(it) }

    fun createHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit, vararg supportedEngines: KtorEngine) =
        createHttpClient(getFirstOfSupportedHttpClient(*supportedEngines), ignoreCertificateErrors, config)

    fun createHttpClient(engine: KtorEngine?, ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit) =
        when (engine) {
            KtorEngine.CIO -> createCIOHttpClient(ignoreCertificateErrors, config)
            else -> HttpClient(config)
        }


    fun createCIOHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient =
        HttpClient(CIO) {
            config(this)

            engine {
                https {
                    // CIO on native platforms does not have an option to disable certificate check
                }
            }
        }

}