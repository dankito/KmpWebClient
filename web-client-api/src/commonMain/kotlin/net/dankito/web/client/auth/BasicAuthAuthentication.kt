package net.dankito.web.client.auth

open class BasicAuthAuthentication(
    val username: String,
    val password: String,
) : Authentication {
    override fun toString() = "Basic Auth authentication for user $username with password ${password.substring(0, 2).padEnd(password.length - 2, '*')}"
}