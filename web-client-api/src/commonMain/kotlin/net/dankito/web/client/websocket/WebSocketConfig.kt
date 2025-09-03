package net.dankito.web.client.websocket

import net.dankito.web.client.Cookie
import net.dankito.web.client.RequestParameters.Companion.DefaultUserAgent
import net.dankito.web.client.auth.Authentication

open class WebSocketConfig(
    open val url: String,

    open val authentication: Authentication? = null,

    open val queryParameters: Map<String, Any> = mapOf(),

    open val headers: Map<String, String> = mutableMapOf(),
    open val userAgent: String? = DefaultUserAgent,

    open val cookies: List<Cookie> = emptyList(),
)