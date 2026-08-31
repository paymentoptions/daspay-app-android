package com.paymentoptions.pos.utils

import java.text.DecimalFormat
import java.math.BigDecimal
import java.math.RoundingMode

fun Float?.formatToPrecisionString(precision: Int = 2): String =
    if (this == null) "" else DecimalFormat("0.${"0".repeat(precision)}").format(this)



fun Float.formatAmount(): String {
    val rounded = BigDecimal(this.toString())
        .setScale(2, RoundingMode.HALF_UP)

    return if (rounded.stripTrailingZeros().scale() <= 0) {
        rounded.toBigInteger().toString()
    } else {
        rounded.toPlainString()
    }
}

