package com.paymentoptions.pos.device

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun screenRatioToDp(ratio: Float): Dp {
    val configuration = LocalConfiguration.current
    return configuration.screenHeightDp.dp.times(ratio)
}