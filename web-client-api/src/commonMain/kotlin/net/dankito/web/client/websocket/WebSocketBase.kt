package net.dankito.web.client.websocket

import net.codinux.log.logger

abstract class WebSocketBase : WebSocket {


    protected val onTextMessageHandlers = mutableListOf<(String) -> Unit>() // TODO: make thread-safe

    protected val onErrorHandlers = mutableListOf<(Throwable?) -> Unit>() // TODO: make thread-safe

    protected val onCloseHandlers = mutableListOf<(Int, String?) -> Unit>() // TODO: make thread-safe

    protected val log by logger()


    override fun onTextMessage(handler: (String) -> Unit) {
        onTextMessageHandlers.add(handler)
    }

    protected open fun handleTextMessage(message: String) {
        if (onTextMessageHandlers.isEmpty()) {
            log.warn { "Retrieved message but no onTextMessage handlers are registered" }
        } else {
            onTextMessageHandlers.toList().forEach { handler ->
                try {
                    handler(message)
                } catch (e: Throwable) {
                    log.error(e) { "$handler threw an error while handling message: $message" }
                }
            }
        }
    }


    protected open fun handleBinaryMessage(message: ByteArray) {
        // TODO
    }


    override fun onError(handler: (error: Throwable?) -> Unit) {
        onErrorHandlers.add(handler)
    }

    protected open fun invokeOnErrorHandlers(error: Throwable?) {
        onErrorHandlers.toList().forEach { handler ->
            handler(error)
        }
    }

    override fun onClose(handler: (statusCode: Int, reason: String?) -> Unit) {
        onCloseHandlers.add(handler)
    }

    protected open fun invokeOnCloseHandlers(statusCode: Int, reason: String?) {
        onCloseHandlers.toList().forEach { handler ->
            handler(statusCode, reason)
        }
    }

}