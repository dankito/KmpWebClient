package net.dankito.web.client

open class WebClientResponse<T>(
    open val successful: Boolean,
    open val requestedUrl: String,
    open val responseDetails: ResponseDetails? = null,
    open val error: Throwable? = null,
    open val body: T? = null
) {

    val statusCode = responseDetails?.statusCode ?: -1


    override fun toString(): String {
        return if (successful) {
            "Successful: $statusCode $body"
        } else {
            "Error: $statusCode $error"
        }
    }

}