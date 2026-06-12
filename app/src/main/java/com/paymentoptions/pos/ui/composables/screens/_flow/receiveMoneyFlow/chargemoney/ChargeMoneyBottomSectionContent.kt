package com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow.chargemoney

import com.paymentoptions.pos.ui.composables._components.dialogs.MyDialog
import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.ClientHeadlessImpl
import com.paymentoptions.pos.analytics.AnalyticsHelper
import com.paymentoptions.pos.device.DeveloperOptions
import com.paymentoptions.pos.device.Nfc
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.device.DPStorageManager.getTapPayDasmid
import com.paymentoptions.pos.device.DPStorageManager.getTransactionCurrency
import com.paymentoptions.pos.getDeviceIpAddress
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.endpoints.payment
import com.paymentoptions.pos.network.endpoints.paymentStatus
import com.paymentoptions.pos.network.ApiHttpException
import com.paymentoptions.pos.network.Address
import com.paymentoptions.pos.network.PaymentMethodRequest
import com.paymentoptions.pos.network.PaymentRequest
import com.paymentoptions.pos.network.PaymentResponse
import com.paymentoptions.pos.network.PaymentReturnUrl
import com.paymentoptions.pos.network.PaymentStatusRequest
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.buttons.OutlinedButton
import com.paymentoptions.pos.ui.composables._components.dialogs.AlertDialogType
import com.paymentoptions.pos.ui.composables._components.dialogs.MyAlertDialog
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.innerShadow
import com.paymentoptions.pos.ui.theme.primary600
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.decodeJwtPayload
import com.paymentoptions.pos.utils.getDeviceTimeZone
import com.paymentoptions.pos.utils.getKeyFromToken
import com.paymentoptions.pos.utils.inProduction
import com.paymentoptions.pos.utils.modifiers.innerShadow
import com.paymentoptions.pos.utils.modifiers.noRippleClickable
import com.paymentoptions.pos.utils.qrCodePaymentMethod
import com.paymentoptions.pos.utils.tapPaymentMethod
import com.theminesec.lib.dto.common.Amount
import com.theminesec.lib.dto.poi.PoiRequest
import com.theminesec.lib.dto.transaction.TranType
import com.paymentoptions.pos.services.apiService.toPaymentStatusRequest
import com.paymentoptions.pos.utils.PaymentMethod

import com.theminesec.lib.dto.transaction.Transaction
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.model.WrappedResult
import kotlinx.coroutines.launch

import java.math.BigDecimal
import java.util.Currency

@Composable
fun PaymentMethodButton(
    paymentMethod: PaymentMethod,
    selectedPaymentMethod: PaymentMethod,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val isSelected = selectedPaymentMethod == paymentMethod

    Column(
        modifier = modifier
            .padding(6.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) Color.White.copy(alpha = 0.9f) else Color.Transparent)
            .padding(10.dp)
            .noRippleClickable(enabled = !isSelected) { onSelected() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Icon(
            paymentMethod.icon, contentDescription = paymentMethod.text, tint = primary600
        )

        Text(
            text = paymentMethod.text,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = primary600
        )

    }
}

@Composable
fun ChargeMoneyBottomSectionContent(
    navController: NavController,
    enableScrolling: Boolean = false,
    amountToCharge: String,
    availablePaymentMethods: List<PaymentMethod>,
    selectedPaymentMethod: PaymentMethod,
    updateSelectedPaymentMethod: (PaymentMethod) -> Unit = {},
    onLoader: (nextStage: () -> Unit) -> Unit = {},
    onSuccessUpdateFlowStage: () -> Unit = {},
    onFailureUpdateFlowStage: () -> Unit = {},
    onChangeAmount: () -> Unit,
    startTapAndPay: Boolean = false,
    updateLatestTransaction: (id: String) -> Unit,
) {
    val context = LocalContext.current
    val currency = getTransactionCurrency()
    val nfcState = Nfc.getStatus(context)

    var showDeveloperOptionsEnabled by remember { mutableStateOf(false) }
    var showNFCNotEnabled by remember { mutableStateOf(false) }

    MyDialog(
        showDialog = if (inProduction) showDeveloperOptionsEnabled else false,
        title = "Caution",
        text = "You need to disable developer options to proceed further.",
        acceptButtonText = "Developer Options",
        cancelButtonText = "Cancel",
        onAcceptFn = {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            context.startActivity(intent)
        },
        onDismissFn = {
            showDeveloperOptionsEnabled = false
            updateSelectedPaymentMethod(qrCodePaymentMethod)
        },
    )

    MyDialog(
        showDialog = showNFCNotEnabled,
        title = "NFC Required",
        text = "This feature needs NFC. Please enable it in your device settings.",
        acceptButtonText = "Go to Settings",
        cancelButtonText = "Cancel",
        onAcceptFn = {
            val intent = Intent(Settings.ACTION_NFC_SETTINGS)
            context.startActivity(intent)
        },
        onDismissFn = {
            showNFCNotEnabled = false
            updateSelectedPaymentMethod(qrCodePaymentMethod)
        },
    )

    if (startTapAndPay && selectedPaymentMethod === tapPaymentMethod) {
        AppLogger.debug(
            "TapToPay gate check: startTapAndPay=$startTapAndPay, selected=${selectedPaymentMethod.text}, inProduction=$inProduction, " +
                    "developerOptionsEnabled=${DeveloperOptions.isEnabled(context)}, nfcEnabled=${nfcState.second}, nfcState=${nfcState.first}"
        )

        if (inProduction && DeveloperOptions.isEnabled(context)) {
            AppLogger.warn("TapToPay blocked: developer options are enabled on production build")
            showDeveloperOptionsEnabled = true
        } else if (inProduction && !Nfc.getStatus(context).second) {
            AppLogger.warn("TapToPay blocked: NFC is disabled on production build")
            showNFCNotEnabled = true
        } else {

            AppLogger.debug("TapToPay checks passed, launching Tap_ChargeMoney")
            Tap_ChargeMoney(
                navController = navController,
                amountToCharge = amountToCharge,
                onLoader = onLoader,
                onSuccessUpdateFlowStage = onSuccessUpdateFlowStage,
                onFailureUpdateFlowStage = onFailureUpdateFlowStage,
                updateLatestTransaction = updateLatestTransaction
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
            .then(if (enableScrolling) Modifier.verticalScroll(rememberScrollState()) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        if(availablePaymentMethods.size == 1){
            // select the one item by default
            updateSelectedPaymentMethod(availablePaymentMethods.first())
        } else {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBackgroundColor)
                    .innerShadow(
                        color = innerShadow,
                        blur = 8.dp,
                        spread = 5.dp,
                        cornersRadius = 8.dp,
                        offsetX = 0.dp,
                        offsetY = 0.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                availablePaymentMethods.filter { it.isEnabled }.forEach {
                    PaymentMethodButton(
                        paymentMethod = it,
                        selectedPaymentMethod = selectedPaymentMethod,
                        onSelected = { updateSelectedPaymentMethod(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "You are receiving",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = primary900,
            )

            Spacer(modifier = Modifier.height(8.dp))

            CurrencyText(currency = currency, amount = amountToCharge, fontWeight = FontWeight(990))

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                text = "Change Amount",
                modifier = Modifier
                    .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                    .height(35.dp)
                    .scale(0.7f),
                onClick = onChangeAmount
            )
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun Tap_ChargeMoney(
    navController: NavController,
    amountToCharge: String,
    onLoader: (nextStage: () -> Unit) -> Unit = {},
    onSuccessUpdateFlowStage: () -> Unit = {},
    onFailureUpdateFlowStage: () -> Unit = {},
    updateLatestTransaction: (id: String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var rawInput = ""
    var paymentLoader by remember { mutableStateOf(false) }
    var showProcessingScreen by remember { mutableStateOf(false) }
    var transactionDetailsText by remember { mutableStateOf("") }
    var hasLaunchedPayment by remember { mutableStateOf(false) }
    val authDetails = DPStorageManager.getAuthDetails()
    AppLogger.debug("TapToPay entry: amount=$amountToCharge, authAvailable=${authDetails != null}")

    if (authDetails == null) {
        AppLogger.warn("TapToPay aborted: auth details not found, redirecting to auth")
        Toast.makeText(
            context, "Your session has expired. Please log in again to continue.", Toast.LENGTH_LONG
        ).show()
        DPStorageManager.clearSharedPreferences()
        navController.navigate(Screens.AuthCheck.route) {
            popUpTo(0) { inclusive = true }
        }
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
                Toast.makeText(context, mineSecFailureMessage, Toast.LENGTH_LONG).show()

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
                    Toast.makeText(
                        context,
                        "Your session has expired. Please log in again to continue.",
                        Toast.LENGTH_LONG
                    ).show()
                    DPStorageManager.clearSharedPreferences()
                    navController.navigate(Screens.AuthCheck.route) {
                        popUpTo(0) { inclusive = true }
                    }
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