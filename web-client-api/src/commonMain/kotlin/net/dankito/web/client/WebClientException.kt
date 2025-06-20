package net.dankito.web.client

open class WebClientException(
    errorMessage: String?,
    cause: Throwable? = null,
    open val responseDetails: ResponseDetails? = null,
    open val responseBody: String? = null
) : Exception(errorMessage, cause) {

    val httpStatusCode = responseDetails?.statusCode ?: -1

    val isClientError = httpStatusCode in 400..499

    val isServerError = httpStatusCode in 500..599


    override fun toString(): String {
        return "$httpStatusCode: $message. isClientError = $isClientError, isServerError = $isServerError"
    }

}