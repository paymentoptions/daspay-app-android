package com.paymentoptions.pos.utils

import java.text.DecimalFormat
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormatSymbols
import java.util.Locale

fun Float?.formatToPrecisionString(precision: Int = 2): String {
    if (this == null) return ""
    val symbols = DecimalFormatSymbols(Locale("en", "US"))
    val pattern = "#,##,##0.${"0".repeat(precision)}"
    return DecimalFormat(pattern, symbols).format(this)
}
fun Float.formatAmount(): String {
    val rounded = BigDecimal(this.toString())
        .setScale(2, RoundingMode.HALF_UP)

    return if (rounded.stripTrailingZeros().scale() <= 0) {
        rounded.toBigInteger().toString()
    } else {
        rounded.toPlainString()
    }
}

