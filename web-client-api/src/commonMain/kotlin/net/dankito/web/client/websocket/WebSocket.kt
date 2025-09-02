package net.dankito.web.client.websocket

interface WebSocket {

    fun onTextMessage(handler: (message: String) -> Unit)

    fun onBinaryMessage(handler: (message: ByteArray) -> Unit)

    fun onError(handler: (error: Throwable?) -> Unit)

    fun onClose(handler: (statusCode: Int, reason: String?) -> Unit)


    suspend fun close(code: Int = 1001, reason: String? = null)

}