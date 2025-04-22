package net.dankito.web.client.sse

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

open class KtorSseConnection(
    protected val scope: CoroutineScope,
    protected val job: Job
) : SseConnection {

    override val isOpen: Boolean
        get() = job.isActive

    override fun close() {
        job.cancel(CancellationException("SSE connection closed by client"))
    }

}