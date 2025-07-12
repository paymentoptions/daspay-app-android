package com.paymentoptions.pos.ui.composables.screens.refund

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Lightbulb
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.inputs.DashedBorderInput
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.screens.dashboard.THE_TRANSACTION
import com.paymentoptions.pos.ui.composables.screens.notifications.ScreenTitleWithCloseButton
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.noBorder
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.ui.theme.purple50
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Date

@Composable
fun BottomSectionContent(
    navController: NavController,
    transaction: TransactionListDataRecord? = THE_TRANSACTION,
) {
    val currency = "HKD"

    val dateString =
        transaction?.Date ?: OffsetDateTime.now().toString()  //"2025-04-23T03:38:57.349+00:00"
    val dateTime = OffsetDateTime.parse(dateString)
    val date: Date = Date.from(dateTime.toInstant())
    val dateStringFormatted: String = SimpleDateFormat("dd MMMM YYYY").format(date)
    val noteState = rememberTextFieldState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Column(
            modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScreenTitleWithCloseButton(navController = navController)
            Text(
                text = "Refund Money",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = primary900,
            )

            Spacer(modifier = Modifier.height(8.dp))
            CurrencyText(currency = currency, amount = "123.12") //transaction.amount.toString())
        }

        Spacer(modifier = Modifier.height(16.dp))

        DashedBorderInput(
            state = noteState,
            modifier = Modifier.padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            placeholder = "Add a note (optional)",
            maxLength = 50,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Reference No.", style = AppTheme.typography.footnote.copy(
                        fontWeight = FontWeight.Normal, fontSize = 14.sp
                    )
                )

                Text(
                    transaction?.TransactionID.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = primary500
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Date", style = AppTheme.typography.footnote.copy(
                        fontWeight = FontWeight.Normal, fontSize = 14.sp
                    )
                )

                Text(
                    dateStringFormatted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = primary500
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Trace", style = AppTheme.typography.footnote.copy(
                        fontWeight = FontWeight.Normal, fontSize = 14.sp
                    )
                )

                Text(
                    "665246", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Approval", style = AppTheme.typography.footnote.copy(
                        fontWeight = FontWeight.Normal, fontSize = 14.sp
                    )
                )

                Text(
                    "305927", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Aggregator", style = AppTheme.typography.footnote.copy(
                        fontWeight = FontWeight.Normal, fontSize = 14.sp
                    )
                )

                Text(
                    "AMEX", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        AssistChip(
            modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            onClick = { },
            label = {
                Text(
                    "Refunded amount will be transferred to the original source of payment",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = purple50
                )
            },
            border = noBorder,
            colors = AssistChipDefaults.assistChipColors(
                containerColor = Color.LightGray.copy(0.2f)
            ),
            leadingIcon = {
                Icon(
                    Icons.TwoTone.Lightbulb,
                    contentDescription = "Hint",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                )
            })

        Spacer(modifier = Modifier.height(20.dp))

        FilledButton(
            text = "Initiate Refund",
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
        )
    }
}