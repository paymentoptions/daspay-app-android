package com.paymentoptions.pos.payment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.paymentoptions.pos.analytics.AnalyticsHelper
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.ApiHttpException
import com.paymentoptions.pos.network.endpoints.sendWebHookNotification
import com.paymentoptions.pos.showToast
import com.paymentoptions.pos.ui.screens.status.StatusScreenType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Maps a native-SDK-classified [MineSecTransactionResult.outcomeKind] to a [StatusScreenType]. */
private fun outcomeKindToStatusScreenType(outcomeKind: String?): StatusScreenType = when (outcomeKind) {
    "cancelled" -> StatusScreenType.CANCELLED
    "timeout" -> StatusScreenType.TIMEOUT
    "error" -> StatusScreenType.ERROR
    else -> StatusScreenType.DECLINED
}

@Composable
actual fun TapChargeMoney(
    navController: NavController,
    amountToCharge: String,
    gatewayNotes: String?,
    onLoader: () -> Unit,
    onSuccessUpdateFlowStage: () -> Unit,
    onFailureUpdateFlowStage: (StatusScreenType) -> Unit,
    updateLatestTransaction: (id: String) -> Unit,
    onFailureMessage: (String) -> Unit,
    onMandatorySignature: () -> Unit,
) {
    val provider = MineSecPlatform.paymentProvider
    val currency = DPStorageManager.getTransactionCurrency()

    if (provider == null || !provider.isInitialized) {
        showToast("MineSec SDK not ready. Please wait a moment and try again.")
        onFailureUpdateFlowStage(StatusScreenType.ERROR)
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
                            // The card read completed — real processing (webhook confirmation)
                            // starts now and runs until the backend actually responds (5.8).
                            onLoader()
                            // A plain scope, not rememberCoroutineScope(): onLoader() above just
                            // triggered a flow-stage change that disposes this composable (and
                            // would cancel a composition-bound scope) before the webhook call
                            // can run — it must survive that transition to actually complete.
                            // (Dispatchers.IO isn't available on iOS/Native; Default is fine for
                            // a suspend network call via Ktor, which doesn't block a thread.)
                            CoroutineScope(Dispatchers.Default).launch {
                                val paymentStatusRequest = PaymentStatusMapper.fromMineSecResult(
                                    posReference = posRef,
                                    result = result,
                                    amount = amountToCharge,
                                    currency = currency,
                                )
                                AppLogger.debug("TapToPay iOS webhook payload built: $paymentStatusRequest")
                                try {
                                    val webhookSuccess = sendWebHookNotification(request = paymentStatusRequest) != null
                                    val webhookTranId = paymentStatusRequest.tranId.orEmpty()
                                    AppLogger.debug("TapToPay iOS webhook response: success=$webhookSuccess, tranId=$webhookTranId")

                                    if (webhookSuccess) {
                                        AnalyticsHelper.trackPaymentApproved(
                                            transactionId = webhookTranId,
                                            amount = amountToCharge,
                                        )
                                        updateLatestTransaction(webhookTranId)
                                        onSuccessUpdateFlowStage()
                                    } else {
                                        AnalyticsHelper.trackPaymentDeclined(
                                            reason = "paymentStatus returned false",
                                            transactionId = webhookTranId,
                                        )
                                        onFailureMessage("Tap to Pay failed while confirming payment status. Please retry.")
                                        updateLatestTransaction(webhookTranId)
                                        onFailureUpdateFlowStage(StatusScreenType.DECLINED)
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
                                    onFailureMessage(e.message ?: "Tap to Pay failed during payment confirmation.")
                                    updateLatestTransaction(paymentStatusRequest.tranId.orEmpty())
                                    onFailureUpdateFlowStage(StatusScreenType.ERROR)
                                }
                            }
                        } else {
                            // Classified from a real reader-session event observed during this
                            // tap (cancelled/timeout), not from string-matching errorMessage (5.9).
                            val outcome = outcomeKindToStatusScreenType(result.outcomeKind)
                            val message = result.errorMessage ?: "Transaction failed"
                            when (outcome) {
                                StatusScreenType.CANCELLED -> AnalyticsHelper.trackPaymentCancelled(reason = message)
                                else -> AnalyticsHelper.trackPaymentFailed(reason = message)
                            }
                            showToast(message)
                            onFailureMessage(message)
                            onFailureUpdateFlowStage(outcome)
                        }
                    },
                )
            },
            onError = { error ->
                AnalyticsHelper.trackPaymentFailed(reason = error)
                showToast(error)
                onFailureMessage(error)
                onFailureUpdateFlowStage(StatusScreenType.ERROR)
            },
        )
    }
}
