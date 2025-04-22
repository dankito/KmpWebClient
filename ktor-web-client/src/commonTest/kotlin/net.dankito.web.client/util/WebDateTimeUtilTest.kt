package net.dankito.web.client.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import net.dankito.datetime.LocalDateTime
import kotlin.test.Test

class WebDateTimeUtilTest {

    @Test
    fun httpDateStringToInstant() {
        val result = WebDateTimeUtil.httpDateStringToInstant("Tue, 22 Apr 2025 20:29:04 GMT")

        assertThat(result).isEqualTo(LocalDateTime(2025, 4, 22, 20, 29, 4).toInstantAtUtc())
    }

}