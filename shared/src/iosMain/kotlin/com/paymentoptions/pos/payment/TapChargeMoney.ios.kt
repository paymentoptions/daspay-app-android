package com.paymentoptions.pos.payment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import com.paymentoptions.pos.analytics.AnalyticsHelper
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.ApiHttpException
import com.paymentoptions.pos.network.endpoints.paymentStatus
import com.paymentoptions.pos.showToast
import kotlinx.coroutines.launch

@Composable
actual fun TapChargeMoney(
    navController: NavController,
    amountToCharge: String,
    onLoader: (nextStage: () -> Unit) -> Unit,
    onSuccessUpdateFlowStage: () -> Unit,
    onFailureUpdateFlowStage: () -> Unit,
    updateLatestTransaction: (id: String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val provider = MineSecPlatform.paymentProvider
    val currency = DPStorageManager.getTransactionCurrency()

    if (provider == null) {
        showToast("MineSec SDK not initialized")
        onFailureUpdateFlowStage()
        return
    }

    LaunchedEffect(Unit) {
        AnalyticsHelper.trackPaymentStarted(
            paymentMethod = "SOFTPOS",
            amount = amountToCharge,
            currency = currency,
        )

        MineSecSDK.processSale(
            amount = amountToCharge,
            onNativeTrigger = { posRef ->
                provider.launchSale(
                    amount = amountToCharge,
                    currency = currency,
                    description = "Payment from POS",
                    posReference = posRef,
                    onResult = { result ->
                        if (result.success) {
                            scope.launch {
                                val paymentStatusRequest = PaymentStatusMapper.fromMineSecResult(
                                    posReference = posRef,
                                    result = result,
                                    amount = amountToCharge,
                                    currency = currency,
                                )
                                AppLogger.debug("TapToPay iOS webhook payload built: $paymentStatusRequest")
                                try {
                                    val webhookSuccess = paymentStatus(request = paymentStatusRequest) != null
                                    val webhookTranId = paymentStatusRequest.tranId.orEmpty()
                                    AppLogger.debug("TapToPay iOS webhook response: success=$webhookSuccess, tranId=$webhookTranId")

                                    if (webhookSuccess) {
                                        AnalyticsHelper.trackPaymentApproved(
                                            transactionId = webhookTranId,
                                            amount = amountToCharge,
                                        )
                                        onLoader {
                                            updateLatestTransaction(webhookTranId)
                                            onSuccessUpdateFlowStage()
                                        }
                                    } else {
                                        AnalyticsHelper.trackPaymentDeclined(
                                            reason = "paymentStatus returned false",
                                            transactionId = webhookTranId,
                                        )
                                        onLoader {
                                            updateLatestTransaction(webhookTranId)
                                            onFailureUpdateFlowStage()
                                        }
                                    }
                                } catch (e: Exception) {
                                    AppLogger.error("TapToPay iOS webhook exception: ${e.message}", e)
                                    AnalyticsHelper.trackPaymentFailed(
                                        reason = e.message,
                                        transactionId = paymentStatusRequest.tranId,
                                        throwable = e,
                                    )
                                    AnalyticsHelper.trackApiError(
                                        endpoint = "paymentStatus",
                                        statusCode = (e as? ApiHttpException)?.statusCode,
                                        message = e.message ?: "Payment status failed",
                                        throwable = e,
                                    )
                                    onLoader {
                                        updateLatestTransaction(paymentStatusRequest.tranId.orEmpty())
                                        onFailureUpdateFlowStage()
                                    }
                                }
                            }
                        } else {
                            val message = result.errorMessage ?: "Transaction failed"
                            if (message.contains("cancel", ignoreCase = true)) {
                                AnalyticsHelper.trackPaymentCancelled(reason = message)
                            } else {
                                AnalyticsHelper.trackPaymentFailed(reason = message)
                            }
                            showToast(message)
                            onFailureUpdateFlowStage()
                        }
                    },
                )
            },
            onError = { error ->
                AnalyticsHelper.trackPaymentFailed(reason = error)
                showToast(error)
                onFailureUpdateFlowStage()
            },
        )
    }
}
