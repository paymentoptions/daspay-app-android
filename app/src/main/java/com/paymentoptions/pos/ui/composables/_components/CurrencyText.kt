package com.paymentoptions.pos.ui.composables._components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import com.paymentoptions.pos.ui.theme.primary100

@Composable
fun CurrencyText(currency: String, amount: String, fontSize: TextUnit) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    primary100.copy(alpha = 0.7f), fontWeight = FontWeight.Medium
                )
            ) { append("$currency ") }

            withStyle(SpanStyle(primary100)) { append(amount) }
        },
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        color = primary100,
    )
}