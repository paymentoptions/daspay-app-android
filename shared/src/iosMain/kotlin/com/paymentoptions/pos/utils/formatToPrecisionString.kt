package com.paymentoptions.pos.utils

import platform.Foundation.NSDecimalNumber
import platform.Foundation.NSDecimalNumberHandler
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.NSNumberFormatterRoundHalfEven
import platform.Foundation.NSRoundingMode

private fun precisionFormatter(precision: Int, grouping: Boolean = true) =
    NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterDecimalStyle
        locale = NSLocale(localeIdentifier = "en_US")
        usesGroupingSeparator = grouping
        groupingSeparator = ","
        groupingSize = 3u
        decimalSeparator = "."
        minimumFractionDigits = precision.toULong()
        maximumFractionDigits = precision.toULong()
        roundingMode = NSNumberFormatterRoundHalfEven   // same as DecimalFormat default
    }

actual fun Float?.formatToPrecisionString(precision: Int): String {
    if (this == null) return ""
    return precisionFormatter(precision).stringFromNumber(NSNumber(this.toDouble())) ?: ""
}

actual fun Double?.formatToPrecisionString(precision: Int): String {
    if (this == null) return ""
    return precisionFormatter(precision).stringFromNumber(NSNumber(this)) ?: ""
}

actual fun Float.formatAmount(): String {
    // NSRoundPlain == Java's HALF_UP (ties round away from zero)
    val handler = NSDecimalNumberHandler(
        roundingMode = NSRoundingMode.NSRoundPlain,
        scale = 2,
        raiseOnExactness = false,
        raiseOnOverflow = false,
        raiseOnUnderflow = false,
        raiseOnDivideByZero = false
    )
    // Build from the string, like BigDecimal(this.toString()), to avoid binary float noise
    val rounded = NSDecimalNumber(string = this.toString())
        .decimalNumberByRoundingAccordingToBehavior(handler)

    val text = precisionFormatter(2, grouping = false).stringFromNumber(rounded) ?: return ""
    // Same as: stripTrailingZeros().scale() <= 0 -> integer, else keep 2 decimals
    return if (text.endsWith(".00")) text.dropLast(3) else text
}