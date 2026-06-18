package com.paymentoptions.pos.utils

import java.text.DecimalFormat

actual fun Float?.formatToPrecisionString(precision: Int): String =
    if (this == null) "0.0" else DecimalFormat("0.${"0".repeat(precision)}").format(this)

actual fun Double?.formatToPrecisionString(precision: Int): String =
    if (this == null) "0.0" else DecimalFormat("0.${"0".repeat(precision)}").format(this)
