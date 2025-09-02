package net.dankito.web.client.websocket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dankito.web.client.auth.Authentication
import net.dankito.web.client.auth.BasicAuthAuthentication
import net.dankito.web.client.auth.BearerAuthentication
import java.net.URI
import java.net.http.HttpClient
import java.util.Base64
import java.util.concurrent.CompletionStage

open class JavaHttpClientWebSocket(
    url: String,
    authentication: Authentication? = null,
    httpClient: HttpClient = defaultHttpClient()
) : WebSocketBase(), WebSocket {

    companion object {
        fun defaultHttpClient(): HttpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()
    }


    protected val coroutineScope = CoroutineScope(Dispatchers.IO)


    protected val webSocket = configureNewWebSocket(httpClient, authentication).buildAsync(URI(url), object : java.net.http.WebSocket.Listener {
        private val buffer = StringBuilder()

        override fun onText(webSocket: java.net.http.WebSocket, data: CharSequence, last: Boolean): CompletionStage<*>? {
            if (last == false) { // partial message (large messages sometimes get broken into parts/chunks) -> add to buffer
                buffer.append(data)
            } else {
                if (buffer.isEmpty()) { // full message at once
                    handleTextMessage(data.toString())
                } else { // last part of chunked message retrieved
                    buffer.append(data)
                    handleTextMessage(buffer.toString())
                    buffer.clear()
                }
            }

            return super.onText(webSocket, data, last)
        }

        override fun onError(webSocket: java.net.http.WebSocket?, error: Throwable?) {
            invokeOnErrorHandlers(error)
            super.onError(webSocket, error)
        }

        override fun onClose(webSocket: java.net.http.WebSocket?, statusCode: Int, reason: String?): CompletionStage<*>? {
            invokeOnCloseHandlers(statusCode, reason)
            return super.onClose(webSocket, statusCode, reason)
        }
    }).join()


    override fun close(reason: String?) {
        // 1000 indicates a normal closure, meaning that the purpose for which the connection was established has been fulfilled.
        // 1001 indicates that an endpoint is "going away", such as a server going down or a browser having navigated away from a page.
        webSocket.sendClose(1001, reason ?: "") // null is not allowed for reason
    }


    override fun handleTextMessage(message: String) {
        coroutineScope.launch { // get off WebSocket thread to not block it for further messages
            super.handleTextMessage(message)
        }
    }


    protected open fun configureNewWebSocket(httpClient: HttpClient, authentication: Authentication?): java.net.http.WebSocket.Builder {
        val builder = httpClient.newWebSocketBuilder()

        (authentication as? BasicAuthAuthentication)?.let { basicAuth ->
            val authHeader = "Basic " + Base64.getEncoder()
                .encodeToString("${basicAuth.username}:${basicAuth.password}".toByteArray())
            builder.header("Authorization", authHeader)
        }

        (authentication as? BearerAuthentication)?.let { bearerAuth ->
            builder.header("Authorization", "Bearer ${bearerAuth.bearerToken}")
        }

        return builder
    }

}