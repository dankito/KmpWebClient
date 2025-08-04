package net.dankito.web.client

import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.charsets.*
import net.dankito.datetime.Instant
import net.dankito.web.client.util.WebDateTimeUtil

open class KtorResponseDetails(
    method: String,
    parameters: RequestParameters<*>,
    open val response: HttpResponse,
) : ResponseDetails(method, parameters, response.status.value, response.status.description) {

    override val requestTime: Instant? by lazy { WebDateTimeUtil.gmtDateToInstant(response.requestTime) }

    override val responseTime: Instant by lazy { WebDateTimeUtil.gmtDateToInstant(response.responseTime) }

    override val httpProtocolVersion: String? by lazy {
        response.version.let { if (it.name == "HTTP") "${it.major}.${it.minor}" else it.toString() }
    }


    override val headers: Map<String, List<String>> by lazy { response.headers.toMap() }

    override val cookies: List<Cookie> by lazy { response.setCookie().map { mapCookie(it) } }


    override val contentType: String? by lazy { response.contentType()?.withoutParameters()?.toString() }

    override val contentLength: Long? by lazy { response.contentLength() }

    override val charset: String? by lazy { response.charset()?.name }


    open suspend fun responseBodyAsText(fallbackCharset: Charset = Charsets.UTF_8): String =
        response.bodyAsText(fallbackCharset)


    protected open fun mapCookie(cookie: io.ktor.http.Cookie) = Cookie(
        cookie.name,
        cookie.value,
        cookie.domain,
        cookie.path,
        cookie.expires?.timestamp,
        cookie.secure,
        cookie.httpOnly
    )

}