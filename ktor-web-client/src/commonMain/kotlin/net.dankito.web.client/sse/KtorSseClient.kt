package net.dankito.web.client.sse

import io.ktor.client.*
import io.ktor.client.plugins.sse.*
import io.ktor.http.*
import kotlinx.coroutines.isActive
import net.codinux.log.logger
import kotlin.coroutines.coroutineContext

open class KtorSseClient(
    protected val client: HttpClient
) : SseClient {

    protected val log by logger()


    override suspend fun listenToSseEvents(url: String, receivedEvent: (ServerSentEvent) -> Unit) =
        listenToSseEventsSuspendable(url, receivedEvent)

    override suspend fun listenToSseEventsSuspendable(url: String, receivedEvent: suspend (ServerSentEvent) -> Unit) {
        try {
            client.sse(url) {
                while (coroutineContext.isActive) {
                    incoming.collect { event ->
                        val mapped = ServerSentEvent(event.data, event.event, event.id, event.retry, event.comments)
                        receivedEvent(mapped)
                    }
                }
            }
        } catch (e: Throwable) {
            log.error(e) { "Listening to SSE events stopped with an exception".decodeURLPart() }
        }

        if (coroutineContext.isActive) {
            log.info { "Listening to SSE events stopped, but as Coroutine is still active restarting listening ..." }
            listenToSseEventsSuspendable(url, receivedEvent)
        }
    }

}