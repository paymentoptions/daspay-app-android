package com.paymentoptions.pos.device

import com.paymentoptions.pos.showToast

actual fun openPdfAtPath(filePath: String) {
    showToast("PDF preview is not available on this platform.")
}
