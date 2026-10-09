package com.paymentoptions.pos.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.paymentoptions.pos.logger.AppLogger

fun openUrlInBrowser(context: Context, url: String?) {
    if (url.isNullOrBlank()) return
    try {
        val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        AppLogger.error("Failed to open URL in browser: $url", e)
    }
}
