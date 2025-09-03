package net.dankito.web.client

import assertk.assertThat
import assertk.assertions.*
import kotlin.test.Test

class JavaHttpClientRequestConfigurerTest {

    private val underTest = JavaHttpClientRequestConfigurer()


    @Test
    fun buildUrl_baseUrlNull() {
        val url = "https://codinux.net"

        val result = underTest.buildUrl(null, RequestParameters(url))

        assertThat(result).isEqualTo(url)
    }

    @Test
    fun buildUrl_baseUrlSet_AbsoluteUrl() {
        val url = "https://codinux.net"

        val result = underTest.buildUrl("https://dankito.net", url)

        assertThat(result).isEqualTo(url)
    }

    @Test
    fun buildUrl_baseUrl_RelativeUrl() {
        val baseUrl = "https://codinux.net"
        val url = "downloads.html"

        val result = underTest.buildUrl(baseUrl, url)

        assertThat(result).isEqualTo(baseUrl + "/" + url)
    }

    @Test
    fun buildUrl_queryParameters() {
        val url = "https://codinux.net"

        val result = underTest.buildUrl(null, url, queryParameters = mapOf("q" to "Liebe", "format" to "cuddle"))

        assertThat(result).isEqualTo(url + "?q=Liebe&format=cuddle")
    }

}