package net.dankito.web.client.websocket

import net.codinux.log.logger
import net.dankito.web.client.serialization.Serializer
import kotlin.reflect.KClass

abstract class WebSocketBase(
    protected val serializer: Serializer? = null,
) : WebSocket {


    protected val onTextMessageHandlers = mutableListOf<(String) -> Unit>() // TODO: make thread-safe

    protected val onBinaryMessageHandlers = mutableListOf<(ByteArray) -> Unit>() // TODO: make thread-safe

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
                    log.error(e) { "$handler threw an error while handling text message: $message" }
                }
            }
        }
    }

    override fun <T : Any> onDeserializedTextMessage(typeClass: KClass<T>, genericType1: KClass<*>?, genericType2: KClass<*>?,
                                                     serializer: Serializer?, handler: (T?, String, Throwable?) -> Unit) {
        val serializer = serializer ?: this.serializer
        if (serializer != null) {
            onTextMessage { message ->
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

    override fun <T : Any> onSuccessfullyDeserializedTextMessage(typeClass: KClass<T>, genericType1: KClass<*>?, genericType2: KClass<*>?,
                                                     serializer: Serializer?, handler: (T) -> Unit) {
        val serializer = serializer ?: this.serializer
        if (serializer != null) {
            onTextMessage { message ->
                try {
                    val deserialized = serializer.deserialize(message, typeClass, genericType1, genericType2)
                    handler(deserialized)
                } catch (e: Throwable) {
                    log.error(e) { "Deserializing WebSocket message to $typeClass failed" }
                }
            }
        }
    }


    override fun onBinaryMessage(handler: (message: ByteArray) -> Unit) {
        onBinaryMessageHandlers.add(handler)
    }

    protected open fun handleBinaryMessage(message: ByteArray) {
        onBinaryMessageHandlers.toList().forEach { handler ->
            try {
                handler(message)
            } catch (e: Throwable) {
                log.error(e) { "$handler threw an error while handling binary message: $message" }
            }
        }
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