package com.paymentoptions.pos.device

import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

actual fun openPdfAtPath(filePath: String) {
    dispatch_async(dispatch_get_main_queue()) {
        val url = NSURL.fileURLWithPath(filePath)
        val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return@dispatch_async
        var topVC = rootVC
        while (topVC.presentedViewController != null) topVC = topVC.presentedViewController!!
        val activityVC = UIActivityViewController(
            activityItems = listOf(url),
            applicationActivities = null,
        )
        topVC.presentViewController(activityVC, animated = true, completion = null)
    }
}
