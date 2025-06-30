package com.paymentoptions.pos.ui.composables.screens.refund

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary900

@Composable
fun BottomSectionContent(navController: NavController) {

    var refundAmount: Float by remember { mutableFloatStateOf(191.00f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)

    ) {
        Text(
            text = "Refund Money",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = primary900,
        )

        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        primary100.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Light
                    )
                ) { append("INR ") }
                withStyle(SpanStyle(primary100)) { append(refundAmount.toString()) }
            },
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = primary100,
            modifier = Modifier.padding(bottom = 10.dp)
        )
    }
}