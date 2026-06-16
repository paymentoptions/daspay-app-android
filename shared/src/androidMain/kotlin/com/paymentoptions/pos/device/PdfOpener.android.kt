package com.paymentoptions.pos.device

import android.content.Intent
import androidx.core.content.FileProvider
import com.paymentoptions.pos.currentActivity
import com.paymentoptions.pos.showToast
import java.io.File

actual fun openPdfAtPath(filePath: String) {
    val context = currentActivity ?: return
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(filePath),
        )
        val printIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(printIntent)
        showToast("PDF created - Opening...")
    } catch (e: Exception) {
        showToast("Error opening PDF: ${e.message}")
    }
}
