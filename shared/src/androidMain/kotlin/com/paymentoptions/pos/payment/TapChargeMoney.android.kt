package com.paymentoptions.pos.payment

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.paymentoptions.pos.ClientHeadlessImpl
import com.paymentoptions.pos.analytics.AnalyticsHelper
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.device.DPStorageManager.getTapPayDasmid
import com.paymentoptions.pos.device.DPStorageManager.getTransactionCurrency
import com.paymentoptions.pos.getDeviceIpAddress
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.Address
import com.paymentoptions.pos.network.ApiHttpException
import com.paymentoptions.pos.network.PaymentMethodRequest
import com.paymentoptions.pos.network.PaymentRequest
import com.paymentoptions.pos.network.PaymentResponse
import com.paymentoptions.pos.network.PaymentReturnUrl
import com.paymentoptions.pos.network.PaymentStatusRequest
import com.paymentoptions.pos.network.endpoints.payment
import com.paymentoptions.pos.network.endpoints.sendWebHookNotification
import com.paymentoptions.pos.showToast
import com.paymentoptions.pos.storage.AppStorage
import com.paymentoptions.pos.ui.composables._components.dialogs.AlertDialogType
import com.paymentoptions.pos.ui.composables._components.dialogs.MyAlertDialog
import com.paymentoptions.pos.ui.navigation.Screens
import com.paymentoptions.pos.ui.screens.status.StatusScreenType
import com.paymentoptions.pos.utils.decodeJwtPayload
import com.paymentoptions.pos.utils.getDeviceTimeZone
import com.paymentoptions.pos.utils.getKeyFromToken
import com.paymentoptions.pos.utils.showSessionExpiredAndNavigateToFingerprint
import com.theminesec.lib.dto.common.Amount
import com.theminesec.lib.dto.poi.PoiRequest
import com.theminesec.lib.dto.transaction.TranType
import com.theminesec.lib.dto.transaction.Transaction
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.model.WrappedResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Currency

/**
 * Classifies a MineSec [WrappedResult.Failure] by its real numeric error code — not by
 * string-matching the message — into a [StatusScreenType] (5.9). Codes confirmed by decompiling
 * `headless-1.2.17-live-release.aar`'s `HeadlessError` enum: 42001 = AbortedCardDetectionTimeout,
 * 42002 = AbortedManually, 42003 = AbortedCardReadRetryCountExceeded.
 */
private fun headlessFailureCodeToStatusScreenType(code: Int): StatusScreenType = when (code) {
    42001, 42003 -> StatusScreenType.TIMEOUT
    42002 -> StatusScreenType.CANCELLED
    else -> StatusScreenType.DECLINED
}

@SuppressLint("CoroutineCreationDuringComposition")
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
    val scope = rememberCoroutineScope()
    var rawInput = ""
    var paymentLoader by remember { mutableStateOf(false) }
    var transactionDetailsText by remember { mutableStateOf("") }
    var hasLaunchedPayment by remember { mutableStateOf(false) }
    val idToken = AppStorage.idToken
    AppLogger.debug("TapToPay entry: amount=$amountToCharge, idToken=${idToken != null}")

    if (idToken.isNullOrBlank()) {
        AppLogger.warn("TapToPay aborted: auth details not found, redirecting to auth")
        showSessionExpiredAndNavigateToFingerprint(navController)
        return
    }

    
    val merchant: MutableMap<String, String> = mutableMapOf<String, String>()
    val decodedJwtPayloadJson = decodeJwtPayload(idToken)
    val currency = getTransactionCurrency()

    merchant["dasmid"] = getTapPayDasmid()
    merchant["name"] = getKeyFromToken(decodedJwtPayloadJson, "name")
    merchant["email"] = getKeyFromToken(decodedJwtPayloadJson, "email")
    merchant["contact"] = getKeyFromToken(decodedJwtPayloadJson, "custom:ContactNo")
    AppLogger.debug(
        "TapToPay merchant prepared: dasmid=${merchant["dasmid"]}, namePresent=${!merchant["name"].isNullOrBlank()}, emailPresent=${!merchant["email"].isNullOrBlank()}, contactPresent=${!merchant["contact"].isNullOrBlank()}, currency=$currency"
    )

    val launcher = rememberLauncherForActivityResult(
        HeadlessActivity.contract(ClientHeadlessImpl::class.java)
    ) {
        paymentLoader = false
        AppLogger.debug("TapToPay MineSec launcher callback received: resultType=${it::class.simpleName}")

        var completedSaleTranId: String? = ""
        var completedSalePosReference: String? = ""
        var completedSaleRequestId: String? = ""

        fun createPaymentRequest(transaction: Transaction): PaymentStatusRequest {
            return transaction.toPaymentStatusRequest()
        }

        when (it) {
            is WrappedResult.Success -> {
                AppLogger.debug(
                    "TapToPay MineSec success: tranType=${it.value.tranType}, tranStatus=${it.value.tranStatus}, tranId=${it.value.tranId}, posReference=${it.value.posReference}"
                )
                if (it.value.tranType == TranType.SALE) {
                    completedSaleTranId = it.value.tranId
                    completedSalePosReference = it.value.posReference
                    completedSaleRequestId = it.value.actions.firstOrNull()?.requestId
                }

                transactionDetailsText =
                    "Transaction of $$amountToCharge was successful. POS Reference Transaction ID returned by MineSec is: $completedSalePosReference."
                AppLogger.debug(
                    "TapToPay MineSec sale extracted: saleTranId=$completedSaleTranId, salePosReference=$completedSalePosReference, saleRequestId=$completedSaleRequestId"
                )

                rawInput = ""

                val paymentStatusRequest = createPaymentRequest(it.value)
                AppLogger.debug("TapToPay webhook payload built: $paymentStatusRequest")

                // The card read completed — real processing (webhook confirmation) starts now
                // and runs until the backend actually responds (5.8).
                onLoader()

                // A plain scope, not the composition-bound `scope` above: onLoader() just
                // triggered a flow-stage change that disposes this composable (and would cancel
                // a composition-bound scope) before the webhook call can run — it must survive
                // that transition to actually complete.
                CoroutineScope(Dispatchers.IO).launch {
                    AppLogger.debug("TapToPay webhook call started")
                    try {
                        val paymentStatusResponse =
                            sendWebHookNotification(request = paymentStatusRequest) != null

                        AppLogger.debug("TapToPay webhook response: success=$paymentStatusResponse, tranId=${paymentStatusRequest.tranId}")
                        if (paymentStatusResponse) {
                            AnalyticsHelper.trackPaymentApproved(
                                transactionId = paymentStatusRequest.tranId.toString(),
                                amount = amountToCharge,
                            )
                            updateLatestTransaction(paymentStatusRequest.tranId.toString())
                            AppLogger.debug("TapToPay flow moving to SUCCESS stage for tranId=${paymentStatusRequest.tranId}")
//                            val cvmLimit = DPStorageManager.getCvmLimit()
//                            val amount = amountToCharge.toDoubleOrNull() ?: 0.0
//                            if (amount >= cvmLimit) {
//                                AppLogger.debug("TapToPay: Amount $amount >= CVM Limit $cvmLimit, moving to MANDATORY_SIGNATURE")
//                                onMandatorySignature()
//                            } else {
//                                onSuccessUpdateFlowStage()
//                            }
                            onSuccessUpdateFlowStage()
                        } else {
                            AnalyticsHelper.trackPaymentDeclined(
                                reason = "paymentStatus returned false",
                                transactionId = paymentStatusRequest.tranId.toString(),
                            )
                            updateLatestTransaction(paymentStatusRequest.tranId.toString())
                            AppLogger.warn("TapToPay flow moving to FAILURE stage due to false webhook response for tranId=${paymentStatusRequest.tranId}")
                            onFailureMessage("Tap to Pay failed while confirming payment status. Please retry.")
                            onFailureUpdateFlowStage(StatusScreenType.DECLINED)
                        }
                    } catch (e: Exception) {
                        AppLogger.error("TapToPay webhook exception for tranId=${paymentStatusRequest.tranId}: ${e.message}", e)

                        AnalyticsHelper.trackPaymentFailed(
                            reason = e.message,
                            transactionId = paymentStatusRequest.tranId.toString(),
                            throwable = e,
                        )
                        AnalyticsHelper.trackApiError(
                            endpoint = "paymentStatus",
                            statusCode = (e as? ApiHttpException)?.statusCode,
                            message = e.message ?: "Payment status failed",
                            throwable = e,
                        )
                        onFailureMessage(e.message ?: "Tap to Pay failed during payment confirmation.")
                        updateLatestTransaction(paymentStatusRequest.tranId.toString())
                        onFailureUpdateFlowStage(StatusScreenType.ERROR)
                    }
                }
            }

            is WrappedResult.Failure -> {
                println("inThis Launcher failure ---->: $it")
                val mineSecFailureMessage =
                     it.extra?.get("message")?.toString() ?: it.message
                AppLogger.error(
                    "TapToPay MineSec failure: code=${it.code}, message=${it.message}, extraMessage=${mineSecFailureMessage}, raw=$it"
                )
                onFailureMessage(mineSecFailureMessage)
                // Classified from the SDK's real numeric error code, not string-matching the
                // message (5.9).
                val outcome = headlessFailureCodeToStatusScreenType(it.code)
                when (outcome) {
                    StatusScreenType.CANCELLED -> AnalyticsHelper.trackPaymentCancelled(reason = it.message)
                    else -> AnalyticsHelper.trackPaymentFailed(reason = it.message)
                }
                showToast(mineSecFailureMessage)
                onFailureUpdateFlowStage(outcome)
            }
        }
    }

    val paymentReturnUrl = PaymentReturnUrl(
        webhook_url = "https://webhook.site/cdaa023f-fd59-4286-a241-1b120fbf1454%22",
        success_url = "https://api-bpm.hiji.xyz/dgv3/success%22",
        decline_url = "https://api-bpm.hiji.xyz/dgv3/decline%22"
    )

    val billingAddress = Address(
        country = "IN",
        email = merchant["email"]!!,
        address1 = "Chiyoda1-1",
        phone_number = merchant["contact"]!!,
        city = "Minatoku",
        state = "Tokyoto",
        postal_code = "100001"
    )

    val paymentMethod = PaymentMethodRequest(type = "daspay")

    val paymentRequest = PaymentRequest(
        amount = amountToCharge,
        currency = currency,
        merchant_txn_ref = "TEST00989012878787878787878787",
        customer_ip = getDeviceIpAddress(),
        merchant_id = merchant["dasmid"]!!,
        return_url = paymentReturnUrl,
        billing_address = billingAddress,
        shipping_address = billingAddress,
        payment_method = paymentMethod,
        time_zone = getDeviceTimeZone(),
        cartID = ""
    )

    MyAlertDialog(
        showDialog = paymentLoader,
        text = "Loading Tap to Pay...Please wait a moment.",
        actionButtonText = "Try Again",
        type = AlertDialogType.LOADER,
        showActionButton = false,
        onActionFn = {})

    if (!hasLaunchedPayment) {
        hasLaunchedPayment = true

        scope.launch {
            paymentLoader = true
            try {
                AnalyticsHelper.trackPaymentStarted(
                    paymentMethod = "SOFTPOS",
                    amount = amountToCharge,
                    currency = currency,
                )
                val paymentResponse: PaymentResponse? = payment(paymentRequest)
                println("paymentResponse: $paymentResponse")
                if (paymentResponse == null) {
                    AnalyticsHelper.trackPaymentFailed(reason = "Payment response is null")
                    showSessionExpiredAndNavigateToFingerprint(navController)
                    return@launch
                }

                paymentResponse?.let {
                    if (it.success) {
                        println("inThis PaymentResponse ---->")
                        launcher.launch(
                            PoiRequest.ActionNew(
                                tranType = TranType.SALE,
                                amount = Amount(
                                    BigDecimal(amountToCharge),
                                    Currency.getInstance(DPStorageManager.getTransactionCurrency()),
                                ),
                                profileId = MineSecPlatform.PROFILE_ID,
                                posReference = it.transaction_details.id
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                AnalyticsHelper.trackPaymentFailed(reason = e.message, throwable = e)
                AnalyticsHelper.trackApiError(
                    endpoint = "payment",
                    statusCode = (e as? ApiHttpException)?.statusCode,
                    message = e.message ?: "Payment start failed",
                    throwable = e,
                )
                DPStorageManager.clearSharedPreferences()
                navController.navigate(Screens.AuthCheck.route) {
                    popUpTo(0) { inclusive = true }
                }
                println("Payment Error: ${e.toString()}")
            } finally {
                paymentLoader = false
            }
        }
    }
}