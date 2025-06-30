package com.paymentoptions.pos.ui.composables.screens.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary900

@Composable
fun BottomSectionContent(navController: NavController) {

    var receivalAmount: Float by remember { mutableFloatStateOf(2000.00f) }
    val currency = "JPY"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Receival for the day",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = primary900,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        primary100.copy(alpha = 0.5f), fontWeight = FontWeight.Light
                    )
                ) { append("$currency ") }

                withStyle(SpanStyle(primary100)) { append(receivalAmount.toString()) }
            },
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = primary100,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        FilledButton(
            text = "View Insights", onClick = {}, modifier = Modifier
                .width(160.dp)
                .height(36.dp)
        )

        Spacer(Modifier.height(20.dp))
        RecentTransactions(navController)
    }
}