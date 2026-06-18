package com.paymentoptions.pos.device

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIScreen

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun ScreenRatioToDp(ratio: Float): Dp {
    val screenHeight = UIScreen.mainScreen.bounds.useContents { size.height }
    return (screenHeight.toFloat() * ratio).dp
}
