package net.dankito.web.client

import io.ktor.client.network.sockets.*
import io.ktor.utils.io.errors.*

open class WebClientException(
    open val httpStatusCode: Int,
    errorMessage: String,
    cause: Throwable? = null
) : Exception(errorMessage, cause) {

    val isConnectTimeout = cause is ConnectTimeoutException

    val isSocketTimeout = cause is SocketTimeoutException

    val isIOException = cause is IOException

    val isNetworkError = isConnectTimeout || isSocketTimeout || isIOException

    val isClientError = httpStatusCode in 400..499

    val isServerError = httpStatusCode in 500..599


    override fun toString(): String {
        return "$httpStatusCode $message. isNetworkError = $isNetworkError, isClientError = $isClientError, isServerError = $isServerError"
    }

}