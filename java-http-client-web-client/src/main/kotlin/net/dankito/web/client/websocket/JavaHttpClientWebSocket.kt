package net.dankito.web.client.websocket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import net.dankito.web.client.ClientConfig
import net.dankito.web.client.JavaHttpClientRequestConfigurer
import java.net.http.HttpClient
import java.nio.ByteBuffer
import java.util.concurrent.CompletionStage
import java.util.concurrent.CopyOnWriteArrayList

open class JavaHttpClientWebSocket(
    config: WebSocketConfig,
    httpClient: HttpClient = defaultHttpClient(),
    clientConfig: ClientConfig,
) : WebSocketBase(clientConfig.serializer), WebSocket {

    companion object {
        fun defaultHttpClient(): HttpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()
    }


    protected val coroutineScope = CoroutineScope(Dispatchers.IO)

    protected open val requestConfigurer = JavaHttpClientRequestConfigurer.Default


    protected val webSocket = configureNewWebSocket(httpClient, config, clientConfig).buildAsync(requestConfigurer.buildUrl(config), object : java.net.http.WebSocket.Listener {
        private val buffer = CopyOnWriteArrayList<CharSequence>() // do not use StringBuilder, it's not thread-safe

        override fun onText(webSocket: java.net.http.WebSocket, data: CharSequence, last: Boolean): CompletionStage<*>? {
            var job: Job? = null

            if (last == false) { // partial message (large messages sometimes get broken into parts/chunks) -> add to buffer
                buffer.add(data)
            } else {
                val message = if (buffer.isEmpty()) { // full message at once
                    data.toString()
                } else { // last part of chunked message retrieved
                    buffer.add(data)
                    val joinedMessage = buffer.joinToString("")
                    buffer.clear()
                    joinedMessage
                }

                // get off WebSocket thread to not block it for further messages; but only the handleTextMessage(), not the buffer handling
                job = coroutineScope.launch { handleTextMessage(message) }
            }

            super.onText(webSocket, data, last)
            return job?.asCompletableFuture()
        }

        override fun onBinary(webSocket: java.net.http.WebSocket, data: ByteBuffer, last: Boolean): CompletionStage<*>? {
            if (last == false) {
                log.error { "This is an error in our WebSocket implementation: It's not the last message chunk, but we " +
                        "haven't implemented buffering partial binary messages yet. Please handle partial chunks yourself." }
            }

            val future = coroutineScope.async {
                handleBinaryMessage(data.array())
            }.asCompletableFuture()

            super.onBinary(webSocket, data, last)

            return future
        }

        override fun onError(webSocket: java.net.http.WebSocket?, error: Throwable?) {
            coroutineScope.async {
                invokeOnErrorHandlers(error)
            }

            super.onError(webSocket, error)
        }

        override fun onClose(webSocket: java.net.http.WebSocket?, statusCode: Int, reason: String?): CompletionStage<*>? {
            val future = coroutineScope.async {
                webSocketClosed(statusCode, reason)
            }.asCompletableFuture()

            super.onClose(webSocket, statusCode, reason)
            return future
        }
    }).join()


    override suspend fun doSendTextMessage(message: String) {
        webSocket.sendText(message, true).await()
    }

    override suspend fun sendBinaryMessage(message: ByteArray, last: Boolean) {
        webSocket.sendBinary(ByteBuffer.wrap(message), last).await()
    }


    override suspend fun close(code: Int, reason: String?) {
        // 1000 indicates a normal closure, meaning that the purpose for which the connection was established has been fulfilled.
        // 1001 indicates that an endpoint is "going away", such as a server going down or a browser having navigated away from a page.
        webSocket.sendClose(code, reason ?: "").await() // null is not allowed for reason
    }


    protected open fun configureNewWebSocket(httpClient: HttpClient, config: WebSocketConfig, clientConfig: ClientConfig): java.net.http.WebSocket.Builder {
        val finalConfig = mergeConfig(config, clientConfig)
        val builder = httpClient.newWebSocketBuilder()

        requestConfigurer.configureRequest(builder, finalConfig)

        return builder
    }

    protected open fun mergeConfig(config: WebSocketConfig, clientConfig: ClientConfig) = WebSocketConfig(
        config.url, config.queryParameters, config.headers,
        config.userAgent ?: clientConfig.defaultUserAgent,
        config.authentication ?: clientConfig.authentication,
        config.cookies
    )

}