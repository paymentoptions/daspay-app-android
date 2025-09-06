package com.paymentoptions.pos.ui.composables._components.buttons

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.ui.composables._components.images.loaders.LoaderWhiteImage
import com.paymentoptions.pos.ui.theme.disabledFilledButtonGradientBrush
import com.paymentoptions.pos.ui.theme.enabledFilledButtonGradientBrush

@Composable
fun FilledButton(
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
        modifier = modifier.background(
            brush = if (!disabled) enabledFilledButtonGradientBrush else disabledFilledButtonGradientBrush,
            shape = RoundedCornerShape(6.dp)
        ),
        colors = ButtonDefaults.buttonColors().copy(
            contentColor = Color.White,
            containerColor = Color.Transparent,
            disabledContentColor = Color.White,
            disabledContainerColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(6.dp),
//        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
    ) {
        if (isLoading) LoaderWhiteImage()
        else Text(text = text, fontSize = fontSize, fontWeight = fontWeight, lineHeight = 1.em)
    }
}