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
import net.dankito.web.client.KtorRequestConfigurer
import net.dankito.web.client.serialization.Serializer

open class KtorWebSocket(
    config: WebSocketConfig,
    httpClient: HttpClient,
    serializer: Serializer? = null,
    protected val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : WebSocketBase(serializer), WebSocket {


    protected lateinit var session: DefaultWebSocketSession

    protected var isOpen = false // TODO: make thread safe

    protected open val requestConfigurer: KtorRequestConfigurer = KtorRequestConfigurer.Default


    init {
        initWebSocket(config, httpClient)
    }

    protected open fun initWebSocket(config: WebSocketConfig, httpClient: HttpClient) {
        coroutineScope.launch {
            session = httpClient.webSocketSession(config.url) {
                requestConfigurer.configureRequest(this, config)
            }

            isOpen = true

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

    protected open fun handleOnClose(reason: CloseReason?) {
        isOpen = false

        invokeOnCloseHandlers(reason?.code?.toInt() ?: -1, reason?.message)
    }


    override suspend fun close(code: Int, reason: String?) {
        session.close(CloseReason(code.toShort(), reason ?: ""))
    }

}