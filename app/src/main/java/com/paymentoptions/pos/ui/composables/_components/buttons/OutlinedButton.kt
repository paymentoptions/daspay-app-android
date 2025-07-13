package com.paymentoptions.pos.ui.composables._components.buttons

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.ui.composables._components.MyCircularProgressIndicator
import com.paymentoptions.pos.ui.theme.primary900

@Composable
fun OutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    disabled: Boolean = false,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Bold,
) {
    Button(
        enabled = !disabled,
        onClick = onClick,
        modifier = modifier.border(1.dp, Color.LightGray, shape = RoundedCornerShape(6.dp)),
        colors = ButtonDefaults.buttonColors().copy(
            contentColor = primary900, containerColor = Color.Transparent
        ),
//        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (isLoading) MyCircularProgressIndicator(color = Color.White)
        else Text(text = text, fontSize = fontSize, fontWeight = fontWeight)
    }
}