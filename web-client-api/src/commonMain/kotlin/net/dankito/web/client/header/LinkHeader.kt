package net.dankito.web.client.header

data class LinkHeader(
    val url: String,
    val parameters: Map<String, String>,
)