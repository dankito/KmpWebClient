package net.dankito.web.client

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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

}