package net.dankito.web.client

import io.ktor.client.*
import io.ktor.client.engine.java.*

actual object Platform {

    actual fun createPlatformSpecificHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient? {
        val engine = JavaPlatformCommon.getFirstOfSupportedHttpClient(KtorEngine.OkHttp, KtorEngine.CIO, KtorEngine.Java, KtorEngine.Apache, KtorEngine.Jetty)

        return when (engine) {
            // Java HttpClient is not supported on Android target as it requires JDK 9+ whilst Android target is compiled with JDK 8
            KtorEngine.Java -> createJavaHttpClient(ignoreCertificateErrors, config)
            else -> JavaPlatformCommon.createHttpClient(engine, ignoreCertificateErrors, config)
        }
    }

    fun createJavaHttpClient(ignoreCertificateErrors: Boolean, config: HttpClientConfig<*>.() -> Unit): HttpClient =
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

}