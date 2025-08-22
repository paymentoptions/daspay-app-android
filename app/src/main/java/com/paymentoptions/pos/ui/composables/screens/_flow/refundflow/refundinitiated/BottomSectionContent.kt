package com.paymentoptions.pos.ui.composables.screens._flow.refundflow.refundinitiated

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.R
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.NoteChip
import com.paymentoptions.pos.ui.composables._components.ProgressState
import com.paymentoptions.pos.ui.composables._components.VerticalProgressBar
import com.paymentoptions.pos.ui.composables._components.buttons.Email
import com.paymentoptions.pos.ui.composables._components.buttons.EmailButton
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.buttons.ScanButton
import com.paymentoptions.pos.ui.composables._components.buttons.ShareButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.screens.dashboard.TRANSACTION_TO_BE_REFUNDED
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.containerBackgroundGradientBrush
import com.paymentoptions.pos.ui.theme.green500
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.modifiers.conditional
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Date

@Composable
fun BottomSectionContent(
    navController: NavController,
    transaction: TransactionListDataRecord? = TRANSACTION_TO_BE_REFUNDED,
    enableScrolling: Boolean = false,
) {
    val currency = "HKD"
    var showRefundStatus by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val dateString =
        transaction?.Date ?: OffsetDateTime.now().toString()  //"2025-04-23T03:38:57.349+00:00"
    val dateTime = OffsetDateTime.parse(dateString)
    val date: Date = Date.from(dateTime.toInstant())
    val dateStringFormatted: String = SimpleDateFormat("dd MMMM YYYY").format(date)

    LaunchedEffect(showRefundStatus) {
        scrollState.scrollTo(if (showRefundStatus) scrollState.maxValue else 0)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Spacer(modifier = Modifier.height(10.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = "Refund Initiated",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = green500,
            )

            Spacer(modifier = Modifier.height(8.dp))
            CurrencyText(currency = currency, amount = transaction?.amount.toString(), fontWeight = FontWeight(980))

            Spacer(modifier = Modifier.height(8.dp))

            FilledButton(
                text = "View Full Receipt",
                onClick = { },
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .width(200.dp)
                    .scale(0.7f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = containerBackgroundGradientBrush, shape = RoundedCornerShape(20.dp)
                )
                .padding(vertical = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .conditional(enableScrolling) { verticalScroll(scrollState) },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            NoteChip(
                text = "The refund is being processed and should be reflected in your account within 3–5 business days",
                modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                fontWeight = FontWeight.W600
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Trace", style = AppTheme.typography.footnote.copy(
                            fontWeight = FontWeight.Normal, fontSize = 14.sp
                        )
                    )

                    Text(
                        "665246",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Approval", style = AppTheme.typography.footnote.copy(
                            fontWeight = FontWeight.Normal, fontSize = 14.sp
                        )
                    )

                    Text(
                        "305927",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                color = Color.LightGray.copy(alpha = 0.2f)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Share with Others",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primary900,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EmailButton(
                        text = "Email", email = Email(), modifier = Modifier
                            .weight(1f)
                            .border(
                                2.dp,
                                color = primary100.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(Color.White)
                            .padding(horizontal = 10.dp, vertical = 20.dp)
                    )

                    ShareButton(
                        text = "Share", modifier = Modifier
                            .weight(1f)
                            .border(
                                2.dp,
                                color = primary100.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(Color.White)
                            .padding(horizontal = 10.dp, vertical = 20.dp)
                    )

                    ScanButton(
                        text = "Scan", modifier = Modifier
                            .weight(1f)
                            .border(
                                2.dp,
                                color = primary100.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(Color.White)
                            .padding(horizontal = 10.dp, vertical = 20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                AssistChip(
                    onClick = { showRefundStatus = !showRefundStatus }, label = {
                        Text(
                            text = "View Refund Status",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primary500
                        )
                    }, border = borderThin, colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color.Transparent,
                    ), trailingIcon = {
                        Icon(
                            painterResource(R.drawable.down_arrow),
                            contentDescription = "Hint",
                            tint = primary500,
                            modifier = Modifier
                                .size(AssistChipDefaults.IconSize.minus(5.dp))
                                .conditional(showRefundStatus) { rotate(180f) })
                    })


                if (showRefundStatus) {
                    VerticalProgressBar(
                        currentState = ProgressState.PROCESSING,
                        submittedTitle = "Request Submitted",
                        submittedText = "We've received your refund request.\nHang tight—we're on it!",
                        initiatedTitle = "Refund Pending",
                        initiatedText = "Your refund has been initiated. We're now working with your payment provider.",
                        processingTitle = "Refund Processing",
                        processingText = "Your refund is being processed. It usually takes 3–5 business days to complete.",
                        completedTitle = "Refund Completed",
                        completedText = "Success! The refund has been processed. You should see it in your account shortly.",
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            }
        }
    }
}