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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paymentoptions.pos.ui.theme.enabledFilledButtonGradientBrush

@Composable
fun MySwitch(isEnabled: Boolean, onClick: (state: Boolean) -> Unit) {

    Box(
        modifier = Modifier
            .width(51.dp)
            .height(31.dp)
            .background(brush = enabledFilledButtonGradientBrush, shape = RoundedCornerShape(50))
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
                    color = androidx.compose.ui.graphics.Color.White, shape = RoundedCornerShape(50)
                )
        )
    }
}

@Preview
@Composable
fun MySwitchPreview() {
    var isEnabledState by remember { mutableStateOf(false) }
    MySwitch(isEnabled = isEnabledState, onClick = {
        isEnabledState = !isEnabledState
    })
}