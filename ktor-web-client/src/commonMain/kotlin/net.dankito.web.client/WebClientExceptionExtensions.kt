package net.dankito.web.client

import io.ktor.client.network.sockets.*
import kotlinx.io.IOException


val WebClientException.isConnectTimeout: Boolean
    get() = this.cause is ConnectTimeoutException

val WebClientException.isSocketTimeout: Boolean
    get() = this.cause is SocketTimeoutException

val WebClientException.isIOException: Boolean
    get() = this.cause is IOException

val WebClientException.isNetworkError: Boolean
    get() = isConnectTimeout || isSocketTimeout || isIOException