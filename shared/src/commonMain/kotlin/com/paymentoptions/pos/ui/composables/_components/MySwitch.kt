package com.paymentoptions.pos.ui.composables._components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.paymentoptions.pos.ui.theme.enabledFilledButtonGradientBrush
import com.paymentoptions.pos.utils.modifiers.conditional

@Composable
fun MySwitch(isEnabled: Boolean, onClick: (state: Boolean) -> Unit) {

    Box(
        modifier = Modifier
            .width(51.dp)
            .height(31.dp)
            .conditional(isEnabled) {
                background(
                    brush = enabledFilledButtonGradientBrush, shape = RoundedCornerShape(50)
                )
            }
            .conditional(!isEnabled) {
                background(
                    color = Color.LightGray, shape = RoundedCornerShape(50)
                )
            }
            .padding(all = 2.dp)
            .clickable {
                onClick(isEnabled)
            },

        contentAlignment = if (isEnabled) Alignment.CenterEnd else Alignment.CenterStart
    ) {

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(29.dp)
                .background(
                    color = Color.White, shape = RoundedCornerShape(50)
                )
        )
    }
}
