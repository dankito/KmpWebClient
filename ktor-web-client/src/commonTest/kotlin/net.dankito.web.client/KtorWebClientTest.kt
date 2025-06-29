package net.dankito.web.client

import assertk.assertThat
import assertk.assertions.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class KtorWebClientTest {

    companion object {
        private val Url = "https://staging.dankito.net/echo/KtorWebClientTest"

        private const val Body = "Just a test, no animals have been harmed"
    }


    private val underTest = KtorWebClient(ignoreCertificateErrors = true)


    @Test
    fun get() = runTest {
        val response = underTest.get<String>(Url)

        assertSuccessGetOrDeleteResponse(response)
    }

    @Test
    fun getWithParameters() = runTest {
        val response = underTest.get(RequestParameters(Url, String::class))

        assertSuccessGetOrDeleteResponse(response)
    }


    @Test
    fun head() = runTest {
        val response = underTest.head(Url)

        assertNoContentResponse(response)
    }

    @Test
    fun headWithParameters() = runTest {
        val response = underTest.head(RequestParameters(Url, Unit::class))

        assertNoContentResponse(response)
    }


    @Test
    fun post() = runTest {
        val response = underTest.post<String>(Url, Body)

        assertSuccessResponseWithBody(response)
    }

    @Test
    fun postWithParameters() = runTest {
        val response = underTest.post(RequestParameters(Url, String::class, Body))

        assertSuccessResponseWithBody(response)
    }


    @Test
    fun put() = runTest {
        val response = underTest.put<String>(Url, Body)

        assertSuccessResponseWithBody(response)
    }

    @Test
    fun putWithParameters() = runTest {
        val response = underTest.put(RequestParameters(Url, String::class, Body))

        assertSuccessResponseWithBody(response)
    }


    @Test
    fun delete() = runTest {
        val response = underTest.delete<String>(Url)

        assertSuccessGetOrDeleteResponse(response)
    }

    @Test
    fun deleteWithParameters() = runTest {
        val response = underTest.delete(RequestParameters(Url, String::class))

        assertSuccessGetOrDeleteResponse(response)
    }


    @Test
    fun timeout() = runTest {
        val response = underTest.get(RequestParameters(Url, String::class, requestTimeoutMillis = 1))

        assertRequestFailed(response, ClientErrorType.Timeout)
    }

    @Test
    fun notFound() = runTest {
        val response = underTest.get<Unit>("http://localhost:65535")

        assertRequestFailed(response, ClientErrorType.NetworkError)
    }


    @Test
    fun testLazyResponseDetailValues() = runTest {
        val response = underTest.post(RequestParameters(Url, String::class, Body, "text/plain; charset=UTF-8"))

        assertThat(response.successful).isTrue()
        assertThat(response.responseDetails).isNotNull()

        val details = response.responseDetails!!
        assertThat(details.contentType).isEqualTo("text/plain")
        assertThat(details.contentLength).isNotNull().isEqualTo(40)
        assertThat(details.charset).isEqualTo("UTF-8")

        assertThat(details.headers).isNotEmpty()
        assertThat(details.cookies).isEmpty()

        assertThat(details.requestTime).isNotNull()
        assertThat(details.responseTime.epochSeconds).isGreaterThan(1_745_000_000)
        assertThat(details.responseTime.toEpochMilliseconds()).isGreaterThanOrEqualTo(details.requestTime!!.toEpochMilliseconds())
        assertThat(details.httpProtocolVersion).isNotNull().isNotEmpty()
    }


    @Test
    fun encodesWhitespacesInPath() = runTest {
        val url = Url + "/Favicon 144px.jpg"

        val response = underTest.head(url)

        println("Response: $response")

        assertThat(response::successful).isTrue()
        assertThat(response::requestedUrl).isEqualTo(url.replace(" ", "%20"))
    }

    @Test
    fun doesNotEncodeLegalValidCharacters() = runTest {
        val url = Url + "/faviconV2?client=chrome&nfrp=2&check_seen=true&min_size=16&size=32&max_size=64&fallback_opts=TYPE,SIZE,URL&url=https://heise.de"

        val response = underTest.head(url)

        assertThat(response::successful).isTrue()
        assertThat(response::requestedUrl).isEqualTo(url)
    }


    private fun assertNoContentResponse(response: WebClientResult<Unit>) {
        assertThat(response::successful).isTrue()
        assertThat(response::statusCode).isEqualTo(204)
        assertThat(response.body is Unit).isTrue()
    }

    private fun assertSuccessGetOrDeleteResponse(response: WebClientResult<String>) {
        assertThat(response::successful).isTrue()
        assertThat(response::statusCode).isEqualTo(200)
        assertThat(response::body).isNotNull().isNotEmpty()
    }

    private fun assertSuccessResponseWithBody(response: WebClientResult<String>, expectedBody: String = Body) {
        assertThat(response::successful).isTrue()
        assertThat(response::statusCode).isEqualTo(200)
        assertThat(response::body).isEqualTo(expectedBody)
    }

    private fun assertRequestFailed(response: WebClientResult<*>, errorType: ClientErrorType) {
        assertThat(response::successful).isFalse()
        assertThat(response::errorType).isNotNull().isEqualByComparingTo(errorType)
    }

}