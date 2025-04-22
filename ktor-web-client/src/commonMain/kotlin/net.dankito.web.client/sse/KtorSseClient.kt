package net.dankito.web.client.sse

import io.ktor.client.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import net.codinux.log.logger

open class KtorSseClient(
    protected val client: HttpClient,
    protected val logStatusInformation: Boolean = false
) : SseClient {

    protected val log by logger()


    override suspend fun listenToSseEvents(url: String, receivedEvent: (ServerSentEvent) -> Unit) =
        listenToSseEventsSuspendable(url, receivedEvent)

    override suspend fun listenToSseEventsSuspendable(url: String, receivedEvent: suspend (ServerSentEvent) -> Unit): SseConnection {
        val job = SupervisorJob()
        val scope = CoroutineScope(Dispatchers.Default + job)

        scope.launch {
            listenToSseEventsRetryable(url, scope, null, receivedEvent)
        }

        if (logStatusInformation) {
            log.info { "Started listening to server-sent events at $url" }
        } else {
            log.debug { "Started listening to server-sent events at $url" }
        }

        return KtorSseConnection(scope, job)
    }

    protected open suspend fun listenToSseEventsRetryable(url: String, scope: CoroutineScope, lastReceivedEventId: String? = null, receivedEvent: suspend (ServerSentEvent) -> Unit) {
        var lastEventId: String? = null

        try {
            client.sse(url, { createSseRequest(url, lastReceivedEventId)}) {
                while (scope.isActive) {
                    incoming.collect { event ->
                        try {
                            event.id?.takeUnless { it.isBlank() }?.let { lastEventId = it }

                            val mapped = ServerSentEvent(event.data, event.event, event.id, event.retry, event.comments)
                            receivedEvent(mapped)
                        } catch (e: Throwable) {
                            log.error(e) { "Handling received SSE event failed: $event" }
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            if (e is CancellationException) { // client tells to shut down SSE listening
                return
            }

            if (logStatusInformation) {
                log.error(e) { "Listening to SSE events stopped with an exception".decodeURLPart() }
            } else {
                log.debug(e) { "Listening to SSE events stopped with an exception".decodeURLPart() }
            }
        }

        if (scope.isActive) {
            if (logStatusInformation) {
                log.info { "Listening to SSE events stopped, but as Coroutine is still active restarting listening with lastEventId $lastEventId ..." }
            } else {
                log.debug { "Listening to SSE events stopped, but as Coroutine is still active restarting listening with lastEventId $lastEventId ..." }
            }

            listenToSseEventsRetryable(url, scope, lastEventId, receivedEvent)
        }
    }

    protected open fun createSseRequest(urlString: String, lastReceivedEventId: String? = null) = HttpRequestBuilder().apply {
        lastReceivedEventId?.let {
            header("Last-Event-ID", it)
        }
    }

}