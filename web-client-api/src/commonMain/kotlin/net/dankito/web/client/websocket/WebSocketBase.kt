package net.dankito.web.client.websocket

import net.codinux.log.logger
import net.dankito.web.client.serialization.Serializer
import kotlin.reflect.KClass

abstract class WebSocketBase(
    protected val serializer: Serializer? = null,
) : WebSocket {

    protected abstract suspend fun doSendTextMessage(message: String)


    protected val onTextMessageHandlers = mutableListOf<suspend (String) -> Unit>() // TODO: make thread-safe

    protected val onBinaryMessageHandlers = mutableListOf<suspend (ByteArray) -> Unit>() // TODO: make thread-safe

    protected val onErrorHandlers = mutableListOf<suspend (Throwable?) -> Unit>() // TODO: make thread-safe

    protected val onCloseHandlers = mutableListOf<suspend (Int, String?) -> Unit>() // TODO: make thread-safe

    protected val log by logger()


    override suspend fun sendTextMessage(message: Any) {
        if (message !is String) {
            if (serializer == null) { // will never occur for KtorWebSocket and JavaHttpClientWebSocket
                log.error { "To send ${message::class} over WebSocket we need to serialize it to String, but no " +
                        "Serializer has been set." }
            } else {
                doSendTextMessage(serializer.serialize(message))
            }
        } else {
            doSendTextMessage(message)
        }
    }


    override fun onTextMessage(handler: suspend (String) -> Unit) {
        onTextMessageHandlers.add(handler)
    }

    protected open suspend fun handleTextMessage(message: String) {
        if (onTextMessageHandlers.isEmpty()) {
            log.warn { "Retrieved message but no onTextMessage handlers are registered" }
        } else {
            onTextMessageHandlers.toList().forEach { handler ->
                try {
                    handler(message)
                } catch (e: Throwable) {
                    log.error(e) { "$handler threw an error while handling text message: $message" }
                }
            }
        }
    }

    override fun <T : Any> onDeserializedTextMessage(typeClass: KClass<T>, genericType1: KClass<*>?, genericType2: KClass<*>?,
                                                     messageFilter: ((message: String) -> Boolean)?, serializer: Serializer?,
                                                     handler: suspend (T?, String, Throwable?) -> Unit) {
        val serializer = serializer ?: this.serializer
        if (serializer != null) {
            onTextMessage { message ->
                if (messageFilter == null || messageFilter(message)) {
                    try {
                        val deserialized = serializer.deserialize(message, typeClass, genericType1, genericType2)
                        handler(deserialized, message, null)
                    } catch (e: Throwable) {
                        log.error(e) { "Deserializing WebSocket message to $typeClass failed" }
                        handler(null, message, e)
                    }
                }
            }
        }
    }

    override fun <T : Any> onSuccessfullyDeserializedTextMessage(typeClass: KClass<T>, genericType1: KClass<*>?, genericType2: KClass<*>?,
                                                                 messageFilter: ((message: String) -> Boolean)?, serializer: Serializer?,
                                                                 handler: suspend (T) -> Unit) {
        val serializer = serializer ?: this.serializer
        if (serializer != null) {
            onTextMessage { message ->
                if (messageFilter == null || messageFilter(message)) {
                    try {
                        val deserialized = serializer.deserialize(message, typeClass, genericType1, genericType2)
                        handler(deserialized)
                    } catch (e: Throwable) {
                        log.error(e) { "Deserializing WebSocket message to $typeClass failed" }
                    }
                }
            }
        }
    }


    override fun onBinaryMessage(handler: suspend (message: ByteArray) -> Unit) {
        onBinaryMessageHandlers.add(handler)
    }

    protected open suspend fun handleBinaryMessage(message: ByteArray) {
        onBinaryMessageHandlers.toList().forEach { handler ->
            try {
                handler(message)
            } catch (e: Throwable) {
                log.error(e) { "$handler threw an error while handling binary message: $message" }
            }
        }
    }


    override fun onError(handler: suspend (error: Throwable?) -> Unit) {
        onErrorHandlers.add(handler)
    }

    protected open suspend fun invokeOnErrorHandlers(error: Throwable?) {
        onErrorHandlers.toList().forEach { handler ->
            handler(error)
        }
    }

    override fun onClose(handler: suspend (statusCode: Int, reason: String?) -> Unit) {
        onCloseHandlers.add(handler)
    }

    protected open suspend fun webSocketClosed(statusCode: Int, reason: String?) {
        invokeOnCloseHandlers(statusCode, reason)

        cleanUp()
    }

    /**
     * Do not call directly! Call [webSocketClosed] instead to also do necessary clean up.
     */
    protected open suspend fun invokeOnCloseHandlers(statusCode: Int, reason: String?) {
        onCloseHandlers.toList().forEach { handler ->
            handler(statusCode, reason)
        }
    }


    protected open fun cleanUp() {
        // remove all handlers to not leak memory
        onTextMessageHandlers.clear()
        onBinaryMessageHandlers.clear()
        onErrorHandlers.clear()
        onCloseHandlers.clear()
    }

    internal fun getSerializer() = serializer

}