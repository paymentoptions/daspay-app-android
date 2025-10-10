package com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow.transactionfailed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.device.getTransactionCurrency
import com.paymentoptions.pos.services.apiService.PaymentDetailsResponse
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.ScreenTitleWithCloseButton
import com.paymentoptions.pos.ui.composables._components.buttons.Email
import com.paymentoptions.pos.ui.composables._components.buttons.EmailButton
import com.paymentoptions.pos.ui.composables._components.buttons.ScanButton
import com.paymentoptions.pos.ui.composables._components.buttons.ShareButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow.ReceiveMoneyFlowStage
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.containerBackgroundGradientBrush
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.ui.theme.red500
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Date

@Composable
fun TransactionFailedBottomSectionContent(
    navController: NavController,
    paymentDetailsResponse: PaymentDetailsResponse?,
    enableScrolling: Boolean = false,
    amountToCharge: String,
    updateFlowStage: (ReceiveMoneyFlowStage) -> Unit = {},
) {
    val context = LocalContext.current
    val currency = getTransactionCurrency(context)
    val dateString =
        paymentDetailsResponse?.data?.Date ?: OffsetDateTime.now()
            .toString()  //"2025-04-23T03:38:57.349+00:00"
    val dateTime = OffsetDateTime.parse(dateString)
    val date: Date = Date.from(dateTime.toInstant())
    val formattedDate = SimpleDateFormat("dd MMMM YYYY").format(date)

    //Sharable text summary for the failed Transaction
    val shareableFailureText = if (paymentDetailsResponse != null) {
        "Details for failed transaction #${paymentDetailsResponse.data.TransactionID}\nAmount: $amountToCharge $currency\nDate: $formattedDate\nStatus: FAILED"
    } else {
        "Transaction failed. Details are unavailable."
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
            .padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        ScreenTitleWithCloseButton(
            navController = navController,
            fontSize = 8.sp,
            onClose = { navController.navigate(Screens.Dashboard) })

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = 20.dp.times(-1))
        ) {

            Text(
                text = "Transaction Failed",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = red500,
            )

            Spacer(modifier = Modifier.height(8.dp))
            CurrencyText(currency = currency, amount = amountToCharge)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = containerBackgroundGradientBrush, shape = RoundedCornerShape(20.dp)
                )
                .verticalScroll(state = rememberScrollState(), enabled = enableScrolling),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Spacer(modifier = Modifier.height(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
                        paymentDetailsResponse?.data?.TransactionID.toString(),
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
                        formattedDate,
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
                        "null", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500
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
                        "null", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                        text = "Email",
                        email = Email(
                            subject = "DASPay Transaction Failure Details",
                            text = shareableFailureText
                        ),
                        modifier = Modifier
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
                        text = "Share",
                        shareContent = shareableFailureText,
                        modifier = Modifier
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

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}