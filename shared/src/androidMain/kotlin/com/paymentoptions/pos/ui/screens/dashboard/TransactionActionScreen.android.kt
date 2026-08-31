package com.paymentoptions.pos.ui.screens.dashboard

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.paymentoptions.pos.ClientHeadlessImpl
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.PaymentStatusRequest
import com.paymentoptions.pos.payment.toPaymentStatusRequest
import com.theminesec.lib.dto.common.Amount
import com.theminesec.lib.dto.poi.PoiRequest
import com.theminesec.lib.dto.transaction.TranType
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.model.WrappedResult
import java.math.BigDecimal
import java.util.Currency

@Composable
actual fun rememberMineSecActionDispatcher(): MineSecActionDispatcher {
    // Pending context captured from launchVoid/launchRefund and read by the launcher callback.
    val pending = remember { AndroidPendingDispatch() }

    val launcher = rememberLauncherForActivityResult(
        HeadlessActivity.contract(ClientHeadlessImpl::class.java)
    ) { result ->
        when (result) {
            is WrappedResult.Success -> {
                val sdkTx = result.value
                AppLogger.debug("MineSec launcher success: ${sdkTx.tranType}")
                val request: PaymentStatusRequest = sdkTx.toPaymentStatusRequest(
                    parentUUID = pending.parentUUID,
                    childUUID = pending.childUUID,
                    gateWayNotes = pending.notes,
                )
                if (sdkTx.tranType == TranType.REFUND) {
                    pending.refundOnReady?.invoke(request)
                } else {
                    pending.voidOnReady?.invoke(request)
                }
            }

            is WrappedResult.Failure -> {
                val msg = result.extra?.get("message") ?: result.message
                AppLogger.error("MineSec launcher failure: $msg")
                pending.voidOnFailure?.invoke(msg)
                pending.refundOnFailure?.invoke(msg)
            }
        }
        pending.clear()
    }

    return remember(launcher) {
        object : MineSecActionDispatcher {
            override fun launchVoid(
                acquirerTransactionId: String,
                parentUUID: String,
                childUUID: String,
                notes: String,
                onReady: (PaymentStatusRequest) -> Unit,
                onFailure: (String) -> Unit,
            ) {
                pending.parentUUID = parentUUID
                pending.childUUID = childUUID
                pending.notes = notes
                pending.voidOnReady = onReady
                pending.voidOnFailure = onFailure
                launcher.launch(PoiRequest.ActionVoid(acquirerTransactionId))
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
                pending.parentUUID = parentUUID
                pending.childUUID = childUUID
                pending.notes = notes
                pending.refundOnReady = onReady
                pending.refundOnFailure = onFailure
                launcher.launch(
                    PoiRequest.ActionLinkedRefund(
                        acquirerTransactionId,
                        Amount(BigDecimal(amount), Currency.getInstance(currency)),
                    )
                )
            }
        }
    }
}

/** Mutable holder for callbacks and context set before launching the HeadlessActivity. */
private class AndroidPendingDispatch {
    var parentUUID: String = ""
    var childUUID: String = ""
    var notes: String = ""
    var voidOnReady: ((PaymentStatusRequest) -> Unit)? = null
    var voidOnFailure: ((String) -> Unit)? = null
    var refundOnReady: ((PaymentStatusRequest) -> Unit)? = null
    var refundOnFailure: ((String) -> Unit)? = null

    fun clear() {
        voidOnReady = null
        voidOnFailure = null
        refundOnReady = null
        refundOnFailure = null
    }
}
