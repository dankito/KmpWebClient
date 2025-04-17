package net.dankito.web.client.sample

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import net.dankito.web.client.*
import net.dankito.web.client.auth.BasicAuthAuthentication

class KtorWebClientUsageExample {

    private val client: WebClient = KtorWebClient()


    fun configureClient() {
        val client: WebClient = KtorWebClient(
            // create your custom HttpClient engine instance if you like to. Otherwise
            // platform's default HttpClient instance will be created (see below)
            customClientCreator = { config, clientConfig -> HttpClient(clientConfig) },

            config = ClientConfig(
                // requests with relative URL will be prepended with this base URL
                baseUrl = "https://api.example.com",

                // set authorization for all requests executed with this client
                authentication = BasicAuthAuthentication("username", "password"),

                // if certificate errors should be ignored. Senseful e.g. for local testing with self signed certificates
                ignoreCertificateErrors = false,

                // to set additional configuration on HttpClient that is not supported out of the box by this library
                customClientConfig = { clientConfig, config ->
                    clientConfig.apply {
                        // example for custom client configuration: add XML serialization. By default JSON serialization is configured
                        install(ContentNegotiation) {
                            // xml()
                        }
                    }
                },

                // the UserAgent that should be used for all requests of this HTTP Client (if not overwritten in RequestParameters)
                defaultUserAgent = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Mobile Safari/537.3",

                // the ContentType HTTP header that should be used if request specifies a body but no content type (defaults to "application/json; charset=UTF-8")
                defaultContentType = "application/json; charset=UTF-8",

                // if response headers should be mapped and added to WebClientResponse -> ResponseDetails (defaults to true)
                mapResponseHeaders = true,

                // if response cookies should be mapped and added to WebClientResponse -> ResponseDetails (defaults to false)
                mapResponseCookies = false,

                // the connect timeout in milliseconds for all requests of this HTTP client (if not overwritten in RequestParameters). Defaults to 5 seconds.
                connectTimeoutMillis = 5_0000,
                // the socket timeout in milliseconds for all requests of this HTTP client (if not overwritten in RequestParameters). Defaults to not set.
                socketTimeoutMillis = null,
                // the request timeout in milliseconds for all requests of this HTTP client (if not overwritten in RequestParameters). Defaults to 15 seconds.
                requestTimeoutMillis = 15_000
            )
        )
    }


    suspend fun configureRequests() {
        val client: WebClient = KtorWebClient()

        // shortcuts if only URL (and request body) should be configured (also available for put(), delete(), head() and custom HTTP methods):
        val getResponse = client.get<User>("https://example.com") // automatically deserializes response body to User object
        val getResponseBody = getResponse.body // for other data available on WebClientResponse object see below

        val putResponseBody = client.post<String>("https://api.example.com/v1/user", User("John Doe")).body


        // detailed request configuration:
        val request = RequestParameters(
            url = "v2/user",
            responseClass = User::class,
            body = User("John Doe"),
            contentType = ContentTypes.JSON,
            accept = ContentTypes.JSON,

            headers = mapOf("Connection" to "keep-alive"),
            queryParameters = mapOf("enabled" to "true"),

            cookies = listOf(Cookie("cookie_name", "cookie_value")),
            userAgent = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Mobile Safari/537.3",

            connectTimeoutMillis = 5_000,
            socketTimeoutMillis = null,
            requestTimeoutMillis = 15_000,
        )

        val response = client.post(request)
    }


    suspend fun responseEvaluation() {
        val response = client.get<User>("https://api.example.com/v1/user/1")

        val isSuccessful: Boolean = response.successful
        val error: Throwable? = response.error

        // response body (in this case the mapped User object
        val responseBody: User? = response.body

        // evaluate response details
        val httpStatusCode: Int? = response.responseDetails?.statusCode // only set in case a response has been retrieved, not in case of Network error
        val reasonPhrase: String? = response.responseDetails?.reasonPhrase

        val contentType: String? = response.responseDetails?.contentType
        val contentLength: Long? = response.responseDetails?.contentLength

        val requestTime = response.responseDetails?.requestTime
        val responseTime = response.responseDetails?.responseTime

        val headers = response.responseDetails?.headers
        // only set if ClientConfig.mapResponseCookies is set to true
        val cookies = response.responseDetails?.cookies
    }


    class User(val username: String)

}