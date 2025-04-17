package net.dankito.web.client.extensions

import io.ktor.http.*

// provide public API that hides internal implementation with Ktor so that
// calling libraries don't have to include Ktor themselves

fun String.decodeUrlPart(start: Int = 0, end: Int = length) =
    this.decodeURLPart(start, end)

fun String.encodeUrlPath(encodeSlash: Boolean = false, encodeEncoded: Boolean = true) =
    this.encodeURLPath(encodeSlash, encodeEncoded)