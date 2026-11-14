package com.paymentoptions.pos.utils

import com.paymentoptions.pos.logger.AppLogger
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

actual class UrlOpener {
    actual fun open(url: String?) {
        val formattedUrl = normalizeUrl(url) ?: return
        val nsUrl = NSURL.URLWithString(formattedUrl)
        if (nsUrl == null) {
            AppLogger.error("Failed to open URL in browser: invalid URL $url")
            return
        }
        dispatch_async(dispatch_get_main_queue()) {
            UIApplication.sharedApplication.openURL(
                nsUrl,
                options = emptyMap<Any?, Any>(),
                completionHandler = { success ->
                    if (!success) AppLogger.error("Failed to open URL in browser: $url")
                }
            )
        }
    }
}