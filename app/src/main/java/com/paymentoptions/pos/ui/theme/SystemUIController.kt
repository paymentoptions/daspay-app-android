package com.paymentoptions.pos.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController


@Composable
fun SystemUiController() {
    val systemUiController = rememberSystemUiController()

    systemUiController.setSystemBarsColor(
        color = Color.White, darkIcons = true
    )

    systemUiController.isSystemBarsVisible = false
}

@Composable
fun SetImmersiveMode(enabled: Boolean) {
    val systemUiController = rememberSystemUiController()
    systemUiController.isSystemBarsVisible = !enabled
}