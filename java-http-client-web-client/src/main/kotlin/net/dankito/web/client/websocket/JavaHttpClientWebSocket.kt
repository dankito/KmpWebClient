package net.dankito.web.client.websocket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import net.dankito.web.client.JavaHttpClientRequestConfigurer
import net.dankito.web.client.serialization.Serializer
import java.net.http.HttpClient
import java.nio.ByteBuffer
import java.util.concurrent.CompletionStage

open class JavaHttpClientWebSocket(
    config: WebSocketConfig,
    httpClient: HttpClient = defaultHttpClient(),
    serializer: Serializer? = null,
) : WebSocketBase(serializer), WebSocket {

    companion object {
        fun defaultHttpClient(): HttpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()
    }


    protected val coroutineScope = CoroutineScope(Dispatchers.IO)

    protected open val requestConfigurer = JavaHttpClientRequestConfigurer.Default


    protected val webSocket = configureNewWebSocket(httpClient, config).buildAsync(requestConfigurer.buildUrl(config), object : java.net.http.WebSocket.Listener {
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

        override fun onBinary(webSocket: java.net.http.WebSocket, data: ByteBuffer, last: Boolean): CompletionStage<*>? {
            if (last == false) {
                log.error { "This is an error in our WebSocket implementation: It's not the last message chunk, but we " +
                        "haven't implemented buffering partial binary messages yet. Please handle partial chunks yourself." }
            }

            handleBinaryMessage(data.array())

            return super.onBinary(webSocket, data, last)
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


    override suspend fun close(code: Int, reason: String?) {
        // 1000 indicates a normal closure, meaning that the purpose for which the connection was established has been fulfilled.
        // 1001 indicates that an endpoint is "going away", such as a server going down or a browser having navigated away from a page.
        webSocket.sendClose(code, reason ?: "").await() // null is not allowed for reason
    }


    override fun handleTextMessage(message: String) {
        coroutineScope.launch { // get off WebSocket thread to not block it for further messages
            super.handleTextMessage(message)
        }
    }


    protected open fun configureNewWebSocket(httpClient: HttpClient, config: WebSocketConfig): java.net.http.WebSocket.Builder {
        val builder = httpClient.newWebSocketBuilder()

        requestConfigurer.configureRequest(builder, config)

        return builder
    }

}