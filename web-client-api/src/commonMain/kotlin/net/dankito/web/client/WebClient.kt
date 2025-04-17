package net.dankito.web.client

interface WebClient {

    suspend fun head(parameters: RequestParameters<Unit>): WebClientResponse<Unit>

    suspend fun <T : Any> get(parameters: RequestParameters<T>): WebClientResponse<T>

    suspend fun <T : Any> post(parameters: RequestParameters<T>): WebClientResponse<T>

    suspend fun <T : Any> put(parameters: RequestParameters<T>): WebClientResponse<T>

    suspend fun <T : Any> delete(parameters: RequestParameters<T>): WebClientResponse<T>

    /**
     * To support custom HTTP methods like PROPFIND and REPORT (WebDAV).
     */
    suspend fun <T : Any> custom(httpMethod: String, parameters: RequestParameters<T>): WebClientResponse<T>

}


suspend fun WebClient.head(url: String) = head(RequestParameters(url, Unit::class))

suspend inline fun <reified T : Any> WebClient.get(url: String) = get(RequestParameters(url, T::class))

suspend inline fun <reified T : Any> WebClient.post(url: String, body: Any, contentType: String? = null) =
    post(RequestParameters(url, T::class, body, contentType))

suspend inline fun <reified T : Any> WebClient.put(url: String, body: Any, contentType: String? = null) =
    put(RequestParameters(url, T::class, body, contentType))

suspend inline fun <reified T : Any> WebClient.delete(url: String) = delete(RequestParameters(url, T::class))

suspend inline fun <reified T : Any> WebClient.custom(httpMethod: String, url: String, body: Any? = null, contentType: String? = null) =
    custom(httpMethod, RequestParameters(url, T::class, body, contentType))