package net.dankito.web.client.auth

open class BearerAuthentication(
    val bearerToken: String,
) : Authentication {
    override fun toString() = "Bearer authentication with token $bearerToken"
}