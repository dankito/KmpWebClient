package net.dankito.web.client

open class Cookie(
    val name: String,
    val value: String,
    val domain: String? = null,
    val path: String? = null,
    val expiresAt: Long? = null,
    val secure: Boolean = false,
    val httpOnly: Boolean = false,
    val persistent: Boolean = false,
    val hostOnly: Boolean = false
) {

    override fun toString(): String {
        return "$name: $value"
    }

}