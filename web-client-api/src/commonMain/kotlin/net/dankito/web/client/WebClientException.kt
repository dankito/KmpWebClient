package net.dankito.web.client

open class WebClientException(
    open val httpStatusCode: Int,
    errorMessage: String
) : Exception(errorMessage) {

    override fun toString(): String {
        return "$httpStatusCode $message"
    }

}