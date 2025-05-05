package net.dankito.web.client

import assertk.assertThat
import assertk.assertions.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class KtorWebClientTest {

    companion object {
        private val Url = "https://staging.dankito.net/request-logger"
    }


    private val underTest = KtorWebClient(ignoreCertificateErrors = true)


    @Test
    fun get() = runTest {
        val response = underTest.get<String>(Url)

        assertTrue(response.successful)
        assertEquals(204, response.statusCode)
        assertThat(response.body).isNotNull().isEmpty()
    }

    @Test
    fun getWithParameters() = runTest {
        val response = underTest.get(RequestParameters(Url, String::class))

        assertTrue(response.successful)
        assertEquals(204, response.statusCode)
        assertThat(response.body).isNotNull().isEmpty()
    }


    @Test
    fun head() = runTest {
        val response = underTest.head(Url)

        assertTrue(response.successful)
        assertEquals(204, response.statusCode)
        assertThat(response.body is Unit).isTrue()
    }

    @Test
    fun headWithParameters() = runTest {
        val response = underTest.head(RequestParameters(Url, Unit::class))

        assertTrue(response.successful)
        assertEquals(204, response.statusCode)
        assertThat(response.body is Unit).isTrue()
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

    private fun assertRequestFailed(response: WebClientResult<*>, errorType: ClientErrorType) {
        assertThat(response.successful).isFalse()
        assertThat(response.errorType).isNotNull().isEqualByComparingTo(errorType)
    }


    @Test
    fun testLazyResponseDetailValues() = runTest {
        val response = underTest.get<String>(Url)

        assertThat(response.successful).isTrue()
        assertThat(response.responseDetails).isNotNull()

        val details = response.responseDetails!!
//        assertThat(details.contentType).isEqualTo("text/html")
////        assertThat(details.contentLength).isNotNull().isGreaterThan(0) // don't know why but on Linux is the Content-Length header missing
//        assertThat(details.charset).isNotNull().isNotEmpty()

        assertThat(details.headers).isNotEmpty()
        assertThat(details.cookies).isEmpty()

        assertThat(details.requestTime).isNotNull()
        assertThat(details.responseTime.epochSeconds).isGreaterThan(1_745_000_000)
        assertThat(details.responseTime.toEpochMilliseconds()).isGreaterThanOrEqualTo(details.requestTime!!.toEpochMilliseconds())
        assertThat(details.httpProtocolVersion).isNotNull().isNotEmpty()
    }

}