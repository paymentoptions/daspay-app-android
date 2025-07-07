package com.paymentoptions.pos.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val enabledFilledButtonGradientBrush =
    Brush.horizontalGradient(colorStops = arrayOf(0.3f to primary300, 1f to primary100))
val disabledFilledButtonGradientBrush = Brush.horizontalGradient(
    colorStops = arrayOf(
        0.3f to primary300.copy(alpha = 0.2f),
        1f to primary100.copy(alpha = 0.2f)
    )
)

val containerBackgroundGradientBrush =
    Brush.verticalGradient(colors = listOf(Color(0xFFE6F6FF), Color.White))
val textGradientBrush =
    Brush.horizontalGradient(colorStops = arrayOf(0.4f to primary300, 1f to primary100))