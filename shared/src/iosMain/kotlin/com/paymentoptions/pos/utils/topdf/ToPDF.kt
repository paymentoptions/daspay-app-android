package com.paymentoptions.pos.utils.topdf

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSFileManager
import platform.Foundation.NSMutableData
import platform.Foundation.NSTemporaryDirectory
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPageWithInfo
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIScreen

actual object ComposePdfExporter {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun export(
        fileName: String,
        composable: @Composable (LazyListState) -> Unit,
        spacing: Int,
        pageSize: PageSize,
        onProgress: (PdfExportProgress) -> Unit,
    ) = withContext(Dispatchers.Main) {
        try {
            val scale = UIScreen.mainScreen.scale
            val pageWidthPt = pageSize.width.value.toDouble()
            val pageHeightPt = pageSize.height.value.toDouble()
            val pageBounds = CGRectMake(0.0, 0.0, pageWidthPt, pageHeightPt)

            val rootView = UIApplication.sharedApplication.keyWindow?.rootViewController?.view
                ?: run {
                    onProgress(PdfExportProgress.Error(IllegalStateException("No root view available")))
                    return@withContext
                }

            val pdfData = NSMutableData()
            UIGraphicsBeginPDFContextToData(pdfData, pageBounds, null)
            onProgress(PdfExportProgress.Progress(1, 1))

            UIGraphicsBeginPDFPageWithInfo(pageBounds, null)

            val context = UIGraphicsGetCurrentContext()
            if (context != null) {
                rootView.layer.renderInContext(context)
            }

            UIGraphicsEndPDFContext()

            val outputPath = "${NSTemporaryDirectory()}${fileName}.pdf"
            NSFileManager.defaultManager.createFileAtPath(outputPath, contents = pdfData, attributes = null)

            onProgress(PdfExportProgress.Success(outputPath))
        } catch (e: Exception) {
            onProgress(PdfExportProgress.Error(e))
        }
    }
}
