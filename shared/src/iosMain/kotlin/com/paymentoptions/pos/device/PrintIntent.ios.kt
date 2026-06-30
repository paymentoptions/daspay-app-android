package com.paymentoptions.pos.device

import platform.Foundation.NSURL
import platform.UIKit.UIPrintInfo
import platform.UIKit.UIPrintInteractionController
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

actual fun openPrintIntent(uri: String, mimeType: String) {
    dispatch_async(dispatch_get_main_queue()) {
        val url = NSURL.URLWithString(uri) ?: NSURL.fileURLWithPath(uri)
        val printInfo = UIPrintInfo.printInfo()
        printInfo.jobName = "Receipt"
        // outputType defaults to UIPrintInfoOutputGeneral (0)

        val printer = UIPrintInteractionController.sharedPrintController()
        printer.printInfo = printInfo
        printer.printingItem = url
        printer.presentAnimated(true, completionHandler = null)
    }
}
