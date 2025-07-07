package com.paymentoptions.pos.ui.composables._components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.ui.composables._components.MyCircularProgressIndicator
import com.paymentoptions.pos.ui.theme.filledButtonGradientBrush

@Composable
fun FilledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    disabled: Boolean = false,
) {
    Button(
        enabled = !disabled,
        onClick = onClick,
        modifier = modifier
            .height(40.dp)
            .background(brush = filledButtonGradientBrush, shape = RoundedCornerShape(8.dp)),
        colors = ButtonDefaults.buttonColors().copy(
            contentColor = Color.White,
            containerColor = Color.Transparent,
            disabledContentColor = Color.White,
            disabledContainerColor = Color.White.copy(alpha = 0.5f),
        ),
        shape = RoundedCornerShape(8.dp)
    ) {

        if (isLoading) MyCircularProgressIndicator()
        else Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}