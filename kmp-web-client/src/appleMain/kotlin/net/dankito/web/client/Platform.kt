package net.dankito.web.client

import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import kotlinx.cinterop.UnsafeNumber
import platform.Foundation.*

actual object Platform {

    @OptIn(UnsafeNumber::class)
    actual fun createPlatformSpecificHttpClient(
        ignoreCertificateErrors: Boolean,
        config: HttpClientConfig<*>.() -> Unit
    ): HttpClient? = HttpClient(Darwin) {
        config(this)

        engine {
            if (ignoreCertificateErrors) {
                this.handleChallenge { session, task, challenge, completionHandler ->
                    if (challenge.protectionSpace.authenticationMethod == "NSURLAuthenticationMethodServerTrust") {
                        val credentials = NSURLCredential.create(challenge.protectionSpace.serverTrust)
                        completionHandler(NSURLSessionAuthChallengeUseCredential, credentials)
                    } else {
                        completionHandler(NSURLSessionAuthChallengePerformDefaultHandling, null)
                    }

                }
            }
        }
    }

}