package net.dankito.web.client.websocket

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import io.ktor.websocket.readReason
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dankito.web.client.ClientConfig
import net.dankito.web.client.KtorRequestConfigurer

open class KtorWebSocket(
    protected val session: DefaultWebSocketSession,
    clientConfig: ClientConfig,
    protected val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : WebSocketBase(clientConfig.serializer), WebSocket {

    companion object {
        suspend fun create(config: WebSocketConfig, httpClient: HttpClient, clientConfig: ClientConfig,
                           requestConfigurer: KtorRequestConfigurer = KtorRequestConfigurer.Default): KtorWebSocket {
            val finalConfig = mergeConfig(config, clientConfig)

            val session = httpClient.webSocketSession(finalConfig.url) {
                requestConfigurer.configureRequest(this, finalConfig)
            }

            return KtorWebSocket(session, clientConfig)
        }

        fun mergeConfig(config: WebSocketConfig, clientConfig: ClientConfig) = WebSocketConfig(
            config.url, config.queryParameters, config.headers,
            config.userAgent ?: clientConfig.defaultUserAgent,
            config.authentication ?: clientConfig.authentication,
            config.cookies
        )
    }


    protected var isOpen = false // TODO: make thread safe


    init {
        isOpen = true

        coroutineScope.launch {
            receiveLoop(session)
        }
    }

    protected open suspend fun receiveLoop(session: DefaultWebSocketSession) {
        while (isOpen) {
            try {
                val frame = session.incoming.receive()
                when (frame) {
                    is Frame.Text -> handleTextMessage(frame.readText())
                    is Frame.Binary -> handleBinaryMessage(frame.readBytes())
                    is Frame.Close -> handleOnClose(frame.readReason())
                    else -> { } // ignore Ping and Pong messages
                }
            } catch (e: Throwable) {
                log.error(e) { "WebSocket threw an error while receiving" }
            }
        }
    }

    protected open suspend fun handleOnClose(reason: CloseReason?) {
        isOpen = false

        invokeOnCloseHandlers(reason?.code?.toInt() ?: -1, reason?.message)
    }


    override suspend fun doSendTextMessage(message: String) {
        session.send(Frame.Text(message))
    }

    override suspend fun sendBinaryMessage(message: ByteArray, last: Boolean) {
        session.send(Frame.Binary(last, message))
    }

    override suspend fun close(code: Int, reason: String?) {
        session.close(CloseReason(code.toShort(), reason ?: ""))
    }

}