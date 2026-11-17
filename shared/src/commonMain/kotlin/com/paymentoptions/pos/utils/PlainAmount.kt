package com.paymentoptions.pos.utils

/**
 * Two-decimal amount without thousands separators ("1234.50"), safe to parse back with
 * toFloat() / BigDecimal and to send as a payment amount. formatToPrecisionString() is for
 * display only: it groups thousands ("1,234.50").
 */
fun Float.toPlainAmountString(): String = formatToPrecisionString().replace(",", "")
