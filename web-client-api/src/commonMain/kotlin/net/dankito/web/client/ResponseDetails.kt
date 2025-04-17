package net.dankito.web.client

import io.ktor.util.date.*

class ResponseDetails(
    val statusCode: Int,
    val reasonPhrase: String,

    // TODO: don't expose Ktor data types to the outside, make implementation agnostic
    val requestTime: GMTDate,
    val responseTime: GMTDate,

    val httpProtocolVersion: String,

    val headers: Map<String, List<String>>,
    val cookies: List<Cookie> = emptyList(),

    // parsed headers:
    val contentType: String? = null,
    val contentLength: Long? = null,
    val charset: String? = null,
) {
    override fun toString() = "$statusCode $reasonPhrase${if (contentType != null) " $contentType" else ""}"
}