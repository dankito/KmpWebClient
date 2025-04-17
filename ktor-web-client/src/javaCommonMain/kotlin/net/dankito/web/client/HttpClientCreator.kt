package net.dankito.web.client

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.engine.apache.*
import io.ktor.client.engine.cio.*
import io.ktor.client.engine.java.*
import io.ktor.client.engine.jetty.*
import io.ktor.client.engine.okhttp.*
import org.eclipse.jetty.util.ssl.SslContextFactory

open class HttpClientCreator {

    /**
     * If you call the HttpClient constructor without an argument, the client will choose an engine
     * automatically depending on the artifacts added in a build script.
     */
    open fun createDefaultHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient =
        KtorWebClient.createDefaultHttpClient(config)


    open fun createOkHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient =
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

    open fun createCIOHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient =
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

    open fun createApacheHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient =
        HttpClient(Apache) {
            config(this)

            engine {
                if (ignoreCertificateErrors) {
                    sslContext = SslSettings.trustAllCertificatesSslContext
                }
            }
        }

    open fun createJettyHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient =
        HttpClient(Jetty) {
            config (this)

            engine {
                if (ignoreCertificateErrors) {
                    sslContextFactory = SslContextFactory.Client().apply {
                        sslContext = SslSettings.trustAllCertificatesSslContext
                    }
                }
            }
        }

    open fun createJavaHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient =
        HttpClient(Java) {
            config(this)

            engine {
                config {
                    if (ignoreCertificateErrors) {
                        sslContext(SslSettings.trustAllCertificatesSslContext)
                    }
                }
            }
        }

    open fun createAndroidHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient =
        HttpClient(Android) {
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