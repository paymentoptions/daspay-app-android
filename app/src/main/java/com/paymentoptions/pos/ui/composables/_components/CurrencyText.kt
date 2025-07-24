package com.paymentoptions.pos.ui.composables._components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.ui.theme.primary100

@Composable
fun CurrencyText(
    currency: String,
    amount: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 36.sp,
    color: Color = primary100,
    textAlign: TextAlign = TextAlign.Center,
    addSpaceAfterCurrency: Boolean = true,
) {
    Text(
        text = buildAnnotatedString {
            if (currency.isNotEmpty()) withStyle(
                SpanStyle(
                    color.copy(alpha = 0.7f), fontWeight = FontWeight.Medium
                )
            ) { append(currency + (if (addSpaceAfterCurrency) " " else "")) }

            if (amount.isNotEmpty()) withStyle(SpanStyle(color)) { append(amount) }
        },
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        color = primary100,
        textAlign = textAlign,
        modifier = modifier
    )
}