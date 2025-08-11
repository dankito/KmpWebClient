package net.dankito.web.client

import net.codinux.log.Log

open class WebClientResult<T>( // TODO: rename to Response or HttpResponse?
    /**
     * The URL that the web client has requested which may be a combination of baseUrl set on WebClient and relative
     * URL used for request.
     *
     * In case of network error may only the (relative) URL used for request, not the URL the web client really has
     * requested.
     */
    open val requestedUrl: String,

    /**
     * Is true if
     * - a response has been received,
     * - the HTTP status code is in range 200..299 (= [ResponseDetails.isSuccessResponse] is true),
     * - the response body could be deserialized.
     *
     * So if there's an error during request body deserialization, `successful` is `false` even though
     * [ResponseDetails.isSuccessResponse] is `true`.
     */
    open val successful: Boolean,

    /**
     * In case a response has been retrieved, details of the response like headers, cookies, ...
     */
    open val responseDetails: ResponseDetails? = null, // TODO: rename to details

    open val errorType: ClientErrorType? = null,

    /**
     * In case an error has occurred, details about the error.
     */
    open val error: WebClientException? = null,

    /**
     * The retrieved (and may deserialized) response body, if any.
     */
    open val body: T? = null
) {

    /**
     * The HTTP status code of the retrieved response.
     *
     * Set to `-1` in case no response has been retrieved.
     */
    open val statusCode = responseDetails?.statusCode ?: -1

    open val successfulAndBodySet: Boolean = successful && body != null

    // made function inline so that also suspendable function can be called in mapper lambda
    inline fun <R> mapResponseBodyIfSuccessful(mapper: (T) -> R): WebClientResult<R> =
        @Suppress("UNCHECKED_CAST")
        if (successful && body != null) {
            try {
                copyWithBody(mapper(body!!))
            } catch (e: Throwable) {
                Log.error(e) { "Could not map response body: $body." }
                WebClientResult(this.requestedUrl, false, this.responseDetails, ClientErrorType.MappingError,
                    WebClientException("Response body '$body' could not be mapped", e, this.responseDetails))
            }
        } else {
            this as WebClientResult<R>
        }

    // made function inline so that also suspendable function can be called in mapper lambda
    inline fun <R> mapResponseBodyIfSuccessful(mapper: (WebClientResult<T>, T) -> R): WebClientResult<R> =
        if (successful && body != null) {
            try {
                copyWithBody(mapper(this, body!!))
            } catch (e: Throwable) {
                Log.error(e) { "Could not map response body: $body." }
                WebClientResult(this.requestedUrl, false, this.responseDetails, ClientErrorType.MappingError,
                    WebClientException("Response body '$body' could not be mapped", e, this.responseDetails))
            }
        } else {
            @Suppress("UNCHECKED_CAST")
            this as WebClientResult<R>
        }

    // TODO: add method for error case


    open fun <K> copyWithBody(body: K) =
        WebClientResult(this.requestedUrl, this.successful, this.responseDetails, this.errorType, this.error, body)


    override fun toString(): String {
        return if (successful) {
            "Successful: $statusCode $body"
        } else if (error != null) {
            "Error: $error" // WebClientException already prints the HTTP status code
        } else if (responseDetails != null) {
            "Error $errorType $statusCode ${responseDetails?.reasonPhrase}: $body"
        } else {
            "Error $errorType: $body"
        }
    }

}