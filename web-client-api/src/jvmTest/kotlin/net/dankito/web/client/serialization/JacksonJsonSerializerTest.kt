package net.dankito.web.client.serialization

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.containsOnly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlin.test.Test

class JacksonJsonSerializerTest {

    private val underTest = JacksonJsonSerializer()


    @Test
    fun deserializeList() {
        val result = underTest.deserialize("""["one","two"]""", List::class, String::class)

        assertThat(result).hasSize(2)
        assertThat(result[0]).isEqualTo("one")
        assertThat(result[1]).isEqualTo("two")
    }

    @Test
    fun deserializeCollection() {
        val result = underTest.deserialize("""["one","two"]""", Collection::class, String::class)

        assertThat(result).hasSize(2)
        assertThat(result is List).isTrue()
        assertThat((result as List)[0]).isEqualTo("one")
        assertThat(result[1]).isEqualTo("two")
    }

    @Test
    fun deserializeSet() {
        val result = underTest.deserialize("""["one","two"]""", Set::class, String::class)

        assertThat(result).hasSize(2)
        assertThat(result).containsExactlyInAnyOrder("one", "two")
    }

    @Test
    fun deserializeMap() {
        val result = underTest.deserialize("""{"one":1,"two":2}""", Map::class, String::class, Int::class)

        assertThat(result).hasSize(2)
        assertThat(result as Map<String, Int>).containsOnly("one" to 1, "two" to 2)
    }

}