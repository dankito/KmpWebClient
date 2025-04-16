package net.dankito.web.client

interface WebClient {

    suspend fun head(parameters: RequestParameters<Unit>): WebClientResponse<Unit>

    suspend fun <T : Any> get(parameters: RequestParameters<T>): WebClientResponse<T>

    suspend fun <T : Any> post(parameters: RequestParameters<T>): WebClientResponse<T>

    suspend fun <T : Any> put(parameters: RequestParameters<T>): WebClientResponse<T>

    suspend fun <T : Any> delete(parameters: RequestParameters<T>): WebClientResponse<T>

}


suspend fun WebClient.head(url: String) = head(RequestParameters(url, Unit::class))

suspend inline fun <reified T : Any> WebClient.get(url: String) = this@get.get(RequestParameters(url, T::class))

suspend inline fun <reified T : Any> WebClient.post(url: String, body: String, contentType: String = RequestParameters.DefaultContentType) =
    post(RequestParameters(url, T::class, body, contentType))

suspend inline fun <reified T : Any> WebClient.put(url: String, body: String, contentType: String = RequestParameters.DefaultContentType) =
    put(RequestParameters(url, T::class, body, contentType))

suspend inline fun <reified T : Any> WebClient.delete(url: String) = delete(RequestParameters(url, T::class))