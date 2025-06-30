package com.paymentoptions.pos.ui.composables._components

import CustomDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.services.apiService.endpoints.refund
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.util.Date


@Composable
fun TransactionCard(
    transaction: TransactionListDataRecord,
    exitToLoginScreen: () -> Unit,
    updateState: () -> Unit,
) {
    val context = LocalContext.current
    val dateString = transaction.Date   //"2025-04-23T03:38:57.349+00:00"
    val dateTime = OffsetDateTime.parse(dateString)
    val date: Date = Date.from(dateTime.toInstant())
    var showRefundConfirmationDialog by remember { mutableStateOf(false) }
    var showRefundResponseSuccessfulDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var showRefundLoader by remember { mutableStateOf(false) }
    var refundButtonText by remember { mutableStateOf("Refund") }
    var transactionState = remember { mutableStateOf<TransactionListDataRecord>(transaction) }

    CustomDialog(
        showDialog = showRefundResponseSuccessfulDialog,
        title = "Refund Success",
        text = "Amount of ${transactionState.value.amount} was refunded successfully",
        acceptButtonText = "OK",
        showCancelButton = false,
        onAccept = { showRefundResponseSuccessfulDialog = false },
        onDismiss = { showRefundResponseSuccessfulDialog = false },
    )

    CustomDialog(
        showDialog = showRefundConfirmationDialog,
        title = "Refund Confirmation",
        text = "Are you sure you want to initiate refund for the transaction?",
        acceptButtonText = "Confirm",
        cancelButtonText = "Cancel",
        onAccept = {
            scope.launch {
                showRefundLoader = true

                try {
                    var refundResponse = refund(
                        context,
                        transactionState.value.uuid,
                        transactionState.value.DASMID,
                        transactionState.value.amount.toFloat()
                    )
                    if (refundResponse == null) {
//                    exitToLoginScreen()
                    }

                    refundResponse?.let {
                        if (refundResponse.success) {
                            transactionState.value.status == "REFUND"
                            showRefundResponseSuccessfulDialog = true
                        }
                    }
                } catch (e: Exception) {
                    println("catch --> $e")
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG)
                        .show()
                } finally {
                    showRefundLoader = false
                }

            }
            showRefundConfirmationDialog = false

        },
        onDismiss = { showRefundConfirmationDialog = false },
    )

    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 100.dp)
    ) {

        SelectionContainer {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {

                SuggestionChip(
                    onClick = {
                    },
                    label = {

                        var text =
                            if (transactionState.value.TransactionType.uppercase() == "REFUND") "REFUNDED" else transactionState.value.status

                        if (text == "REFUND")
                            println("tet: $text")

                        Text(
                            text = text,
                            fontSize = 8.sp
                        )
                    },
                    modifier = Modifier
                        .height(20.dp)
                        .align(alignment = Alignment.End),
                )

                Text(
                    text = "Transaction ID: " + transactionState.value.uuid,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 20.dp)
                )

                Text(
                    text = "Merchant ID: " + transactionState.value.DASMID,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 20.dp)
                )

                Text(
                    text = "Card: ${transactionState.value.CardNumber}",
                    fontSize = 10.sp,
                )

                Text(
                    text = "Amount: ${transactionState.value.CurrencyCode} ${transactionState.value.amount}",
                    fontSize = 10.sp,
                )

                Text(
                    text = "Date: $date",
                    fontSize = 10.sp,
                )

                if (transactionState.value.status.toString()
                        .uppercase() == "SUCCESSFUL" && transactionState.value.TransactionType.uppercase() == "PURCHASE"
                )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(top = 20.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = !showRefundLoader) {
                                showRefundConfirmationDialog = true
                            },
                        contentAlignment = Alignment.Center,

                        ) {
                        if (showRefundLoader)
                            CustomCircularProgressIndicator()
                        else
                            Text(
                                refundButtonText,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                    }
            }
        }
    }
}
