package net.dankito.web.client.header

import net.codinux.log.logger

open class LinkHeaderParser {

    companion object {
        val Instance = LinkHeaderParser()
    }

    protected val log by logger()


    fun parse(linkHeader: String): List<LinkHeader>? =
        try {
            linkHeader.split(',').map { header ->
                val url = parseUrl(header)
                val parameters = parseParameters(header)

                LinkHeader(url, parameters)
            }
        } catch (e: Throwable) {
            log.error(e) { "Could not parse Link header '$linkHeader'" }
            null
        }

    protected open fun parseUrl(header: String): String {
        var url = header.substringBefore(';').trim()

        if (url.startsWith('<')) { // should actually always be the case
            url = url.substring(1)
        }
        if (url.endsWith('>')) { // should actually always be the case
            url = url.substringBeforeLast('>')
        }

        return url
    }

    protected open fun parseParameters(header: String): Map<String, String> =
        header.substringAfter(';').split(';').associate { param ->
            val name = param.substringBefore('=').trim()

            var value = param.substringAfter('=').trim()
            if (value.startsWith('"') && value.endsWith('"')) {
                value = value.substring(1, value.length - 1)
            }

            name to value
        }
}