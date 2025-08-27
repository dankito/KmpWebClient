package net.dankito.web.client

object ContentTypes {

    const val Any = "*/*"
    /**
     * Synonym for [Any].
     */
    const val Wildcard = Any

    const val JSON = "application/json; charset=UTF-8"
    const val XML = "application/xml; charset=UTF-8"

    const val PLAIN_TEXT = "text/plain; charset=UTF-8"
    const val HTML = "text/html; charset=UTF-8"
    const val FORM_URL_ENCODED = "application/x-www-form-urlencoded; charset=UTF-8"
    const val MULTIPART = "multipart/form-data; charset=UTF-8"
    const val CSS = "text/css; charset=UTF-8"
    const val JAVASCRIPT = "application/javascript; charset=UTF-8"

    const val OCTET_STREAM = "application/octet-stream"
    const val PDF = "application/pdf"

    const val PNG = "image/png"
    const val JPEG = "image/jpeg"
    const val GIF = "image/gif"
    const val WEBP = "image/webp"
    const val ICO = "image/x-icon"
    const val SVG = "image/svg+xml"

}