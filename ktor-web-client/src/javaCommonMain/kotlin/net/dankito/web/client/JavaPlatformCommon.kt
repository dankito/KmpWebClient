package net.dankito.web.client

import io.ktor.client.*
import java.util.*

object JavaPlatformCommon {

    var clientCreator: HttpClientCreator = HttpClientCreator()


    // copied from Ktor source, this is how Ktor loads available engines on the JVM
    val engines: List<HttpClientEngineContainer> = HttpClientEngineContainer::class.java.let {
        ServiceLoader.load(it, it.classLoader).toList()
    }

    val availableEngines: LinkedHashSet<KtorEngine> = engines.mapNotNull { container ->
        KtorEngine.entries.firstOrNull { name -> container.toString() == name.engineName }
    }.toCollection(LinkedHashSet())


    fun createPlatformSpecificHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient? =
        createHttpClient(getFirstOfSupportedHttpClient(Platform.preferredEngines), ignoreCertificateErrors, config)

    fun createHttpClient(engine: KtorEngine?, ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit) =
        when (engine) {
            KtorEngine.OkHttp -> clientCreator.createOkHttpClient(ignoreCertificateErrors, config)
            KtorEngine.CIO -> clientCreator.createCIOHttpClient(ignoreCertificateErrors, config)
            KtorEngine.Apache -> clientCreator.createApacheHttpClient(ignoreCertificateErrors, config)
            KtorEngine.Jetty -> clientCreator.createJettyHttpClient(ignoreCertificateErrors, config)
            KtorEngine.Java -> clientCreator.createJavaHttpClient(ignoreCertificateErrors, config)
            KtorEngine.Android -> clientCreator.createAndroidHttpClient(ignoreCertificateErrors, config)
            else -> clientCreator.createDefaultHttpClient(config)
        }

    fun getFirstOfSupportedHttpClient(supportedEngines: List<KtorEngine>): KtorEngine? =
        availableEngines.firstOrNull { supportedEngines.contains(it) }

}