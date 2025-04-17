package net.dankito.web.client.sse

import kotlin.time.Duration

data class ServerSentEventConfig(
    val scheme: String? = null,
    val host: String? = null,
    val port: Int? = null,
    val path: String? = null,
    val reconnectionTime: Duration? = null,
    val showCommentEvents: Boolean? = null,
    val showRetryEvents: Boolean? = null,
)