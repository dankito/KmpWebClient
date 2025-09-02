package net.dankito.web.client

import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.date.*
import net.dankito.web.client.auth.BasicAuthAuthentication
import net.dankito.web.client.auth.BearerAuthentication

open class KtorRequestConfigurer {

    companion object {
        val Default = KtorRequestConfigurer()
    }


    open fun <T : Any> configureRequest(builder: HttpRequestBuilder, method: HttpMethod, parameters: RequestParameters<T>, config: ClientConfig) = builder.apply {
        this.method = method

        url {
            val url = parameters.url.replace(" ", "%20") // is not a real encoding, but at least encodes white spaces
            if (url.startsWith("http", true)) { // absolute url
                takeFrom(url)
            } else { // relative url
                appendPathSegments(url)
            }

            parameters.queryParameters.forEach { (name, value) -> this.parameters.append(name, value.toString()) }
        }

        parameters.headers.forEach { (name, value) ->
            this.headers.append(name, value)
        }

        parameters.cookies.forEach { cookie ->
            this.cookie(cookie.name, cookie.value, 0, cookie.expiresAt?.let { GMTDate(it) }, cookie.domain, cookie.path, cookie.secure, cookie.httpOnly)
        }

        parameters.userAgent?.let {
            this.userAgent(it)
        }

        this.accept(ContentType.parse(parameters.accept ?: config.defaultAccept))

        timeout {
            // JS doesn't support connectTimeout and socketTimeout
            parameters.connectTimeoutMillis?.let { connectTimeoutMillis = it }
            parameters.socketTimeoutMillis?.let { socketTimeoutMillis = it }
            parameters.requestTimeoutMillis?.let { requestTimeoutMillis = it }
        }

        parameters.body?.let {
            contentType((parameters.contentType ?: config.defaultContentType).let { ContentType.parse(it) })

            setBody(it)
        }

        parameters.authentication?.let { authentication ->
            (authentication as? BasicAuthAuthentication)?.let { basicAuth ->
                this.basicAuth(basicAuth.username, basicAuth.password)
            }

            (authentication as? BearerAuthentication)?.let { bearerAuth ->
                this.bearerAuth(bearerAuth.bearerToken)
            }
        }
    }

}