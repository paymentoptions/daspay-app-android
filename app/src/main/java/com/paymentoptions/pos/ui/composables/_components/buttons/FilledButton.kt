package com.paymentoptions.pos.ui.composables._components.buttons

import androidx.compose.foundation.layout.fillMaxWidth
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
import com.paymentoptions.pos.ui.composables._components.CustomCircularProgressIndicator
import com.paymentoptions.pos.ui.theme.primary100

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
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors().copy(
            contentColor = Color.White, containerColor = primary100
        ),
        shape = RoundedCornerShape(8.dp)
    ) {

        if (isLoading) CustomCircularProgressIndicator(color = Color.White)
        else Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}