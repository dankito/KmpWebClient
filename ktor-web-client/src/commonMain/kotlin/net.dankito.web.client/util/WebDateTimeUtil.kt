package net.dankito.web.client.util

import io.ktor.util.date.*
import net.dankito.datetime.Instant
import net.dankito.datetime.LocalDateTime

object WebDateTimeUtil {

    fun gmtDateToInstant(date: GMTDate): Instant =
        if (date.timestamp == 0L && date.year != 0) {
            LocalDateTime(date.year, date.month.ordinal + 1, date.dayOfMonth, date.hours, date.minutes, date.seconds).toInstantAtUtc()
        } else {
            Instant.ofEpochMilli(date.timestamp)
        }

}