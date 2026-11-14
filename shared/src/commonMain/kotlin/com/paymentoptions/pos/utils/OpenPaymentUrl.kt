package com.paymentoptions.pos.utils

import androidx.compose.ui.platform.UriHandler
import com.paymentoptions.pos.logger.AppLogger

/** Opens a payment link in the device browser; logs instead of crashing if nothing can open it. */
fun UriHandler.openPaymentUrl(url: String?) {
    if (url.isNullOrBlank()) return
    try {
        openUri(url)
    } catch (e: Exception) {
        AppLogger.error("Could not open payment URL: ${e.message}")
    }
}
