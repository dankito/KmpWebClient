# Kotlin (Multiplatform) Web Client
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.dankito.web/ktor2-web-client/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.dankito.web/ktor2-web-client)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

In so many projects I need a web client. 

Each time anew I had to add the platform specific Ktor engine dependencies, 
configure Content Negotiation, Authorization, request und response body (de-)serialization, 
setting timeouts, headers, cookies, user agent, base URL, query parameters, certificate handling, ... 

So I extracted all these to a common library with easy configuration of HTTP client, requests and response handling.
So now I need to add only one dependency which ships all the platform specific engines already
and I am ready to go to implement the domain specific code:


## Setup

### Gradle

```
implementation("net.dankito.web:ktor-web-client:1.5.1")
```

### Maven

```xml
<dependency>
   <groupId>net.dankito.web</groupId>
   <artifactId>ktor2-web-client-jvm</artifactId>
   <version>1.5.1</version>
</dependency>
```

Or if you only want to have the API (see [Usage](#usage)) and implement the HTTP client by yourself, simply replace `ktor-web-client` with `web-client-api`.


## Usage

### HTTP Client creation and configuration

The only currently implemented implementation of `WebClient` is `KtorWebClient`:

```kotlin
val client: WebClient = KtorWebClient()
```

A lot of default settings can be configured on `KtorWebClient` that automatically get applied to all requests executed with this client (if not overwritten by `RequestParameter` on request level):

```kotlin
val client = KtorWebClient(
    // create your custom HttpClient engine instance if you like to. Otherwise 
    // platform's default HttpClient instance will be created (see below)
    customClientCreator = { config, clientConfig -> HttpClient(clientConfig) },
    
    config = ClientConfig(
        // requests with relative URL will be prepended with this base URL
        baseUrl = "https://api.example.com/v1/",

        // set authorization for all requests executed with this client
        authentication = BasicAuthAuthentication("username", "password"),

        // if certificate errors should be ignored. Senseful e.g. for local testing with self signed certificates
        ignoreCertificateErrors = false,

        // to set additional configuration on HttpClient that is not supported out of the box by this library
        customClientConfig = { clientConfig, config ->
            clientConfig.apply {
                // example for custom client configuration: add XML serialization. By default JSON serialization is configured
                install(ContentNegotiation) {
                    xml()
                }
            }
        },

        // the UserAgent that should be used for all requests of this HTTP Client (if not overwritten in RequestParameters)
        defaultUserAgent = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Mobile Safari/537.3",
        
        // the ContentType HTTP header that should be used if request specifies a body but no content type (defaults to "application/json; charset=UTF-8")
        defaultContentType = "application/json; charset=UTF-8",
        
        // the connect timeout in milliseconds for all requests of this HTTP client (if not overwritten in RequestParameters). Defaults to 5 seconds.
        connectTimeoutMillis = 5_0000,
        // the socket timeout in milliseconds for all requests of this HTTP client (if not overwritten in RequestParameters). Defaults to not set.
        socketTimeoutMillis = null,
        // the request timeout in milliseconds for all requests of this HTTP client (if not overwritten in RequestParameters). Defaults to 15 seconds.
        requestTimeoutMillis = 15_000
    )
)
```


### Request configuration

For all common HTTP methods there are shortcuts where only URL and optionally request body and content type have to be specified:

```kotlin
val client: WebClient = KtorWebClient()

// shortcuts if only URL (and request body) should be configured (also available for put(), delete(), head() and custom HTTP methods):
val getResponse = client.get<User>("https://example.com") // automatically deserializes response body to User object
val getResponseBody = getResponse.body // for other data available on WebClientResponse object see below

val putResponseBody = client.post<String>("https://api.example.com/v1/user", User("John Doe")).body
```

For the full set of request options configuration create a `RequestParameters` object and pass it to `get()`, `post()`, `put()`, ...:

```kotlin
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
```


### Evaluating response

A lot of details of the response get automatically mapped:

```kotlin
val response = client.get<User>("https://api.example.com/v1/user/1")

val isSuccessful: Boolean = response.successful
val error: Throwable? = response.error

// response body (in this case the mapped User object)
val responseBody: User? = response.body

// evaluate response details
val httpStatusCode: Int? = response.responseDetails?.statusCode // only set in case a response has been retrieved, not in case of Network error
val reasonPhrase: String? = response.responseDetails?.reasonPhrase

val contentType: String? = response.responseDetails?.contentType
val contentLength: Long? = response.responseDetails?.contentLength

val requestTime = response.responseDetails?.requestTime
val responseTime = response.responseDetails?.responseTime

val headers = response.responseDetails?.headers
val cookies = response.responseDetails?.cookies
```


## Platform specific client engines

If not specified otherwise depending on platform these client engines are used:

| Platform     | Default Engine                    | Remark                                                                           |
|--------------|-----------------------------------|----------------------------------------------------------------------------------|
| JVM          | CIO                               | Why not OkHttp? To not interfere with other OkHttp dependencies on the classpath |
| Android      | CIO                               | Why not OkHttp? To not interfere with other OkHttp dependencies on the classpath |
| Apple system | NSURLSession (ktor-client-darwin) |                                                                                  |
| Linux        | cURL                              |                                                                                  |
| Windows      | WinHttp                           |                                                                                  |
| JS/Browser   | fetch API (ktor-client-js)        |                                                                                  |
| JS/Node      | node-fetch (ktor-client-js)       |                                                                                  |


## License
```
Copyright 2023 dankito

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```