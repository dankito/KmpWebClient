package net.dankito.web.client

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.engine.apache.*
import io.ktor.client.engine.cio.*
import io.ktor.client.engine.jetty.*
import io.ktor.client.engine.okhttp.*
import org.eclipse.jetty.util.ssl.SslContextFactory

object HttpClientCreator {

    fun fallback(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient =
        HttpClient(config)


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

    fun createJettyHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient =
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

    fun createAndroidHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient =
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