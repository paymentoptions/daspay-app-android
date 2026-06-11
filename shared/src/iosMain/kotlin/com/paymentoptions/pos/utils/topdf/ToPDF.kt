package com.paymentoptions.pos.utils.topdf

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable

actual object ComposePdfExporter {
    actual suspend fun export(
        fileName: String,
        composable: @Composable (LazyListState) -> Unit,
        spacing: Int,
        pageSize: PageSize,
        onProgress: (PdfExportProgress) -> Unit,
    ) {
        onProgress(PdfExportProgress.Error(Exception("PDF export is not yet implemented on iOS")))
    }
}
