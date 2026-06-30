package com.paymentoptions.pos.ui.screens.dashboard
import paymentoptionspos.shared.generated.resources.ic_warning

import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res

import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.RefundResponse
import com.paymentoptions.pos.network.TransactionListDataRecord
import com.paymentoptions.pos.network.endpoints.paymentStatus
import com.paymentoptions.pos.network.endpoints.refund
import com.paymentoptions.pos.network.endpoints.void
import com.paymentoptions.pos.payment.MineSecPlatform
import com.paymentoptions.pos.payment.MineSecTransactionResult
import com.paymentoptions.pos.payment.PaymentStatusMapper
import com.paymentoptions.pos.showToast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.formatDate
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun TransactionActionScreen(
    navController: NavController,
    transaction: TransactionListDataRecord,
    targetAction: TransactionAction
) {
    val provider = MineSecPlatform.paymentProvider

    var showBottomSheet by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var processingScreenType by remember { mutableStateOf(StatusScreenType.PROCESSING) }
    var processingMessage by remember { mutableStateOf("Processing...") }
    val delayTime = 5000L
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateTime = safeParseDateTime(transaction.Date)
    val dateStringFormatted = formatDate(dateTime, "dd MMMM, yyyy")
    val notesInput = remember { TextFieldState() }

    fun showTransactionFailure() {
        CoroutineScope(Dispatchers.Default).launch {
            // Show error screen on Main thread
            withContext(Dispatchers.Main) {
                processingScreenType = StatusScreenType.ERROR
                processingMessage =
                    if (targetAction == TransactionAction.VOID) "Void Failed" else "Refund Failed"
            }
            AppLogger.debug("StatusScreen: Showing ERROR screen for 5 seconds")

            // Wait 5 seconds
            delay(delayTime)
            AppLogger.debug("StatusScreen: 5 seconds elapsed, closing screen")

            // Call refresh and close on main thread
            withContext(Dispatchers.Main) {
                navController.popBackStack()
                showToast(errorMessage)
            }
        }
    }

    // Stores the childUUID from the void/refund API response
    var refundChildUUID by remember { mutableStateOf<String?>(null) }
    var voidChildUUID by remember { mutableStateOf<String?>(null) }

    fun sendWebhookNotification(
        sdkResult: MineSecTransactionResult,
        parentUUID: String,
        childUUID: String,
        actionLabel: String,
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                withContext(Dispatchers.Main) {
                    processingScreenType = StatusScreenType.PROCESSING
                    processingMessage = "Processing $actionLabel..."
                }

                val paymentStatusRequest = PaymentStatusMapper.fromMineSecResult(
                    posReference = childUUID,
                    result = sdkResult,
                    amount = transaction.amount,
                    currency = transaction.CurrencyCode,
                    parentUUID = parentUUID,
                    childUUID = childUUID,
                )


                AppLogger.debug("Webhook request for $actionLabel: parentUUID=$parentUUID, childUUID=$childUUID and paymentStatusRequest: $paymentStatusRequest")

                val webhookSuccess = paymentStatus(
                    request = paymentStatusRequest,
                ) != null

                AppLogger.debug("Webhook response for $actionLabel: $webhookSuccess")

                // Show success
                withContext(Dispatchers.Main) {
                    processingScreenType = StatusScreenType.SUCCESS
                    processingMessage = "$actionLabel Completed"
                }
                delay(delayTime)

                withContext(Dispatchers.Main) {
                    navController.navigate(
                        "${Screens.TransactionReceipt.route}?transactionId=$childUUID" +
                                "&title=Your transaction is ${actionLabel}ed&amount=${transaction.amount}&refrenceId=$childUUID" +
                                "&aggregator=${transaction.Scheme}&dateString=${transaction.Date}"
                    ) {
                        popUpTo(Screens.TransactionAction.route) { inclusive = true }
                    }
                }
            } catch (e: Exception) {
                AppLogger.error("Webhook notification failed for $actionLabel: ${e.message}", e)
                // Still show success since the void/refund API already succeeded
                withContext(Dispatchers.Main) {
                    processingScreenType = StatusScreenType.SUCCESS
                    processingMessage = "$actionLabel Completed"
                }
                delay(delayTime)
                withContext(Dispatchers.Main) {
                    navController.navigate(
                        "${Screens.TransactionReceipt.route}?transactionId=$childUUID" +
                                "&title=Your transaction is ${actionLabel}ed&amount=${transaction.amount}&refrenceId=$childUUID" +
                                "&aggregator=${transaction.Scheme}&dateString=${transaction.Date}"
                    ) {
                        popUpTo(Screens.TransactionAction.route) { inclusive = true }
                    }
                }
            }
        }
    }

    fun updateRefundTransaction(sdkResult: MineSecTransactionResult?) {
        if (sdkResult != null && refundChildUUID != null) {
            sendWebhookNotification(
                sdkResult = sdkResult,
                parentUUID = transaction.uuid,
                childUUID = refundChildUUID!!,
                actionLabel = "Refund",
            )
            return
        }

        CoroutineScope(Dispatchers.Default).launch {
            val maxRetries = 1
            var currentAttempt = 0
            var lastError: Exception? = null
            var response: RefundResponse? = null

            withContext(Dispatchers.Main) {
                processingScreenType = StatusScreenType.PROCESSING
                processingMessage = "Processing Refund..."
            }

            while (currentAttempt < maxRetries && response == null) {
                try {
                    currentAttempt++
                    AppLogger.debug("Refund attempt $currentAttempt of $maxRetries")

                    response = refund(
                        transactionId = transaction.uuid,
                        merchantId = transaction.DASMID,
                        amount = transaction.amount,
                        notes = notesInput.text.toString().ifBlank { "Refund from DASPay App" }
                    )

                    if (response != null) {
                        AppLogger.debug("Refund successful on attempt $currentAttempt: $response")
                        break
                    } else {
                        lastError = Exception("API returned null response")
                    }
                } catch (e: Exception) {
                    AppLogger.error("Refund attempt $currentAttempt failed: ${e.message}", e)
                    lastError = e

                    errorMessage = parseApiErrorMessage(e,"Refund Failed")
                    if (currentAttempt < maxRetries) {
                        delay(1000L * currentAttempt)
                    }
                }
            }

            if (response != null) {
                withContext(Dispatchers.Main) {
                    processingScreenType = StatusScreenType.SUCCESS
                    processingMessage = "Refund Completed"
                }
                delay(delayTime)
                withContext(Dispatchers.Main) {
                    navController.navigate(
                        "${Screens.TransactionReceipt.route}?transactionId=${response.transaction_details.id}" +
                                "&title=Your transaction is Refunded&amount=${transaction.amount}&refrenceId=${response.transaction_details.id}" +
                                "&aggregator=${transaction.Scheme}&dateString=${transaction.Date}"
                    ) {
                        popUpTo(Screens.TransactionAction.route) { inclusive = true }
                    }
                }
            } else {
                AppLogger.error("Refund failed after $maxRetries attempts. Last error: ${lastError?.message}")
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

    fun updateVoidTransaction(sdkResult: MineSecTransactionResult?) {
        if (sdkResult != null && voidChildUUID != null) {
            sendWebhookNotification(
                sdkResult = sdkResult,
                parentUUID = transaction.uuid,
                childUUID = voidChildUUID!!,
                actionLabel = "Void",
            )
        } else {
            AppLogger.error("updateVoidTransaction called without sdkResult or voidChildUUID")
            showTransactionFailure()
        }
    }

    fun handleMineSecResult(result: MineSecTransactionResult) {
        if (result.success) {
            if (result.tranType == "REFUND") {
                errorMessage = "Sorry could not be refunded"
                updateRefundTransaction(result)
            } else {
                errorMessage = "Sorry could not be voided"
                updateVoidTransaction(result)
            }
        } else {
            errorMessage = result.errorMessage ?: "Transaction action failed"
            showTransactionFailure()
        }
    }

    fun doVoid(transaction: TransactionListDataRecord) {
        AppLogger.debug("full transaction object: $transaction")
        processingScreenType = StatusScreenType.PROCESSING
        processingMessage = "Processing Void..."

        // Step 1: Call void API first (without daspay_res)
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val response = void(
                    transactionId = transaction.uuid,
                    merchantId = transaction.DASMID,
                )

                if (response != null) {
                    AppLogger.debug("Void API successful, childUUID: ${response.transaction_details.id}")
                    withContext(Dispatchers.Main) {
                        voidChildUUID = response.transaction_details.id
                        val acquirerId = transaction.AcquirerTransactionID
                        if (provider == null || acquirerId.isNullOrBlank()) {
                            errorMessage = "MineSec SDK not available for void"
                            showTransactionFailure()
                        } else {
                            provider.launchVoid(
                                acquirerTransactionId = acquirerId,
                                profileId = MineSecPlatform.PROFILE_ID,
                                onResult = { result ->
                                    handleMineSecResult(result)
                                },
                            )
                        }
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
        AppLogger.debug("full transaction object: $transaction")

        when (transaction.ProductType) {
            "SOFTPOS" -> {
                processingScreenType = StatusScreenType.PROCESSING
                processingMessage = "Processing Refund..."

                // Step 1: Call refund API first (without daspay_res)
                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        val response = refund(
                            transactionId = transaction.uuid,
                            merchantId = transaction.DASMID,
                            amount = transaction.amount,
                            notes = notesInput.text.toString().ifBlank { "Refund from DASPay App" }
                        )

                        if (response != null) {
                            AppLogger.debug("Refund API successful, childUUID: ${response.transaction_details.id}")
                            withContext(Dispatchers.Main) {
                                refundChildUUID = response.transaction_details.id
                                val acquirerId = transaction.AcquirerTransactionID
                                if (provider == null || acquirerId.isNullOrBlank()) {
                                    errorMessage = "MineSec SDK not available for refund"
                                    showTransactionFailure()
                                } else {
                                    provider.launchLinkedRefund(
                                        acquirerTransactionId = acquirerId,
                                        amount = transaction.amount,
                                        currency = DPStorageManager.getTransactionCurrency(),
                                        profileId = MineSecPlatform.PROFILE_ID,
                                        onResult = { result ->
                                            handleMineSecResult(result)
                                        },
                                    )
                                }
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
                // No MineSec involved for PBL/QR, call API directly
                updateRefundTransaction(null)
            }
        }
    }

    // Bottom Sheet for Refund/Void Operations
    if (showBottomSheet) {

        Box(modifier = Modifier
            .fillMaxSize()
        ) {
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
                showBottomSheet = !showBottomSheet
                navController.popBackStack()
            },
            sheetState = sheetState,
            dragHandle = {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Top
                ) {
                    IconButton(onClick = {
                        showBottomSheet = false
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = primary900
                        )
                    }
                }

                // Title
                Text(
                    text = when (targetAction) {
                        TransactionAction.VOID -> "Void Transaction"
                        TransactionAction.REFUND -> "Refund Transaction"
                        else -> "Transaction Action"
                    },
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = primary900,
                    modifier = Modifier.padding(top = 0.dp)
                )

                // Amount in large blue text
                CurrencyText(
                    currency = transaction.CurrencyCode,
                    amount = transaction.amount
                )

                // A notes can be sent along with refund
                if(targetAction == TransactionAction.REFUND){
                    BasicTextInput(
                        state = notesInput,
                        placeholder = "Notes",
                    )
                }

                // Transaction Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Reference No.",
                            fontSize = 12.sp,
                            color = purple50,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = transaction.uuid,
                            fontSize = 12.sp,
                            color = primary500,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Date",
                            fontSize = 12.sp,
                            color = purple50,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = dateStringFormatted,
                            fontSize = 12.sp,
                            color = primary500,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Aggregator",
                            fontSize = 12.sp,
                            color = purple50,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = transaction.Scheme,
                            fontSize = 12.sp,
                            color = primary500,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Warning message with icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.LightGray.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                       .padding(6.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    // Warning icon
                    Icon(
                        painter = painterResource(Res.drawable.ic_warning),
                        contentDescription = "Warning",
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(24.dp)
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
                        lineHeight = 18.sp
                    )
                }

                //Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons - Two buttons side by side
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                showBottomSheet = false
                                navController.popBackStack()
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFBDBDBD)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CANCEL",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Void/Refund Button
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                showBottomSheet = !showBottomSheet
                                when (targetAction) {
                                    TransactionAction.VOID -> doVoid(transaction)
                                    TransactionAction.REFUND -> doRefund(transaction)
                                    else -> {}
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = primary500
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
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
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Processing/Success/Error Screen Overlay using StatusScreen
        val dataMessage = MessageForStatusScreen(
            text = processingMessage,
            statusScreenType = processingScreenType
        )

        StatusScreen(navController, dataMessage, strategyFn = {})
    }
}
