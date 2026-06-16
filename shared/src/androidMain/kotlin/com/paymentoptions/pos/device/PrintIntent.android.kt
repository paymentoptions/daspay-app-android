package com.paymentoptions.pos.device

import android.content.Intent
import com.paymentoptions.pos.currentActivity

actual fun openPrintIntent(uri: String, mimeType: String) {
    val context = currentActivity ?: return
    val printIntent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, android.net.Uri.parse(uri))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(printIntent, "Print Receipt"))
}
