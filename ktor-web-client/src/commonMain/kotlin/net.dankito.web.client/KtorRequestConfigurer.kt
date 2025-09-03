package net.dankito.web.client

import io.ktor.client.plugins.compression.compress
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.basicAuth
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.cookie
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import io.ktor.http.userAgent
import io.ktor.util.date.GMTDate
import net.dankito.web.client.auth.Authentication
import net.dankito.web.client.auth.BasicAuthAuthentication
import net.dankito.web.client.auth.BearerAuthentication
import net.dankito.web.client.websocket.WebSocketConfig

open class KtorRequestConfigurer {

    companion object {
        val Default = KtorRequestConfigurer()
    }


    open fun <T : Any> configureRequest(builder: HttpRequestBuilder, method: HttpMethod, parameters: RequestParameters<T>, config: ClientConfig) = builder.apply {
        this.method = method

        configureCommonConfig(builder, parameters.url, parameters.queryParameters, parameters.authentication,
            parameters.headers, parameters.userAgent, parameters.cookies)

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
    }


    open fun configureRequest(builder: HttpRequestBuilder, config: WebSocketConfig) {
        configureCommonConfig(builder, config.url, config.queryParameters, config.authentication, config.headers, config.userAgent, config.cookies)
    }

    protected open fun configureCommonConfig(builder: HttpRequestBuilder, url: String, queryParameters: Map<String, Any>, authentication: Authentication?,
                                             headers: Map<String, String>, userAgent: String?, cookies: List<Cookie>) = builder.apply {
        url {
            val url = url.replace(" ", "%20") // is not a real encoding, but at least encodes white spaces
            if (url.startsWith("http", true) || url.startsWith("ws", true)) { // absolute url
                takeFrom(url)
            } else { // relative url
                appendPathSegments(url)
            }

            queryParameters.forEach { (name, value) -> this.parameters.append(name, value.toString()) }
        }

        headers.forEach { (name, value) ->
            this.headers.append(name, value)
        }

        userAgent?.let {
            this.userAgent(userAgent)
        }

        cookies.forEach { cookie ->
            this.cookie(cookie.name, cookie.value, 0, cookie.expiresAt?.let { GMTDate(it) }, cookie.domain, cookie.path, cookie.secure, cookie.httpOnly)
        }


        authentication?.let { authentication ->
            (authentication as? BasicAuthAuthentication)?.let { basicAuth ->
                this.basicAuth(basicAuth.username, basicAuth.password)
            }

            (authentication as? BearerAuthentication)?.let { bearerAuth ->
                this.bearerAuth(bearerAuth.bearerToken)
            }
        }
    }

}