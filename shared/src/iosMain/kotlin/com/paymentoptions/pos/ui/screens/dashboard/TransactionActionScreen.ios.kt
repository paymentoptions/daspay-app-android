package com.paymentoptions.pos.ui.screens.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.PaymentStatusRequest
import com.paymentoptions.pos.payment.MineSecPlatform
import com.paymentoptions.pos.payment.PaymentStatusMapper

@Composable
actual fun rememberMineSecActionDispatcher(): MineSecActionDispatcher {
    val provider = MineSecPlatform.paymentProvider

    return remember(provider) {
        object : MineSecActionDispatcher {
            override fun launchVoid(
                acquirerTransactionId: String,
                parentUUID: String,
                childUUID: String,
                notes: String,
                onReady: (PaymentStatusRequest) -> Unit,
                onFailure: (String) -> Unit,
            ) {
                if (provider == null || acquirerTransactionId.isBlank()) {
                    onFailure("MineSec SDK not available for void")
                    return
                }
                provider.launchVoid(
                    acquirerTransactionId = acquirerTransactionId,
                    profileId = MineSecPlatform.PROFILE_ID,
                    onResult = { result ->
                        AppLogger.debug("MineSec void result: success=${result.success}")
                        if (result.success) {
                            val request = PaymentStatusMapper.fromMineSecResult(
                                posReference = childUUID,
                                result = result,
                                amount = "",
                                currency = "",
                                parentUUID = parentUUID,
                                childUUID = childUUID,
                            )
                            onReady(request)
                        } else {
                            onFailure(result.errorMessage ?: "Void failed")
                        }
                    },
                )
            }

            override fun launchRefund(
                acquirerTransactionId: String,
                parentUUID: String,
                childUUID: String,
                amount: String,
                currency: String,
                notes: String,
                onReady: (PaymentStatusRequest) -> Unit,
                onFailure: (String) -> Unit,
            ) {
                if (provider == null || acquirerTransactionId.isBlank()) {
                    onFailure("MineSec SDK not available for refund")
                    return
                }
                provider.launchLinkedRefund(
                    acquirerTransactionId = acquirerTransactionId,
                    amount = amount,
                    currency = currency,
                    profileId = MineSecPlatform.PROFILE_ID,
                    onResult = { result ->
                        AppLogger.debug("MineSec refund result: success=${result.success}")
                        if (result.success) {
                            val request = PaymentStatusMapper.fromMineSecResult(
                                posReference = childUUID,
                                result = result,
                                amount = amount,
                                currency = currency,
                                parentUUID = parentUUID,
                                childUUID = childUUID,
                            )
                            onReady(request)
                        } else {
                            onFailure(result.errorMessage ?: "Refund failed")
                        }
                    },
                )
            }
        }
    }
}
