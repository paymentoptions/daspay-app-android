package com.paymentoptions.pos.ui.composables.screens._flow.refundflow.refundTransaction

import android.os.Handler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.services.apiService.RefundRequest
import com.paymentoptions.pos.services.apiService.RefundResponse
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.services.apiService.endpoints.refund
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.NoteChip
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.inputs.DashedBorderInput
import com.paymentoptions.pos.ui.composables._components.screentitle.ScreenTitleWithCloseButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.composables.screens.dashboard.TRANSACTION_TO_BE_REFUNDED
import com.paymentoptions.pos.ui.composables.screens.status.StatusScreenType
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Date

@Composable
fun BottomSectionContent(
    navController: NavController,
    transaction: TransactionListDataRecord? = TRANSACTION_TO_BE_REFUNDED,
    enableScrolling: Boolean = false,
    scope: CoroutineScope,
    updateRefundStatus: (StatusScreenType?) -> Unit = {},

    ) {
    val context = LocalContext.current
    val currency = "HKD"
    val dateString =
        transaction?.Date ?: OffsetDateTime.now().toString()  //"2025-04-23T03:38:57.349+00:00"
    val dateTime = OffsetDateTime.parse(dateString)
    val date: Date = Date.from(dateTime.toInstant())
    val dateStringFormatted: String = SimpleDateFormat("dd MMMM YYYY").format(date)
    val noteState = rememberTextFieldState()
    var isLoading by remember { mutableStateOf(false) }

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
            CurrencyText(currency = currency, amount = transaction?.amount.toString(), fontWeight = FontWeight(980))
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

        NoteChip(
            text = "Refunded amount will be transferred to the original source of payment",
            modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            fontWeight = FontWeight.W600
        )

        Spacer(modifier = Modifier.height(20.dp))

        FilledButton(
            text = "Initiate Refund",
            onClick = {
                scope.launch {
                    isLoading = true

                    var refundResponse: RefundResponse? = null

                    try {
                        updateRefundStatus(StatusScreenType.PROCESSING)

                        val refundRequest = RefundRequest(
                            id = transaction?.uuid.toString(),
                            merchant_id = transaction?.DASMID.toString(),
                            refundAmount = transaction?.amount?.toFloat() ?: 0.0f,
//                            note = noteState.text
                        )

                        refundResponse = refund(context = context, refundRequest = refundRequest)

                        if (refundResponse == null) throw Exception("Error processing refund")
                        else {
//                            Toast.makeText(
//                                context, "Refund processed successfully", Toast.LENGTH_LONG
//                            ).show()
                            updateRefundStatus(StatusScreenType.SUCCESS)

                            Handler().postDelayed({
                                navController.navigate(Screens.RefundInitiated.route)
                            }, 2000)

                        }
                    } catch (e: Exception) {
//                        Toast.makeText(context, "Error processing refund", Toast.LENGTH_LONG).show()
                        updateRefundStatus(StatusScreenType.ERROR)
                        Handler().postDelayed({
                            navController.navigate(Screens.RefundInitiated.route)
                        }, 2000)
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(59.dp)
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
        )
    }
}