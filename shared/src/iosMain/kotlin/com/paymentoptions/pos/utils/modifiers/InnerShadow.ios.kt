package com.paymentoptions.pos.utils.modifiers

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

// TODO(KMP): port inner-shadow drawing to Skia/Canvas on iOS.
// Current iOS build renders without the inner shadow effect.
actual fun Modifier.innerShadow(
    color: Color,
    cornersRadius: Dp,
    spread: Dp,
    blur: Dp,
    offsetY: Dp,
    offsetX: Dp,
    showBottom: Boolean,
): Modifier = this
