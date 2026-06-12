package com.paymentoptions.pos.utils

import kotlinx.datetime.Instant

expect fun formatEpochMillis(millis: Long, pattern: String): String

fun parseIsoDateToMillis(dateString: String): Long {
    // Fix malformed date strings with space instead of + in timezone offset
    // e.g., "2026-01-13T05:56:07.759 00:00" -> "2026-01-13T05:56:07.759+00:00"
    val fixedDateString = dateString.replace(
        Regex("(\\d{2}:\\d{2}:\\d{2}\\.\\d+) (\\d{2}:\\d{2})$"),
        "$1+$2"
    )

    return try {
        Instant.parse(fixedDateString).toEpochMilliseconds()
    } catch (e: Exception) {
        try {
            // Try parsing without timezone by appending Z if it's missing but expected
            val cleaned = if (dateString.contains(" ")) dateString.substringBefore(" ") else dateString
            val withZ = if (!cleaned.endsWith("Z") && !cleaned.contains("+")) cleaned + "Z" else cleaned
            Instant.parse(withZ).toEpochMilliseconds()
        } catch (e2: Exception) {
            0L
        }
    }
}
