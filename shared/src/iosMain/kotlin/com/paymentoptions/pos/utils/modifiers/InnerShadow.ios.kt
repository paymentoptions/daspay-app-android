package com.paymentoptions.pos.utils.modifiers

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

actual fun Modifier.innerShadow(
    color: Color,
    cornersRadius: Dp,
    spread: Dp,
    blur: Dp,
    offsetY: Dp,
    offsetX: Dp,
    showBottom: Boolean,
): Modifier = this

//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.drawWithContent
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Rect
//import androidx.compose.ui.graphics.BlendMode
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Paint
//import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//
//actual fun Modifier.innerShadow(
//    color: Color,
//    cornersRadius: Dp,
//    spread: Dp,
//    blur: Dp,
//    offsetY: Dp,
//    offsetX: Dp,
//    showBottom: Boolean,
//): Modifier = drawWithContent {
//    drawContent()
//
//    val rect = Rect(Offset.Zero, size)
//
//    drawIntoCanvas { canvas ->
//        val layerPaint = Paint().apply {
//            this.color = color
//            isAntiAlias = true
//        }
//        canvas.saveLayer(rect, layerPaint)
//        canvas.drawRoundRect(
//            rect.left, rect.top, rect.right, rect.bottom,
//            cornersRadius.toPx(), cornersRadius.toPx(),
//            layerPaint,
//        )
//
//        val blurSteps = if (blur > 0.dp) 6 else 1
//        val baseAlpha = if (blurSteps > 1) 0.25f else 1f
//
//        val left = if (offsetX > 0.dp) rect.left + offsetX.toPx() else rect.left
//        val top = if (offsetY > 0.dp) rect.top + offsetY.toPx() else rect.top
//        val right = if (offsetX < 0.dp) rect.right + offsetX.toPx() else rect.right
//        val shadowBottom = if (showBottom) {
//            (if (offsetY < 0.dp) rect.bottom + offsetY.toPx() else rect.bottom) - spread.toPx() / 2
//        } else {
//            rect.bottom + spread.toPx() + 50
//        }
//
//        for (step in 0 until blurSteps) {
//            val progress = if (blurSteps > 1) step.toFloat() / (blurSteps - 1) else 0f
//            val inset = progress * blur.toPx()
//            val alpha = baseAlpha * (1f - progress * 0.6f)
//
//            val innerPaint = Paint().apply {
//                isAntiAlias = true
//                this.color = Color.Black.copy(alpha = alpha)
//                blendMode = BlendMode.DstOut
//            }
//            canvas.drawRoundRect(
//                left + spread.toPx() / 2 + inset,
//                top + spread.toPx() / 2 + inset,
//                right - spread.toPx() / 2 - inset,
//                shadowBottom - inset,
//                cornersRadius.toPx(), cornersRadius.toPx(),
//                innerPaint,
//            )
//        }
//
//        canvas.restore()
//    }
//}
