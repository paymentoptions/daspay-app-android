package com.paymentoptions.pos.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.Clock

/**
 * Safely parses a date string to Instant.
 * Handles malformed date strings with space instead of + in timezone offset.
 * e.g., "2026-01-13T05:56:07.759 00:00" -> "2026-01-13T05:56:07.759+00:00"
 */
fun safeParseDateTime(dateString: String): Instant {
    // Fix malformed date strings with space instead of + in timezone offset
    val fixedDateString = dateString.replace(
        Regex("(\\d{2}:\\d{2}:\\d{2}\\.\\d+) (\\d{2}:\\d{2})$"),
        "$1+$2"
    )

    return try {
        Instant.parse(fixedDateString)
    } catch (e: Exception) {
        try {
            // Try parsing without timezone by appending Z
            Instant.parse(dateString.substringBefore(" ").plus("Z"))
        } catch (e2: Exception) {
            // Fallback to current time
            Clock.System.now()
        }
    }
}
