package net.dankito.web.client

interface WebClient {

    suspend fun headAsync(parameters: RequestParameters<Unit>): WebClientResponse<Unit>

    suspend fun <T : Any> getAsync(parameters: RequestParameters<T>): WebClientResponse<T>

    suspend fun <T : Any> postAsync(parameters: RequestParameters<T>): WebClientResponse<T>

    suspend fun <T : Any> putAsync(parameters: RequestParameters<T>): WebClientResponse<T>

    suspend fun <T : Any> deleteAsync(parameters: RequestParameters<T>): WebClientResponse<T>

}


suspend fun WebClient.headAsync(url: String) = headAsync(RequestParameters(url, Unit::class))

suspend inline fun <reified T : Any> WebClient.getAsync(url: String) = getAsync(RequestParameters(url, T::class))

suspend inline fun <reified T : Any> WebClient.postAsync(url: String, body: String, contentType: String = RequestParameters.DefaultContentType) =
    postAsync(RequestParameters(url, T::class, body, contentType))

suspend inline fun <reified T : Any> WebClient.putAsync(url: String, body: String, contentType: String = RequestParameters.DefaultContentType) =
    putAsync(RequestParameters(url, T::class, body, contentType))

suspend inline fun <reified T : Any> WebClient.deleteAsync(url: String) = deleteAsync(RequestParameters(url, T::class))