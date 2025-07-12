package com.paymentoptions.pos.ui.composables._components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MyElevatedCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White,
        ), elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp
        ), shape = RoundedCornerShape(6.dp), modifier = modifier
            .padding(1.dp)
            .shadow(
                elevation = 4.dp, shape = RoundedCornerShape(6.dp), ambientColor = Color(0xFFC2E3F7)
            )
            .border(1.dp, color = Color(0xFFB9E2FC), shape = RoundedCornerShape(6.dp))

    ) {
        content()
    }
}