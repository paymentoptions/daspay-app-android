package com.paymentoptions.pos.utils

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Dashed separator used in the receipt charge breakdown (release 21e807b).
@Composable
fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFB0B8D9),
    thickness: Dp = 2.dp,
    dashWidth: Dp = 5.dp,
    dashGap: Dp = 4.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = thickness.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(dashWidth.toPx(), dashGap.toPx()),
                phase = 0f
            )
        )
    }
}
