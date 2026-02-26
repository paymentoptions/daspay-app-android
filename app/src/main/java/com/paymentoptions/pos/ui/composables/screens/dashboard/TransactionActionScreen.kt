package com.paymentoptions.pos.ui.composables.screens.dashboard

import android.os.Handler
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.ClientHeadlessImpl
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.services.apiService.endpoints.refund
import com.paymentoptions.pos.services.apiService.endpoints.void
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.images.LogoImage
import com.paymentoptions.pos.ui.composables.layout.sectioned.LOGO_HEIGHT_IN_DP
import com.paymentoptions.pos.ui.composables.layout.simple.SimpleLayout
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.composables.screens.status.MessageForStatusScreen
import com.paymentoptions.pos.ui.composables.screens.status.StatusScreen
import com.paymentoptions.pos.ui.composables.screens.status.StatusScreenType
import com.paymentoptions.pos.ui.theme.disabledFilledButtonGradientBrush
import com.paymentoptions.pos.ui.theme.enabledFilledButtonGradientBrush
import com.paymentoptions.pos.ui.theme.primary300
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.ui.theme.red300
import com.paymentoptions.pos.ui.theme.red500
import com.paymentoptions.pos.utils.TransactionAction
import com.paymentoptions.pos.utils.TransactionColors
import com.paymentoptions.pos.utils.getAmountSign
import com.paymentoptions.pos.utils.getAvailableAction
import com.paymentoptions.pos.utils.getStatusColor
import com.paymentoptions.pos.utils.getTransactionIcon
import com.paymentoptions.pos.utils.getTransactionTypeLabel
import com.paymentoptions.pos.utils.modifiers.conditional
import com.paymentoptions.pos.utils.safeParseOffsetDateTime
import com.paymentoptions.pos.workers.TransactionRetryScheduler
import com.paymentoptions.pos.workers.TransactionRetryWorker
import com.theminesec.lib.dto.common.Amount
import com.theminesec.lib.dto.poi.PoiRequest
import com.theminesec.lib.dto.transaction.TranType
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.model.WrappedResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Currency
import java.util.Date


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionActionScreen(
    navController: NavController,
    transaction: TransactionListDataRecord,
    targetAction: TransactionAction
) {
    val context = LocalContext.current


    var showBottomSheet by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var processingScreenType by remember { mutableStateOf(StatusScreenType.PROCESSING) }
    var processingMessage by remember { mutableStateOf("Processing...") }
    val delayTime = 5000L
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateString = transaction.Date   //"2025-04-23T03:38:57.349+00:00"
    val dateTime = safeParseOffsetDateTime(dateString)
    val date: Date = Date.from(dateTime.toInstant())
    val dateStringFormatted = SimpleDateFormat("dd MMMM, YYYY").format(date)


    val statusColor = getStatusColor(transaction)
//    val amountSign = getAmountSign(transaction)
//    val transactionIcon = getTransactionIcon(transaction)
//    val transactionTypeLabel = getTransactionTypeLabel(transaction)

    fun showTransactionFailure() {
        CoroutineScope(Dispatchers.IO).launch {
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
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateRefundTransaction(sdkTransaction: com.theminesec.lib.dto.transaction.Transaction?) {
        CoroutineScope(Dispatchers.IO).launch {
            val maxRetries = 5
            var currentAttempt = 0
            var lastError: Exception? = null
            var response: com.paymentoptions.pos.services.apiService.RefundResponse? = null

            // Show processing screen on Main thread FIRST
            withContext(Dispatchers.Main) {
                processingScreenType = StatusScreenType.PROCESSING
                processingMessage = "Processing Refund..."
            }

            // Retry loop with exponential backoff
            while (currentAttempt < maxRetries && response == null) {
                try {
                    currentAttempt++
                    AppLogger.debug("Refund attempt $currentAttempt of $maxRetries")

                    // Update message to show retry attempt
                    if (currentAttempt > 1) {
//                        withContext(Dispatchers.Main) {
//                            processingMessage = "Processing Refund... (Attempt $currentAttempt/$maxRetries)"
//                        }
                    }

                    // Call API
                    response = refund(
                        context = context,
                        transactionId = transaction.uuid,
                        merchantId = transaction.DASMID,
                        transaction = sdkTransaction,
                        amount = transaction.amount
                    )

                    if (response != null) {
                        AppLogger.debug("Refund successful on attempt $currentAttempt: $response")
                        break // Success - exit retry loop
                    } else {
                        AppLogger.warn("Refund returned null on attempt $currentAttempt")
                        lastError = Exception("API returned null response")
                    }

                } catch (e: retrofit2.HttpException) {
                    errorMessage = e.response()?.errorBody()?.string() ?: e.message()
                    AppLogger.error("void HTTP error ${e.code()}: $errorMessage")
                } catch (e: Exception) {
                    AppLogger.error("Refund attempt $currentAttempt failed: ${e.message}", e)
                    lastError = e

                    // If not the last attempt, wait before retrying (exponential backoff)
                    if (currentAttempt < maxRetries) {
                        val delayTime = (1000L * currentAttempt) // 1s, 2s, 3s, 4s
                        AppLogger.debug("Waiting ${delayTime}ms before retry...")
                        delay(delayTime)
                    }
                }
            }

            // Check final result
            if (response != null) {
                // Show success screen on Main thread
                withContext(Dispatchers.Main) {
                    processingScreenType = StatusScreenType.SUCCESS
                    processingMessage = "Refund Completed"
                }
                AppLogger.debug("StatusScreen: Showing SUCCESS screen for 5 seconds")

                // Wait 5 seconds
                delay(delayTime)
                AppLogger.debug("StatusScreen: 5 seconds elapsed, closing screen")

                // Navigate to Transaction Receipt screen
                withContext(Dispatchers.Main) {
                    navController.navigate(
                        "${Screens.TransactionReceipt.route}?transactionId=${response.transaction_details.id}&title=Your transaction is Refunded"
                    ) {
                        popUpTo(Screens.TransactionAction.route) { inclusive = true }
                    }
                }

            } else {
                // All immediate retries failed
                AppLogger.error("Refund failed after $maxRetries attempts. Last error: ${lastError?.message}")

                // Schedule hourly retries for 24 hours
                AppLogger.debug("Scheduling hourly retries for refund over next 24 hours")
                TransactionRetryScheduler.scheduleHourlyRetries(
                    context = context,
                    transaction = transaction,
                    sdkTransaction = sdkTransaction,
                    operationType = TransactionRetryWorker.OPERATION_REFUND
                )

                withContext(Dispatchers.Main) {
                    processingScreenType = StatusScreenType.ERROR
                    processingMessage = "Refund failed"
                    //processingMessage = "Refund Failed After $maxRetries Attempts\nScheduled hourly retries for 24 hours"
                }
                delay(delayTime)
                withContext(Dispatchers.Main) {
                    navController.popBackStack()

                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun updateVoidTransaction(sdkTransaction: com.theminesec.lib.dto.transaction.Transaction?) {
        CoroutineScope(Dispatchers.IO).launch {
            val maxRetries = 5
            var currentAttempt = 0
            var lastError: Exception? = null
            var response: com.paymentoptions.pos.services.apiService.RefundResponse? = null

            // Show processing screen on Main thread FIRST
            withContext(Dispatchers.Main) {
                processingScreenType = StatusScreenType.PROCESSING
                processingMessage = "Processing Void..."
            }

            // Retry loop with exponential backoff
            while (currentAttempt < maxRetries && response == null) {
                try {
                    currentAttempt++
                    AppLogger.debug("Void attempt $currentAttempt of $maxRetries")

                    // Update message to show retry attempt
                    if (currentAttempt > 1) {
//                        withContext(Dispatchers.Main) {
//                            processingMessage = "Processing Void... (Attempt $currentAttempt/$maxRetries)"
//                        }
                    }

                    // Call API
                    response = void(
                        context = context,
                        transactionId = transaction.uuid,
                        merchantId = transaction.DASMID,
                        transaction = sdkTransaction!!
                    )

                    if (response != null) {
                        AppLogger.debug("Void successful on attempt $currentAttempt: $response")
                        break // Success - exit retry loop
                    } else {
                        AppLogger.warn("Void returned null on attempt $currentAttempt")
                        lastError = Exception("API returned null response")
                    }

                } catch (e: retrofit2.HttpException) {
                    errorMessage = e.response()?.errorBody()?.string() ?: e.message()
                    AppLogger.error("void HTTP error ${e.code()}: $errorMessage")
                } catch (e: Exception) {
                    AppLogger.error("Void attempt $currentAttempt failed: ${e.message}", e)
                    lastError = e

                    // If not the last attempt, wait before retrying (exponential backoff)
                    if (currentAttempt < maxRetries) {
                        val delayTime = (1000L * currentAttempt) // 1s, 2s, 3s, 4s
                        AppLogger.debug("Waiting ${delayTime}ms before retry...")
                        delay(delayTime)
                    }
                }
            }

            // Check final result
            if (response != null) {
                // Show success screen on Main thread
                withContext(Dispatchers.Main) {
                    processingScreenType = StatusScreenType.SUCCESS
                    processingMessage = "Voided"
                }
                AppLogger.debug("StatusScreen: Showing SUCCESS screen for 5 seconds")

                // Wait 5 seconds
                delay(delayTime)

                // Navigate to Transaction Receipt screen
                withContext(Dispatchers.Main) {
                    navController.navigate(
                        "${Screens.TransactionReceipt.route}?transactionId=${response.transaction_details.id}&title=Your transaction is Voided"
                    ) {
                        popUpTo(Screens.TransactionAction.route) { inclusive = true }
                    }
                }

            } else {
                // All immediate retries failed
                AppLogger.error("Void failed after $maxRetries attempts. Last error: ${lastError?.message}")

                // Schedule hourly retries for 24 hours
                AppLogger.debug("Scheduling hourly retries for void over next 24 hours")
                TransactionRetryScheduler.scheduleHourlyRetries(
                    context = context,
                    transaction = transaction,
                    sdkTransaction = sdkTransaction,
                    operationType = TransactionRetryWorker.OPERATION_VOID
                )

                withContext(Dispatchers.Main) {
                    processingScreenType = StatusScreenType.ERROR
                    //processingMessage = "Void Failed After $maxRetries Attempts\nScheduled hourly retries for 24 hours"
                    processingMessage = "Void failed"
                }
                delay(delayTime)
                withContext(Dispatchers.Main) {
                    navController.popBackStack()

                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        HeadlessActivity.contract(ClientHeadlessImpl::class.java)
    ) {
        when (it) {
            is WrappedResult.Success -> {
                AppLogger.debug("inThis Launche response: ${it.toString()}")
                if (it.value.tranType == TranType.REFUND) {
                    errorMessage = "Sorry could not be refunded"
                    updateRefundTransaction(it.value)
                } else {
                    errorMessage = "Sorry could not be voided"
                    updateVoidTransaction(it.value)
                }
            }

            is WrappedResult.Failure -> {
                AppLogger.error("inThis Launcher failure ---->: $it and mesage ${it.extra?.get("message")}")
                errorMessage = it.extra?.get("message") ?: it.message
                showTransactionFailure()
            }
        }
    }

    fun doVoid(transaction: TransactionListDataRecord) {
        AppLogger.debug("full transaction object: $transaction")
        launcher.launch(input = PoiRequest.ActionVoid(transaction.AcquirerTransactionID!!))
    }

    fun doRefund(transaction: TransactionListDataRecord) {
        AppLogger.debug("full transaction object: $transaction")

        when (transaction.ProductType) {
            "SOFTPOS" -> {
                launcher.launch(
                    input = PoiRequest.ActionLinkedRefund(
                        transaction.AcquirerTransactionID!!,
                        Amount(BigDecimal(transaction.amount), Currency.getInstance("HKD"))
                    )
                )
            }

            "PBL", "QR" -> {
                updateRefundTransaction(null)
            }
        }
    }

    // Bottom Sheet for Refund/Void Operations
    if (showBottomSheet) {
        SimpleLayout {}

        ModalBottomSheet(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.White,
            contentColor = primary500,
            onDismissRequest = {
                showBottomSheet = false
                navController.popBackStack()
            },
            sheetState = sheetState,
            dragHandle = {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                    horizontalArrangement = Arrangement.End,
                ) {

                    IconButton(onClick = {
                        showBottomSheet = false
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = primary500
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = when (targetAction) {
                            TransactionAction.VOID -> "Void Transaction"
                            TransactionAction.REFUND -> "Refund Transaction"
                            else -> "Transaction Action"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary900,
                    )
                }

                CurrencyText(
                    currency = transaction.CurrencyCode,
                    amount = transaction.amount
                )

                // Transaction Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Reference No:",
                            fontSize = 12.sp,
                            color = purple50,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = transaction.uuid,
                            fontSize = 12.sp,
                            color = primary500,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Date:",
                            fontSize = 12.sp,
                            color = purple50,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = dateStringFormatted,
                            fontSize = 12.sp,
                            color = primary500,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Aggegator:",
                            fontSize = 12.sp,
                            color = purple50,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = transaction.Scheme,
                            fontSize = 12.sp,
                            color = primary500,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Warning message
                val warningMessage = when (targetAction) {
                    TransactionAction.VOID -> "Are you sure you want to void this transaction?\nThis action cannot be undone."
                    TransactionAction.REFUND -> "Are you sure you want to refund this transaction?\nThis action cannot be undone."
                    else -> ""
                }

                Text(
                    text = warningMessage,
                    fontSize = 14.sp,
                    color = TransactionColors.Yellow,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                )

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                        colors = CardDefaults.cardColors(containerColor = Color.Gray),
                        border = BorderStroke(1.dp, primary500),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Confirm Button
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                when (targetAction) {
                                    TransactionAction.VOID -> doVoid(transaction)
                                    TransactionAction.REFUND -> doRefund(transaction)
                                    else -> {}
                                }
                                showBottomSheet = false
                            }
                        ,
                        colors = CardDefaults.cardColors(
                            containerColor = when (targetAction) {
                                TransactionAction.VOID -> primary300
                                TransactionAction.REFUND -> primary300
                                else -> red500
                            }
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (targetAction) {
                                    TransactionAction.VOID -> "Void"
                                    TransactionAction.REFUND -> "Refund"
                                    else -> "Confirm"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
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