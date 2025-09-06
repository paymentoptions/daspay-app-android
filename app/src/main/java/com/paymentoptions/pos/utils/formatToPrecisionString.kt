package com.paymentoptions.pos.utils

import java.text.DecimalFormat

fun Float?.formatToPrecisionString(precision: Int = 2): String =
    if (this == null) "" else DecimalFormat("0.${"0".repeat(precision)}").format(this)

