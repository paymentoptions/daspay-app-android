package com.paymentoptions.pos.utils.topdf

/**
 * Sealed class to represent the progress of a PDF export operation.
 */
sealed class PdfExportProgress {
    data class Progress(val currentPage: Int, val totalPages: Int) : PdfExportProgress()
    data class Success(val filePath: String) : PdfExportProgress()
    data class Error(val exception: Exception) : PdfExportProgress()
}
