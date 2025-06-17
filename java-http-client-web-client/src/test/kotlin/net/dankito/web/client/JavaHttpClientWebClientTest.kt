package net.dankito.web.client

import assertk.assertThat
import assertk.assertions.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class JavaHttpClientWebClientTest {

    companion object {
        private val Url = "https://staging.dankito.net/echo/KtorWebClientTest"

        private const val Body = "Just a test, no animals have been harmed"
    }


    private val underTest = JavaHttpClientWebClient(ClientConfig(ignoreCertificateErrors = true))


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