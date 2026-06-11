package com.paymentoptions.pos.utils

import java.text.DecimalFormat

actual fun Float?.formatToPrecisionString(precision: Int): String =
    if (this == null) "" else DecimalFormat("0.${"0".repeat(precision)}").format(this)
