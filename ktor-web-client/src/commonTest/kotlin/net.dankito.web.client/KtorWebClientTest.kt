package net.dankito.web.client

import assertk.assertThat
import assertk.assertions.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class KtorWebClientTest {

    companion object {
        private val Url = "https://www.nytimes.com" // does not work for JS Browser, i guess due to CORS
    }


    private val underTest = KtorWebClient(ignoreCertificateErrors = true)


    @Test
    fun get() = runTest {
        val response = underTest.get<String>(Url)

        assertTrue(response.successful)
        assertEquals(200, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun getWithParameters() = runTest {
        val response = underTest.get(RequestParameters(Url, String::class))

        assertTrue(response.successful)
        assertEquals(200, response.statusCode)
        assertNotNull(response.body)
    }


    @Test
    fun head() = runTest {
        val response = underTest.head(Url)

        assertTrue(response.successful)
        assertEquals(200, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun headWithParameters() = runTest {
        val response = underTest.head(RequestParameters(Url, Unit::class))

        assertTrue(response.successful)
        assertEquals(200, response.statusCode)
        assertNotNull(response.body)
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

        assertThat(response.responseDetails!!.contentType).isEqualTo("text/html")
//        assertThat(response.responseDetails!!.contentLength).isNotNull().isGreaterThan(0) // don't know why but on Linux is the Content-Length header missing
        assertThat(response.responseDetails!!.charset).isNotNull().isNotEmpty()

        assertThat(response.responseDetails!!.headers).isNotEmpty()
        assertThat(response.responseDetails!!.cookies).isNotEmpty()

        assertThat(response.responseDetails!!.requestTimeHttpDateString).isNotNull().isNotEmpty()
        assertThat(response.responseDetails!!.responseTimeHttpDateString).isNotNull().isNotEmpty()
        assertThat(response.responseDetails!!.httpProtocolVersion).isNotNull().isNotEmpty()
    }

}