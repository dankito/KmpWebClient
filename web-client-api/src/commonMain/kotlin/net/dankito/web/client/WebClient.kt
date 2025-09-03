package net.dankito.web.client

import net.dankito.web.client.auth.Authentication
import net.dankito.web.client.websocket.WebSocket
import net.dankito.web.client.websocket.WebSocketConfig

interface WebClient {

    suspend fun head(parameters: RequestParameters<Unit>): WebClientResult<Unit>

    suspend fun <T : Any> get(parameters: RequestParameters<T>): WebClientResult<T>

    suspend fun <T : Any> post(parameters: RequestParameters<T>): WebClientResult<T>

    suspend fun <T : Any> put(parameters: RequestParameters<T>): WebClientResult<T>

    suspend fun <T : Any> delete(parameters: RequestParameters<T>): WebClientResult<T>

    /**
     * To support custom HTTP methods like PROPFIND and REPORT (WebDAV).
     */
    suspend fun <T : Any> custom(httpMethod: String, parameters: RequestParameters<T>): WebClientResult<T>


    fun webSocket(url: String, authentication: Authentication? = null): WebSocket =
        webSocket(WebSocketConfig(url, authentication))

    fun webSocket(config: WebSocketConfig): WebSocket

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