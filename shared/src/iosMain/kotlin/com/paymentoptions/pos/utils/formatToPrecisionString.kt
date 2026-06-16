package com.paymentoptions.pos.utils

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle

actual fun Float?.formatToPrecisionString(precision: Int): String {
    if (this == null) return ""
    val formatter = NSNumberFormatter().apply {
        minimumFractionDigits = precision.toULong()
        maximumFractionDigits = precision.toULong()
        numberStyle = NSNumberFormatterDecimalStyle
    }
    return formatter.stringFromNumber(NSNumber(this.toDouble())) ?: ""
}

actual fun Double?.formatToPrecisionString(precision: Int): String {
    if (this == null) return ""
    val formatter = NSNumberFormatter().apply {
        minimumFractionDigits = precision.toULong()
        maximumFractionDigits = precision.toULong()
        numberStyle = NSNumberFormatterDecimalStyle
    }
    return formatter.stringFromNumber(NSNumber(this)) ?: ""
}
