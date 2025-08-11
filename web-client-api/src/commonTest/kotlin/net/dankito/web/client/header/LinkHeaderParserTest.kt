package net.dankito.web.client.header

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import kotlin.test.Test

class LinkHeaderParserTest {

    private val underTest = LinkHeaderParser.Instance


    @Test
    fun paramWithoutQuotes() {
        val result = underTest.parse("<url>; rel=next")

        assertLinkHeader(result, "url", "rel" to "next")
    }

    @Test
    fun paramWithQuotes() {
        val result = underTest.parse("""<url>; rel="next"""")

        assertLinkHeader(result, "url", "rel" to "next")
    }

    @Test
    fun multipleParameters() {
        val result = underTest.parse("""<url>; rel="next";a=b; c="d"""")

        assertLinkHeader(result, "url", "rel" to "next", "a" to "b", "c" to "d")
    }

    @Test
    fun multipleUrls() {
        val result = underTest.parse("""<url1>; rel="next",<url2>;rel=last""")

        assertThat(result).isNotNull().hasSize(2)
        assertLinkHeader(result!![0], "url1", "rel" to "next")
        assertLinkHeader(result[1], "url2", "rel" to "last")
    }


    private fun assertLinkHeader(result: List<LinkHeader>?, url: String, vararg parameters: Pair<String, String>) {
        assertThat(result).isNotNull().hasSize(1)

        assertLinkHeader(result!!.first(), url, *parameters)
    }

    private fun assertLinkHeader(header: LinkHeader, url: String, vararg parameters: Pair<String, String>) {
        assertThat(header.url).isEqualTo(url)
        assertThat(header.parameters).containsOnly(*parameters)
    }

}