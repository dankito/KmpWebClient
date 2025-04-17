package net.dankito.web.client

class ResponseDetails(
    val statusCode: Int,
    val reasonPhrase: String,

    // TODO: map to platform independent Instance objects
    val requestTime: String,
    val responseTime: String,

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