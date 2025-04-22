package net.dankito.web.client.util

import io.ktor.http.*
import io.ktor.util.date.*
import net.dankito.datetime.Instant
import net.dankito.datetime.LocalDateTime

object WebDateTimeUtil {

    fun httpDateStringToInstantOr(httpDateString: String, defaultValue: Instant): Instant =
        httpDateStringToInstantOrNull(httpDateString) ?: defaultValue

    fun httpDateStringToInstantOrNull(httpDateString: String): Instant? =
        try {
            httpDateStringToInstant(httpDateString)
        } catch (e: Throwable) {
            null
        }

    fun httpDateStringToInstant(httpDateString: String): Instant =
        gmtDateToInstant(httpDateString.fromHttpToGmtDate())


    fun gmtDateToInstant(date: GMTDate): Instant =
        if (date.timestamp == 0L && date.year != 0) {
            LocalDateTime(date.year, date.month.ordinal + 1, date.dayOfMonth, date.hours, date.minutes, date.seconds).toInstantAtUtc()
        } else {
            Instant.ofEpochMilli(date.timestamp)
        }

}