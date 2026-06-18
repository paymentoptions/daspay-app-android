package com.paymentoptions.pos.payment

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
import com.paymentoptions.pos.network.endpoints.paymentStatus
import com.paymentoptions.pos.showToast
import com.paymentoptions.pos.ui.composables._components.dialogs.AlertDialogType
import com.paymentoptions.pos.ui.composables._components.dialogs.MyAlertDialog
import com.paymentoptions.pos.ui.navigation.Screens
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
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Currency

@SuppressLint("CoroutineCreationDuringComposition")
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
    var rawInput = ""
    var paymentLoader by remember { mutableStateOf(false) }
    var showProcessingScreen by remember { mutableStateOf(false) }
    var transactionDetailsText by remember { mutableStateOf("") }
    var hasLaunchedPayment by remember { mutableStateOf(false) }
    val authDetails = DPStorageManager.getAuthDetails()
    AppLogger.debug("TapToPay entry: amount=$amountToCharge, authAvailable=${authDetails != null}")

    if (authDetails == null || authDetails.data == null || authDetails.data!!.token.idToken.isBlank()) {
        AppLogger.warn("TapToPay aborted: auth details not found, redirecting to auth")
        showSessionExpiredAndNavigateToFingerprint(navController)
        return
    }

    
    val merchant: MutableMap<String, String> = mutableMapOf<String, String>()
    val decodedJwtPayloadJson = decodeJwtPayload(authDetails!!.data!!.token.idToken)
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

                scope.launch {
                    showProcessingScreen = true
                    AppLogger.debug("TapToPay webhook call started")
                    try {
                        val paymentStatusResponse =
                            paymentStatus(request = paymentStatusRequest) != null

                        showProcessingScreen = false
                        AppLogger.debug("TapToPay webhook response: success=$paymentStatusResponse, tranId=${paymentStatusRequest.tranId}")
                        if (paymentStatusResponse) {
                            AnalyticsHelper.trackPaymentApproved(
                                transactionId = paymentStatusRequest.tranId.toString(),
                                amount = amountToCharge,
                            )
                            updateLatestTransaction(paymentStatusRequest.tranId.toString())
                            AppLogger.debug("TapToPay flow moving to SUCCESS stage for tranId=${paymentStatusRequest.tranId}")
                            onLoader { onSuccessUpdateFlowStage() }
                        } else {
                            AnalyticsHelper.trackPaymentDeclined(
                                reason = "paymentStatus returned false",
                                transactionId = paymentStatusRequest.tranId.toString(),
                            )
                            updateLatestTransaction(paymentStatusRequest.tranId.toString())
                            AppLogger.warn("TapToPay flow moving to FAILURE stage due to false webhook response for tranId=${paymentStatusRequest.tranId}")
                            //onFailureMessage("Tap to Pay failed while confirming payment status. Please retry.")
                            onLoader {
                                onFailureUpdateFlowStage()
                            }
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
                        showProcessingScreen = false
                        updateLatestTransaction(paymentStatusRequest.tranId.toString())
                        onLoader {
                            onFailureUpdateFlowStage()
                        }
                    }
                }
            }

            is WrappedResult.Failure -> {
                println("inThis Launcher failure ---->: $it")
                val mineSecFailureMessage =
                     it.extra?.get("message")?.toString() ?: it.message
                AppLogger.error(
                    "TapToPay MineSec failure: message=${it.message}, extraMessage=${mineSecFailureMessage}, raw=$it"
                )
                val failureMessage = it.message
                val isCancelled = failureMessage.contains("cancel", ignoreCase = true)
                if (isCancelled) {
                    AnalyticsHelper.trackPaymentCancelled(reason = failureMessage)
                } else {
                    AnalyticsHelper.trackPaymentFailed(reason = failureMessage)
                }
                showToast(mineSecFailureMessage)

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
        time_zone = getDeviceTimeZone()
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
                                profileId = "prof_01KH8NQC4PVFKRNH31ZPC2QJNN",
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