package net.dankito.web.client.sse

interface SseClient {


    /**
     * Listens to Server-sent events.
     *
     * @param url Relative or absolute URL
     */
    suspend fun listenToSseEvents(url: String, receivedEvent: (ServerSentEvent) -> Unit): SseConnection

    /**
     * Listens to Server-sent events.
     *
     * The same as [listenToSseEvents], but the [receivedEvent] callback supports suspend functions.
     *
     * @param url Relative or absolute URL
     */
    suspend fun listenToSseEventsSuspendable(url: String, receivedEvent: suspend (ServerSentEvent) -> Unit): SseConnection

}