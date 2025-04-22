package net.dankito.web.client.sse

interface SseConnection {

    val isOpen: Boolean

    fun close()

}