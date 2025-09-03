package net.dankito.web.client.websocket

/**
 * A wrapper around [WebSocket] that reconnects on close message.
 * The user can decide on which close reasons and status codes to reconnect.
 */
open class ReconnectingWebSocket(
    var webSocket: WebSocket,
    protected val shouldReconnect: ((statusCode: Int, reason: String?) -> Boolean)? = null,
    protected val reconnect: suspend () -> WebSocket
) : WebSocketBase((webSocket as? WebSocketBase)?.getSerializer()), WebSocket {

    companion object {
        suspend fun create(shouldReconnect: ((statusCode: Int, reason: String?) -> Boolean)? = null, connect: suspend () -> WebSocket): ReconnectingWebSocket =
            ReconnectingWebSocket(connect(), shouldReconnect, connect)
    }


    init {
        initializeNewWebSocket(webSocket)
    }


    protected open fun initializeNewWebSocket(webSocket: WebSocket) {
        webSocket.onTextMessage { handleTextMessage(it) }
        webSocket.onBinaryMessage { handleBinaryMessage(it) }
        webSocket.onError { invokeOnErrorHandlers(it) }

        webSocket.onClose { statusCode, reason ->
            val shouldWeReconnect = shouldReconnect == null || shouldReconnect(statusCode, reason)
            log.info { "WebSocket $webSocket closed. Should reconnect? $shouldWeReconnect" }

            if (shouldWeReconnect) {
                onNewlyConnectedWebSocket(reconnect())
            } else {
                invokeOnCloseHandlers(statusCode, reason)
            }
        }
    }

    protected open fun onNewlyConnectedWebSocket(webSocket: WebSocket) {
        this.webSocket = webSocket

        initializeNewWebSocket(webSocket)
    }


    override suspend fun doSendTextMessage(message: String) =
        webSocket.sendTextMessage(message)

    override suspend fun sendBinaryMessage(message: ByteArray, last: Boolean) =
        webSocket.sendBinaryMessage(message, last)

    override suspend fun close(code: Int, reason: String?) =
        webSocket.close(code, reason)

}