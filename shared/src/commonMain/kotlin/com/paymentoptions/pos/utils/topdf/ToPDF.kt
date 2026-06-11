package com.paymentoptions.pos.utils.topdf

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable

expect object ComposePdfExporter {
    suspend fun export(
        fileName: String,
        composable: @Composable (LazyListState) -> Unit,
        spacing: Int = 0,
        pageSize: PageSize = PageSize.LETTER,
        onProgress: (PdfExportProgress) -> Unit,
    )
}
