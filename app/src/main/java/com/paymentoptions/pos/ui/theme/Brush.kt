package com.paymentoptions.pos.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val filledButtonGradientBrush = Brush.horizontalGradient(colors = listOf(primary500, primary400))
val containerBackgroundGradientBrush =
    Brush.verticalGradient(colors = listOf(Color(0xFFE6F6FF), Color.White))
val textGradientBrush =
    Brush.horizontalGradient(colorStops = arrayOf(0.5f to primary300, 1f to primary100))