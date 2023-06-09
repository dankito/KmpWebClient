package net.dankito.web.client

import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

object SslSettings {

    val trustAllCertificatesTrustManager = object : X509TrustManager {

        override fun getAcceptedIssuers(): Array<X509Certificate?> = arrayOf()

        override fun checkClientTrusted(certs: Array<X509Certificate?>?, authType: String?) { }

        override fun checkServerTrusted(certs: Array<X509Certificate?>?, authType: String?) {
            // trust all certificates
        }
    }

    val trustAllCertificatesSslContext: SSLContext by lazy {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustAllCertificatesTrustManager), null)

        sslContext
    }

    val trustAllCertificatesSocketFactory: SSLSocketFactory by lazy {
        trustAllCertificatesSslContext.socketFactory
    }

}