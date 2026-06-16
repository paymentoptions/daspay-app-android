package com.paymentoptions.pos.ui.screens.settlement

import com.paymentoptions.pos.showToast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.paymentoptions.pos.analytics.AnalyticsHelper
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.ApiHttpException
import com.paymentoptions.pos.network.endpoints.settleBatch
import com.paymentoptions.pos.ui.screens.status.MessageForStatusScreen
import com.paymentoptions.pos.ui.screens.status.StatusScreen
import com.paymentoptions.pos.ui.screens.status.StatusScreenType
import com.paymentoptions.pos.utils.parseApiErrorMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettlementActionScreen(
    navController: NavController,
    settleId : String
) {

    var processingScreenType by remember { mutableStateOf(StatusScreenType.PROCESSING) }
    var processingMessage by remember { mutableStateOf("") }
    val delayTime = 4000L

    fun doSettlement(){
        processingMessage = "Processing\n Settlement..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = settleBatch(settleId)
                if (response != null && response.SettleStatus == SETTLED_BATCH) {
                    AnalyticsHelper.trackSettlementResult(success = true, batchId = settleId)
                    withContext(Dispatchers.Main) {
                        processingScreenType = StatusScreenType.SUCCESS
                        processingMessage = "Settlement\n Completed"
                    }
                    // Wait 5 seconds
                    delay(delayTime)
                    withContext(Dispatchers.Main) {
                        navController.popBackStack()
                       // showToast("Transaction is Settled")
                    }
                }
            } catch (e: Exception) {
                AnalyticsHelper.trackSettlementResult(success = false, batchId = settleId, reason = e.message)
                AnalyticsHelper.trackApiError(
                    endpoint = "settleBatch",
                    statusCode = (e as? ApiHttpException)?.statusCode,
                    message = e.message ?: "Settlement failed",
                    throwable = e,
                )
                AppLogger.error("settle: $e")
                val errorMessage = parseApiErrorMessage(e, "Settlement failed")
                withContext(Dispatchers.Main) {
                    processingScreenType = StatusScreenType.ERROR
                    processingMessage = errorMessage
                }
                // Wait 5 seconds
                delay(delayTime)
                withContext(Dispatchers.Main) {
                    navController.popBackStack()
                    showToast(errorMessage)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        doSettlement()
    }

    // Processing/Success/Error Screen Overlay using StatusScreen
    val dataMessage = MessageForStatusScreen(
        text = processingMessage,
        statusScreenType = processingScreenType
    )

    StatusScreen(navController, dataMessage,{})
}
