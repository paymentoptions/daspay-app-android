package com.paymentoptions.pos.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.paymentoptions.pos.logger.AppLogger

actual class UrlOpener(private val context: Context) {
    actual fun open(url: String?) {
        val formattedUrl = normalizeUrl(url) ?: return
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            AppLogger.error("Failed to open URL in browser: $url", e)
        }
    }
}