@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package com.paymentoptions.pos.device

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectGetHeight
import platform.UIKit.UIScreen

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun ScreenRatioToDp(ratio: Float): Dp {
    val screenHeight = CGRectGetHeight(UIScreen.mainScreen.bounds)
    return (screenHeight.toFloat() * ratio).dp
}
