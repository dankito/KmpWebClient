package net.dankito.web.client

import io.ktor.client.*
import java.util.*

object JavaPlatformCommon {

    // copied from Ktor source, this is how Ktor loads available engines on the JVM
    val engines: List<HttpClientEngineContainer> = HttpClientEngineContainer::class.java.let {
        ServiceLoader.load(it, it.classLoader).toList()
    }

    val availableEngines: LinkedHashSet<KtorEngine> = engines.mapNotNull { container ->
        KtorEngine.entries.firstOrNull { name -> container.toString() == name.engineName }
    }.toCollection(LinkedHashSet())


    fun getFirstOfSupportedHttpClient(vararg supportedEngines: KtorEngine): KtorEngine? =
        availableEngines.firstOrNull { supportedEngines.contains(it) }

    fun createHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit, vararg supportedEngines: KtorEngine) =
        createHttpClient(getFirstOfSupportedHttpClient(*supportedEngines), ignoreCertificateErrors, config)

    fun createHttpClient(engine: KtorEngine?, ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit) =
        when (engine) {
            KtorEngine.OkHttp -> HttpClientCreator.createOkHttpClient(ignoreCertificateErrors, config)
            KtorEngine.CIO -> HttpClientCreator.createCIOHttpClient(ignoreCertificateErrors, config)
            KtorEngine.Apache -> HttpClientCreator.createApacheHttpClient(ignoreCertificateErrors, config)
            KtorEngine.Jetty -> HttpClientCreator.createJettyHttpClient(ignoreCertificateErrors, config)
            KtorEngine.Android -> HttpClientCreator.createAndroidHttpClient(ignoreCertificateErrors, config)
            else -> HttpClientCreator.fallback(ignoreCertificateErrors, config)
        }

}