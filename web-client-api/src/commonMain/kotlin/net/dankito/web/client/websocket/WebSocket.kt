package net.dankito.web.client.websocket

import net.dankito.web.client.serialization.Serializer
import kotlin.reflect.KClass

interface WebSocket {

    fun onTextMessage(handler: (message: String) -> Unit)

    /**
     * `handler` will be called in both cases, if deserialization succeeds or fails.
     * In first case `T` is set. In latter `error`.
     *
     * In most cases WebClient implementations have already a [Serializer] set. So you only need to set it here
     * if it's not set on WebClient or if you need special deserialization logic for the WebSocket message.
     */
    fun <T : Any> onDeserializedTextMessage(typeClass: KClass<T>, genericType1: KClass<*>? = null, genericType2: KClass<*>? = null,
                                            serializer: Serializer? = null, handler: (T?, originalMessage: String, error: Throwable?) -> Unit)

    /**
     * `handler` will only be called if deserialization succeeds.
     * Errors will be logged, so check your locks.
     *
     * In most cases WebClient implementations have already a [Serializer] set. So you only need to set it here
     * if it's not set on WebClient or if you need special deserialization logic for the WebSocket message.
     */
    fun <T : Any> onSuccessfullyDeserializedTextMessage(typeClass: KClass<T>, genericType1: KClass<*>? = null, genericType2: KClass<*>? = null,
                                            serializer: Serializer? = null, handler: (T) -> Unit)

    fun onBinaryMessage(handler: (message: ByteArray) -> Unit)

    fun onError(handler: (error: Throwable?) -> Unit)

    fun onClose(handler: (statusCode: Int, reason: String?) -> Unit)


    suspend fun close(code: Int = 1001, reason: String? = null)

}