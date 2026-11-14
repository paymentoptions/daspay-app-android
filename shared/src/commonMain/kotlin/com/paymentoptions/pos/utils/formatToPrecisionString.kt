package com.paymentoptions.pos.utils

// Default values live only on the expect declaration
expect fun Float?.formatToPrecisionString(precision: Int = 2): String
expect fun Double?.formatToPrecisionString(precision: Int = 2): String

expect fun Float.formatAmount(): String