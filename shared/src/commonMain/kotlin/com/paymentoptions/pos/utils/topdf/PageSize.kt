package com.paymentoptions.pos.utils.topdf

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Data class to represent the size of a page in points.
 *
 * @param width The width of the page in points.
 * @param height The height of the page in points.
 */
data class PageSize(
    val width: Dp,
    val height: Dp,
) {
    companion object {
        val A4 = PageSize(595.dp, 842.dp)
        val A3 = PageSize(842.dp, 1191.dp)
        val LETTER = PageSize(612.dp, 792.dp)
        val LEGAL = PageSize(612.dp, 1008.dp)
    }
}
