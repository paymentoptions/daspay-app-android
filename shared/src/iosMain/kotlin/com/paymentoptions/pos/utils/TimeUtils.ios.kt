package com.paymentoptions.pos.utils

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.dateWithTimeIntervalSince1970

actual fun formatEpochMillis(millis: Long, pattern: String): String {
    val date = NSDate.dateWithTimeIntervalSince1970(millis / 1000.0)
    val formatter = NSDateFormatter().apply {
        dateFormat = pattern
    }
    return formatter.stringFromDate(date)
}
