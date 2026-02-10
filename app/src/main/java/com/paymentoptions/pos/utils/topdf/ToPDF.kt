package com.paymentoptions.pos.utils.topdf

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.view.View
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.FileProvider
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ComposePdfExporter {
    @OptIn(InternalComposeUiApi::class)
    suspend fun export(
        context: Context,
        fileName: String,
        composable: @Composable (LazyListState) -> Unit,
        spacing: Int = 0,
        pageSize: PageSize = PageSize.LETTER,
        onProgress: (PdfExportProgress) -> Unit,
    ) = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val pixelsPerDp = context.resources.displayMetrics.density
            val pageWidth = (pageSize.width.value * pixelsPerDp).toInt()
            val pageHeight = (pageSize.height.value * pixelsPerDp).toInt()

            val listState = LazyListState()

            val composeView = withContext(Dispatchers.Main) {
                // Create ComposeView and attach it to the activity
                val composeView = ComposeView(context).apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                    val activity =
                        context.findActivity() ?: throw IllegalStateException("Activity not found")
                    setViewTreeSavedStateRegistryOwner(activity)
                    setViewTreeLifecycleOwner(activity)
                }

                // Create the dialog and set the layout params
                val dialog = Dialog(context).apply {
                    setContentView(composeView)
                    window?.setLayout(pageWidth, pageHeight)
                }

                // Set content
                composeView.setContent {
                    composable(listState)
                }

                dialog.show()

                // Give time for composition
                delay(500)

                // make sure the compose view is the size of the page
                // so that it can be correctly drawn to the PDF
                composeView.measure(
                    View.MeasureSpec.makeMeasureSpec(pageWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(pageHeight, View.MeasureSpec.EXACTLY)
                )
                composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)

                dialog.dismiss()

                composeView
            }

            val totalCount = listState.layoutInfo.totalItemsCount
            var totalHeight = 0
            withContext(Dispatchers.Main) {
                val spacing =
                    spacing.dpToPx(context) // the spacing between each item, via Arrangement.spacedBy(16.dp)

                // Go through each item in the list and calculate the total height
                for (page in 0 until totalCount) {
                    listState.scrollTo(page)

                    val item =
                        listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == page }
                            ?: continue

                    totalHeight += item.size
                    // Add any spacing between items
                    if (page < totalCount - 1) {
                        totalHeight += spacing
                    }
                }

                // add the top and bottom content padding, if not the last page might be clipped
                totalHeight += listState.layoutInfo.beforeContentPadding + listState.layoutInfo.afterContentPadding
                // HACK: Add some extra height to ensure the last item is fully visible,
                // 50dp works like a charm
                totalHeight += 50.dpToPx(context)

                // Go back to the top, so we can start drawing the pages
                listState.scrollTo(0)
            }

            val pagesNeeded = (totalHeight + pageHeight - 1) / pageHeight

            // Create pages
            for (pageNum in 0 until pagesNeeded) {
                withContext(Dispatchers.Main) {
                    onProgress(PdfExportProgress.Progress(pageNum + 1, pagesNeeded))

                    val remainingHeight = totalHeight - (pageNum * pageHeight)
                    val currentPageHeight = minOf(pageHeight, remainingHeight)

                    val bitmap = Bitmap.createBitmap(
                        pageWidth, pageHeight, Bitmap.Config.ARGB_8888
                    )
                    composeView.draw(Canvas(bitmap))

                    withContext(Dispatchers.IO) {
                        val pageInfo = PdfDocument.PageInfo.Builder(
                            pageWidth, pageHeight, pageNum + 1
                        ).create()

                        val page = pdfDocument.startPage(pageInfo)

                        // For the last page, we need to crop the bitmap to the remaining height
                        if (currentPageHeight != pageHeight && pageNum > 0) {
                            page.canvas.drawBitmap(
                                bitmap, Rect(
                                    0,
                                    pageHeight - currentPageHeight,  // Source starts from where we want to crop
                                    pageWidth,
                                    pageHeight
                                ), Rect(
                                    0,
                                    0,  // Destination starts at 0 since we're creating a new smaller bitmap
                                    pageWidth,
                                    currentPageHeight  // Use currentPageHeight for destination height
                                ), null
                            )
                        } else {
                            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                        }
                        pdfDocument.finishPage(page)
                        bitmap.recycle()
                    }

                    listState.scrollBy(currentPageHeight.toFloat())
                }
            }

            val outputFile = File(context.externalCacheDir, "${fileName}.pdf")

            // Write PDF to file
            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }

            pdfDocument.close()

            onProgress(
                PdfExportProgress.Success(
                    FileProvider.getUriForFile(
                        context, "${context.packageName}.fileprovider", outputFile
                    )
                )
            )
        } catch (e: Exception) {
            onProgress(PdfExportProgress.Error(e))
        }
    }

    /**
     * Scrolls the list to the given index and waits for the item to be visible
     */
    private suspend fun LazyListState.scrollTo(index: Int) {
        scrollToItem(index)
        snapshotFlow { layoutInfo.visibleItemsInfo }.first { visibleItems ->
            visibleItems.any { it.index == index }
        }
    }
}