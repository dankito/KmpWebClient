package net.dankito.web.client

import net.dankito.datetime.Instant
import java.net.http.HttpResponse

open class JavaHttpClientResponseDetails(
    method: String,
    parameters: RequestParameters<*>,
    requestTime: Instant,
    val response: HttpResponse<*>,
    statusCode: Int,
    reasonPhrase: String,
) : ResponseDetails(method, parameters,
    statusCode, reasonPhrase, requestTime, Instant.now(), response.version().toString(),
    response.headers().map(), emptyList(), // TODO: map cookies
    response.headers().firstValue("Content-Type").orElse(null), response.headers().firstValue("Content-Length").orElse(null)?.toLongOrNull(), // TODO: extract Charset from Content-Type
) {
}