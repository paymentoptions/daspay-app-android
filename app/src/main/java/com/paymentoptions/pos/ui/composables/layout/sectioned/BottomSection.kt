package com.paymentoptions.pos.ui.composables.layout.sectioned

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BottomSection(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .background(color = Color.Transparent)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(color = Color.White)
    ) {
        content()
    }
}