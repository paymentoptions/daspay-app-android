package com.paymentoptions.pos.ui.composables._components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.paymentoptions.pos.ui.theme.shadowColor2
import com.paymentoptions.pos.utils.modifiers.conditional
import com.paymentoptions.pos.utils.modifiers.innerShadow

@Composable
fun MyElevatedCard(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    content: @Composable () -> Unit,
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White,
        ), elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 1.dp
        ), shape = RoundedCornerShape(6.dp), modifier = modifier
            .padding(1.dp)
            .conditional(isSelected) {
                innerShadow(
                    color = shadowColor2,
                    blur = 10.dp,
                    spread = 2.dp,
                    cornersRadius = 6.dp,
                    offsetX = 0.dp,
                    offsetY = 0.dp
                )
            }
            .conditional(!isSelected) {
                innerShadow(
                    color = shadowColor2,
                    blur = 0.3.dp,
                    spread = 0.3.dp,
                    cornersRadius = 6.dp,
                    offsetX = 0.dp,
                    offsetY = 0.dp
                )
            }
    ) {
        content()
    }
}