package com.paymentoptions.pos.ui.composables.screens.settlement

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.endpoints.settleBatch
import com.paymentoptions.pos.services.analytics.AppAnalytics
import com.paymentoptions.pos.ui.composables.screens.status.MessageForStatusScreen
import com.paymentoptions.pos.ui.composables.screens.status.StatusScreen
import com.paymentoptions.pos.ui.composables.screens.status.StatusScreenType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettlementActionScreen(
    navController: NavController,
    settleId : String
) {
    val context = LocalContext.current

    var processingScreenType by remember { mutableStateOf(StatusScreenType.PROCESSING) }
    var processingMessage by remember { mutableStateOf("") }
    val delayTime = 4000L

    fun doSettlement(){
        processingMessage = "Processing\n Settlement..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = settleBatch(context, settleId)
                if (response != null && response.SettleStatus == SETTLED_BATCH) {
                    AppAnalytics.settlementResult(batchId = settleId, result = "successful")
                    withContext(Dispatchers.Main) {
                        processingScreenType = StatusScreenType.SUCCESS
                        processingMessage = "Settlement\n Completed"
                    }
                    // Wait 5 seconds
                    delay(delayTime)
                    withContext(Dispatchers.Main) {
                        navController.popBackStack()
                       // Toast.makeText(context, "Transaction is Settled", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                AppLogger.error("settle HTTP error ${e.code()}: $errorBody")
                AppAnalytics.settlementResult(batchId = settleId, result = "failed")
                withContext(Dispatchers.Main) {
                    processingScreenType = StatusScreenType.ERROR
                    processingMessage = "Settlement\n Failed"
                }
                // Wait 5 seconds
                delay(delayTime)
                withContext(Dispatchers.Main) {
                    navController.popBackStack()
                    Toast.makeText(context, errorBody, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                AppLogger.error("settle: $e")
                AppAnalytics.settlementResult(batchId = settleId, result = "failed")
                withContext(Dispatchers.Main) {
                    processingScreenType = StatusScreenType.ERROR
                    processingMessage = "Settlement\n Failed"
                }
                // Wait 5 seconds
                delay(delayTime)
                withContext(Dispatchers.Main) {
                    navController.popBackStack()
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
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
