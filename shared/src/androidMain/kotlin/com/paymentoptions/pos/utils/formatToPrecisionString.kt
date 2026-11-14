package com.paymentoptions.pos.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private fun precisionFormatter(precision: Int): DecimalFormat {
    val symbols = DecimalFormatSymbols(Locale("en", "US"))
    val pattern = "#,##,##0.${"0".repeat(precision)}"
    return DecimalFormat(pattern, symbols)
}

actual fun Float?.formatToPrecisionString(precision: Int): String {
    if (this == null) return ""
    return precisionFormatter(precision).format(this)
}

actual fun Double?.formatToPrecisionString(precision: Int): String {
    if (this == null) return ""
    return precisionFormatter(precision).format(this)
}

actual fun Float.formatAmount(): String {
    val rounded = BigDecimal(this.toString())
        .setScale(2, RoundingMode.HALF_UP)

    return if (rounded.stripTrailingZeros().scale() <= 0) {
        rounded.toBigInteger().toString()
    } else {
        rounded.toPlainString()
    }
}