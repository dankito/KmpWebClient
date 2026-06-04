package net.dankito.web.client.header

import net.dankito.datetime.Instant
import net.dankito.web.client.Cookie

open class CookieHeaderParser(
    protected val httpDateParser: ((String) -> Long?)? = null,
) {

    open fun parseSetCookie(headerValue: String): Cookie? {
        val parts = headerValue.split(";").map { it.trim() }
        val nameValue = parts.firstOrNull()?.split("=", limit = 2) ?: return null
        if (nameValue.size < 2) return null

        val name = nameValue[0].trim()
        val value = nameValue[1].trim()

        var domain: String? = null
        var path: String? = null
        var expiresAt: Long? = null
        var secure = false
        var httpOnly = false

        parts.drop(1).forEach { part ->
            when {
                part.startsWith("domain=", ignoreCase = true) -> domain = part.substringAfter("=")
                part.startsWith("path=", ignoreCase = true) -> path = part.substringAfter("=")
                part.startsWith("expires=", ignoreCase = true) -> expiresAt = parseHttpDate(part.substringAfter("="))
                part.startsWith("max-age=", ignoreCase = true) -> expiresAt = Instant.now().toEpochMilliseconds() + (part.substringAfter("=").toLongOrNull() ?: 0) * 1000
                part.equals("secure", ignoreCase = true) -> secure = true
                part.equals("httponly", ignoreCase = true) -> httpOnly = true
            }
        }

        return Cookie(name, value, domain, path, expiresAt, secure, httpOnly)
    }

    open fun parseHttpDate(date: String): Long? = runCatching {
        httpDateParser?.invoke(date)
    }.getOrNull()

}