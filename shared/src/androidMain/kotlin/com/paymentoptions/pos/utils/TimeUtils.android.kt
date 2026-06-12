package com.paymentoptions.pos.utils

import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Date
import java.util.Locale

actual fun formatEpochMillis(millis: Long, pattern: String): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(millis))
}

//actual fun parseIsoDateToMillis(dateString: String): Long {
//    return try {
//        OffsetDateTime.parse(dateString).toInstant().toEpochMilli()
//    } catch (e: Exception) {
//        0L
//    }
//}
