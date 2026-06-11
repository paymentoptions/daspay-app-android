package com.paymentoptions.pos.device

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp

@Composable
expect fun ScreenRatioToDp(ratio: Float): Dp
