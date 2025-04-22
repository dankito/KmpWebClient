package net.dankito.web.client.sse

import io.ktor.client.*
import io.ktor.client.plugins.sse.*
import io.ktor.http.*
import kotlinx.coroutines.*
import net.codinux.log.logger

open class KtorSseClient(
    protected val client: HttpClient
) : SseClient {

    protected val log by logger()


    override suspend fun listenToSseEvents(url: String, receivedEvent: (ServerSentEvent) -> Unit) =
        listenToSseEventsSuspendable(url, receivedEvent)

    override suspend fun listenToSseEventsSuspendable(url: String, receivedEvent: suspend (ServerSentEvent) -> Unit): SseConnection {
        val job = SupervisorJob()
        val scope = CoroutineScope(Dispatchers.Default + job)

        scope.launch {
            listenToSseEventsRetryable(url, scope, receivedEvent)
        }

        log.info { "Started listening to server-sent events at $url" }

        return KtorSseConnection(scope, job)
    }

    protected open suspend fun listenToSseEventsRetryable(url: String, scope: CoroutineScope, receivedEvent: suspend (ServerSentEvent) -> Unit) {
        try {
            client.sse(url) {
                while (scope.isActive) {
                    incoming.collect { event ->
                        val mapped = ServerSentEvent(event.data, event.event, event.id, event.retry, event.comments)
                        receivedEvent(mapped)
                    }
                }
            }
        } catch (e: Throwable) {
            if (e is CancellationException) { // client tells to shut down SSE listening
                return
            }

            log.error(e) { "Listening to SSE events stopped with an exception".decodeURLPart() }
        }

        if (scope.isActive) {
            log.info { "Listening to SSE events stopped, but as Coroutine is still active restarting listening ..." }
            listenToSseEventsSuspendable(url, receivedEvent)
        }
    }

}