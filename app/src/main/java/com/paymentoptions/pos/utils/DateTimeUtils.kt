package com.paymentoptions.pos.utils

import java.time.OffsetDateTime

/**
 * Safely parses a date string to OffsetDateTime.
 * Handles malformed date strings with space instead of + in timezone offset.
 * e.g., "2026-01-13T05:56:07.759 00:00" -> "2026-01-13T05:56:07.759+00:00"
 */
fun safeParseOffsetDateTime(dateString: String): OffsetDateTime {
    // Fix malformed date strings with space instead of + in timezone offset
    val fixedDateString = dateString.replace(
        Regex("(\\d{2}:\\d{2}:\\d{2}\\.\\d+) (\\d{2}:\\d{2})$"),
        "$1+$2"
    )

    return try {
        OffsetDateTime.parse(fixedDateString)
    } catch (e: Exception) {
        try {
            // Try parsing without timezone by appending Z
            OffsetDateTime.parse(dateString.substringBefore(" ").plus("Z"))
        } catch (e2: Exception) {
            // Fallback to current time
            OffsetDateTime.now()
        }
    }
}

