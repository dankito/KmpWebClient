package net.dankito.web.client

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.engine.apache.*
import io.ktor.client.engine.cio.*
import io.ktor.client.engine.jetty.*
import io.ktor.client.engine.okhttp.*
import org.eclipse.jetty.util.ssl.SslContextFactory
import java.util.*

object JavaPlatformCommon {

    // copied from Ktor source, this is how Ktor loads available engines on the JVM
    val engines: List<HttpClientEngineContainer> = HttpClientEngineContainer::class.java.let {
        ServiceLoader.load(it, it.classLoader).toList()
    }

    val availableEngines: LinkedHashSet<KtorEngine> = engines.mapNotNull { container ->
        KtorEngine.values().firstOrNull { name -> container.toString() == name.engineName }
    }.toCollection(LinkedHashSet())


    fun getFirstOfSupportedHttpClient(vararg supportedEngines: KtorEngine): KtorEngine? =
        availableEngines.firstOrNull { supportedEngines.contains(it) }

    fun createHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit, vararg supportedEngines: KtorEngine) =
        createHttpClient(getFirstOfSupportedHttpClient(*supportedEngines), ignoreCertificateErrors, config)

    fun createHttpClient(engine: KtorEngine?, ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit) =
        when (engine) {
            KtorEngine.OkHttp -> createOkHttpClient(ignoreCertificateErrors, config)
            KtorEngine.CIO -> createCIOHttpClient(ignoreCertificateErrors, config)
            KtorEngine.Apache -> createApacheHttpClient(ignoreCertificateErrors, config)
            KtorEngine.Jetty -> createJettyHttpClient(ignoreCertificateErrors, config)
            KtorEngine.Android -> createAndroidHttpClient(ignoreCertificateErrors, config)
            else -> HttpClient(config)
        }

    fun createOkHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient =
        HttpClient(OkHttp) {
            config(this)

            engine {
                config {
                    if (ignoreCertificateErrors) {
                        sslSocketFactory(SslSettings.trustAllCertificatesSocketFactory, SslSettings.trustAllCertificatesTrustManager)
                    }
                }
            }
        }

    fun createCIOHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient =
        HttpClient(CIO) {
            config(this)

            engine {
                https {
                    if (ignoreCertificateErrors) {
                        trustManager = SslSettings.trustAllCertificatesTrustManager
                    }
                }
            }
        }

    fun createApacheHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient =
        HttpClient(Apache) {
            config(this)

            engine {
                if (ignoreCertificateErrors) {
                    sslContext = SslSettings.trustAllCertificatesSslContext
                }
            }
        }

    fun createJettyHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient(Jetty) {
            config (this)

            engine {
                if (ignoreCertificateErrors) {
                    sslContextFactory = SslContextFactory.Client().apply {
                        sslContext = SslSettings.trustAllCertificatesSslContext
                    }
                }
            }
        }

    fun createAndroidHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient(Android) {
            config(this)

            engine {
                if (ignoreCertificateErrors) {
                    sslManager = { httpsURLConnection ->
                        httpsURLConnection.sslSocketFactory = SslSettings.trustAllCertificatesSocketFactory
                    }
                }
            }
        }

}