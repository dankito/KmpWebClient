package net.dankito.web.client.websocket

interface WebSocket {

    fun onTextMessage(handler: (message: String) -> Unit)

    fun onError(handler: (error: Throwable?) -> Unit)

    fun onClose(handler: (statusCode: Int, reason: String?) -> Unit)


    fun close(reason: String? = null)

}