package com.paymentoptions.pos.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.formatDate
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.PaymentStatusRequest
import com.paymentoptions.pos.network.RefundResponse
import com.paymentoptions.pos.network.TransactionListDataRecord
import com.paymentoptions.pos.network.endpoints.refund
import com.paymentoptions.pos.network.endpoints.sendWebHookNotification
import com.paymentoptions.pos.network.endpoints.void
import com.paymentoptions.pos.showToast
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.images.BackgroundImage
import com.paymentoptions.pos.ui.composables._components.images.LogoImage
import com.paymentoptions.pos.ui.composables._components.inputs.BasicTextInput
import com.paymentoptions.pos.ui.composables.layout.sectioned.LOGO_HEIGHT_IN_DP
import com.paymentoptions.pos.ui.composables.layout.sectioned.LOGO_TOP_PADDING_IN_DP
import com.paymentoptions.pos.ui.navigation.Screens
import com.paymentoptions.pos.ui.screens.status.MessageForStatusScreen
import com.paymentoptions.pos.ui.screens.status.StatusScreen
import com.paymentoptions.pos.ui.screens.status.StatusScreenType
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.utils.TransactionAction
import com.paymentoptions.pos.utils.parseApiErrorMessage
import com.paymentoptions.pos.utils.safeParseDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.ic_warning
import kotlin.time.Duration.Companion.seconds

// ── Platform seam ─────────────────────────────────────────────────────────────

/**
 * Abstracts the platform-specific MineSec SDK launch + PaymentStatusRequest
 * construction. The common screen calls these after a successful API response;
 * each platform implementation handles SDK launch and calls back with the
 * ready-to-send PaymentStatusRequest (or an error message).
 */
interface MineSecActionDispatcher {
    fun launchVoid(
        acquirerTransactionId: String,
        parentUUID: String,
        childUUID: String,
        notes: String,
        onReady: (PaymentStatusRequest) -> Unit,
        onFailure: (String) -> Unit,
    )

    fun launchRefund(
        acquirerTransactionId: String,
        parentUUID: String,
        childUUID: String,
        amount: String,
        currency: String,
        notes: String,
        onReady: (PaymentStatusRequest) -> Unit,
        onFailure: (String) -> Unit,
    )
}

@Composable
expect fun rememberMineSecActionDispatcher(): MineSecActionDispatcher

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionActionScreen(
    navController: NavController,
    transaction: TransactionListDataRecord,
    targetAction: TransactionAction,
) {
    val dispatcher = rememberMineSecActionDispatcher()

    var showBottomSheet by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var processingScreenType by remember { mutableStateOf(StatusScreenType.PROCESSING) }
    var processingMessage by remember { mutableStateOf("Processing...") }
    val delayTime = 5.seconds
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateStringFormatted = formatDate(safeParseDateTime(transaction.Date), "dd MMMM, yyyy")
    val notesInput = remember { TextFieldState() }

    fun showTransactionFailure() {
        CoroutineScope(Dispatchers.Default).launch {
            withContext(Dispatchers.Main) {
                processingScreenType = StatusScreenType.ERROR
                processingMessage =
                    if (targetAction == TransactionAction.VOID) "Void Failed" else "Refund Failed"
            }
            AppLogger.debug("StatusScreen: Showing ERROR screen for 5 seconds")

            // Wait 5 seconds
            delay(delayTime)
            AppLogger.debug("StatusScreen: 5 seconds elapsed, closing screen")

            withContext(Dispatchers.Main) {
                navController.popBackStack()
                showToast(errorMessage)
            }
        }
    }

    fun sendWebhookAndNavigate(request: PaymentStatusRequest, actionLabel: String, childUUID: String) {
        CoroutineScope(Dispatchers.Default).launch {
            val receiptRoute =
                "${Screens.TransactionReceipt.route}?transactionId=$childUUID" +
                "&title=Your transaction is ${actionLabel}ed&amount=${transaction.amount}&refrenceId=$childUUID" +
                "&aggregator=${transaction.Scheme}&dateString=${transaction.Date}"
            try {
                withContext(Dispatchers.Main) {
                    processingScreenType = StatusScreenType.PROCESSING
                    processingMessage = "Processing $actionLabel..."
                }
                sendWebHookNotification(request = request)

                if(request.tranStatus == "APPROVED"){
                    withContext(Dispatchers.Main) {
                        processingScreenType = StatusScreenType.SUCCESS
                        processingMessage = "$actionLabel Completed"
                    }
                } else {
                    // Webhook failure doesn't block navigation — action already succeeded
                    withContext(Dispatchers.Main) {
                       showTransactionFailure()
                    }
                }

            } catch (e: Exception) {
                AppLogger.error("Webhook failed for $actionLabel: ${e.message}", e)
                // Webhook failure doesn't block navigation — action already succeeded
                if(request.tranStatus == "APPROVED"){
                    withContext(Dispatchers.Main) {
                        processingScreenType = StatusScreenType.SUCCESS
                        processingMessage = "$actionLabel Completed"
                    }
                } else {
                    AppLogger.error("Webhook failed for $actionLabel: ${e.message}", e)
                    // Webhook failure doesn't block navigation — action already succeeded
                    withContext(Dispatchers.Main) {
                        showTransactionFailure()
                    }
                }
            }
            delay(delayTime)
            withContext(Dispatchers.Main) {
                navController.navigate(receiptRoute) {
                    popUpTo(Screens.TransactionAction.route) { inclusive = true }
                }
            }
        }
    }

    fun doVoid(transaction: TransactionListDataRecord) {
        AppLogger.debug("doVoid: $transaction")
        processingScreenType = StatusScreenType.PROCESSING
        processingMessage = "Processing Void..."

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val response = void(
                    transactionId = transaction.uuid,
                    merchantId = transaction.DASMID,
                )
                if (response != null) {
                    val childUUID = response.transaction_details.id
                    AppLogger.debug("Void API success, childUUID=$childUUID")
                    withContext(Dispatchers.Main) {
                        dispatcher.launchVoid(
                            acquirerTransactionId = transaction.AcquirerTransactionID ?: "",
                            parentUUID = transaction.uuid,
                            childUUID = childUUID,
                            notes = notesInput.text.toString(),
                            onReady = { request ->
                                sendWebhookAndNavigate(request, "Void", childUUID)
                            },
                            onFailure = { msg ->
                                errorMessage = msg
                                showTransactionFailure()
                            },
                        )
                    }
                } else {
                    AppLogger.error("Void API returned null")
                    withContext(Dispatchers.Main) {
                        errorMessage = "Void request failed"
                        showTransactionFailure()
                    }
                }
            } catch (e: Exception) {
                AppLogger.error("Void API failed: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    errorMessage = parseApiErrorMessage(e, "Void failed")
                    showTransactionFailure()
                }
            }
        }
    }

    fun doRefund(transaction: TransactionListDataRecord) {
        AppLogger.debug("doRefund: $transaction")

        when (transaction.ProductType) {
            "SOFTPOS" -> {
                processingScreenType = StatusScreenType.PROCESSING
                processingMessage = "Processing Refund..."

                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        val response = refund(
                            transactionId = transaction.uuid,
                            merchantId = transaction.DASMID,
                            amount = transaction.amount,
                            notes = notesInput.text.toString().ifBlank { "Refund from DASPay App" },
                        )
                        if (response != null) {
                            val childUUID = response.transaction_details.id
                            AppLogger.debug("Refund API success, childUUID=$childUUID")
                            withContext(Dispatchers.Main) {
                                dispatcher.launchRefund(
                                    acquirerTransactionId = transaction.AcquirerTransactionID ?: "",
                                    parentUUID = transaction.uuid,
                                    childUUID = childUUID,
                                    amount = transaction.amount,
                                    currency = DPStorageManager.getTransactionCurrency(),
                                    notes = notesInput.text.toString(),
                                    onReady = { request ->
                                        sendWebhookAndNavigate(request, "Refund", childUUID)
                                    },
                                    onFailure = { msg ->
                                        errorMessage = msg
                                        showTransactionFailure()
                                    },
                                )
                            }
                        } else {
                            AppLogger.error("Refund API returned null")
                            withContext(Dispatchers.Main) {
                                errorMessage = "Refund request failed"
                                showTransactionFailure()
                            }
                        }
                    } catch (e: Exception) {
                        AppLogger.error("Refund API failed: ${e.message}", e)
                        withContext(Dispatchers.Main) {
                            errorMessage = parseApiErrorMessage(e, "Refund failed")
                            showTransactionFailure()
                        }
                    }
                }
            }

            "PBL", "QR" -> {
                // No MineSec for PBL/QR — call API directly and navigate
                processingScreenType = StatusScreenType.PROCESSING
                processingMessage = "Processing Refund..."

                CoroutineScope(Dispatchers.Default).launch {
                    val maxRetries = 1
                    var attempt = 0
                    var response: RefundResponse? = null
                    var lastError: Exception? = null

                    while (attempt < maxRetries && response == null) {
                        try {
                            attempt++
                            AppLogger.debug("Refund attempt $attempt of $maxRetries")
                            response = refund(
                                transactionId = transaction.uuid,
                                merchantId = transaction.DASMID,
                                amount = transaction.amount,
                                notes = notesInput.text.toString().ifBlank { "Refund from DASPay App" },
                            )
                        } catch (e: Exception) {
                            AppLogger.error("Refund attempt $attempt failed: ${e.message}", e)
                            lastError = e
                            errorMessage = parseApiErrorMessage(e, "Refund Failed")
                            if (attempt < maxRetries) delay(1000L * attempt)
                        }
                    }

                    if (response != null) {
                        val id = response.transaction_details.id
                        withContext(Dispatchers.Main) {
                            processingScreenType = StatusScreenType.SUCCESS
                            processingMessage = "Refund Completed"
                        }
                        delay(delayTime)
                        withContext(Dispatchers.Main) {
                            navController.navigate(
                                "${Screens.TransactionReceipt.route}?transactionId=$id" +
                                "&title=Your transaction is Refunded&amount=${transaction.amount}&refrenceId=$id" +
                                "&aggregator=${transaction.Scheme}&dateString=${transaction.Date}"
                            ) { popUpTo(Screens.TransactionAction.route) { inclusive = true } }
                        }
                    } else {
                        AppLogger.error("Refund failed after $maxRetries attempts: ${lastError?.message}")
                        withContext(Dispatchers.Main) {
                            processingScreenType = StatusScreenType.ERROR
                            processingMessage = "Refund failed"
                        }
                        delay(delayTime)
                        withContext(Dispatchers.Main) {
                            navController.popBackStack()
                            showToast(errorMessage)
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundImage(modifier = Modifier.fillMaxSize())
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = LOGO_TOP_PADDING_IN_DP)
            ) {
                LogoImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(LOGO_HEIGHT_IN_DP)
                )
            }
        }

        ModalBottomSheet(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.White,
            contentColor = primary500,
            onDismissRequest = {
                showBottomSheet = false
                navController.popBackStack()
            },
            sheetState = sheetState,
            dragHandle = {},
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Top,
                ) {
                    IconButton(onClick = {
                        showBottomSheet = false
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = primary900)
                    }
                }

                Text(
                    text = when (targetAction) {
                        TransactionAction.VOID -> "Void Transaction"
                        TransactionAction.REFUND -> "Refund Transaction"
                        else -> "Transaction Action"
                    },
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = primary900,
                )

                CurrencyText(currency = transaction.CurrencyCode, amount = transaction.amount)

                if (targetAction == TransactionAction.REFUND) {
                    BasicTextInput(state = notesInput, placeholder = "Notes")
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Reference No.", fontSize = 12.sp, color = purple50, fontWeight = FontWeight.Normal)
                        Text(transaction.uuid, fontSize = 12.sp, color = primary500, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Date", fontSize = 12.sp, color = purple50, fontWeight = FontWeight.Normal)
                        Text(dateStringFormatted, fontSize = 12.sp, color = primary500, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Aggregator", fontSize = 12.sp, color = purple50, fontWeight = FontWeight.Normal)
                        Text(transaction.Scheme, fontSize = 12.sp, color = primary500, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_warning),
                        contentDescription = "Warning",
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (targetAction) {
                            TransactionAction.VOID -> "Are you sure you want to void this transaction? This process can not be undone."
                            TransactionAction.REFUND -> "Are you sure you want to refund this transaction? This process can not be undone."
                            else -> ""
                        },
                        fontSize = 13.sp,
                        color = Color.Red.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Card(
                        modifier = Modifier.weight(1f).clickable {
                            showBottomSheet = false
                            navController.popBackStack()
                        },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFBDBDBD)),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("CANCEL", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f).clickable {
                            showBottomSheet = false
                            when (targetAction) {
                                TransactionAction.VOID -> doVoid(transaction)
                                TransactionAction.REFUND -> doRefund(transaction)
                                else -> {}
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = primary500),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = when (targetAction) {
                                    TransactionAction.VOID -> "VOID"
                                    TransactionAction.REFUND -> "REFUND"
                                    else -> "CONFIRM"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp,
                            )
                        }
                    }
                }
            }
        }
    } else {
        StatusScreen(
            navController,
            MessageForStatusScreen(text = processingMessage, statusScreenType = processingScreenType),
            strategyFn = {},
        )
    }
}
