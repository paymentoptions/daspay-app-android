package com.paymentoptions.pos.ui.composables._components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Square checkbox used for the product "service fee enabled" toggle (release 62d16d8).
@Composable
fun RectangleCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    checkedColor: Color = Color(0xFF2563EB),
    uncheckedColor: Color = Color.Transparent,
    borderColor: Color = Color.Gray,
    cornerRadius: Dp = 4.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .size(size)
            .background(color = if (checked) checkedColor else uncheckedColor, shape = shape)
            .border(width = 1.5.dp, color = if (checked) checkedColor else borderColor, shape = shape)
            .clickable(interactionSource = interactionSource, indication = null) {
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Checked",
                tint = Color.White,
                modifier = Modifier.size(size * 0.7f)
            )
        }
    }
}
