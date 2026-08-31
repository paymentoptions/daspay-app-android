package com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow.chargemoney

import MyDialog
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
import com.paymentoptions.pos.device.DeveloperOptions
import com.paymentoptions.pos.device.Nfc
import com.paymentoptions.pos.device.DPSharedPreferences
import com.paymentoptions.pos.device.DPSharedPreferences.getTapPayDasmid
import com.paymentoptions.pos.device.DPSharedPreferences.getTransactionCurrency
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.Address
import com.paymentoptions.pos.services.apiService.PaymentRequest
import com.paymentoptions.pos.services.apiService.PaymentResponse
import com.paymentoptions.pos.services.apiService.PaymentReturnUrl
import com.paymentoptions.pos.services.apiService.PaymentStatusRequest
import com.paymentoptions.pos.services.apiService.endpoints.payment
import com.paymentoptions.pos.services.apiService.endpoints.sendWebHookNotification
import com.paymentoptions.pos.services.analytics.AppAnalytics
import com.paymentoptions.pos.services.apiService.endpoints.payByLink
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
import com.paymentoptions.pos.utils.PaymentMethod
import com.paymentoptions.pos.utils.decodeJwtPayload
import com.paymentoptions.pos.utils.getDeviceIpAddress
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
import com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow.Cart
import com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow.getPayByLinkRequest
import com.paymentoptions.pos.utils.parseApiErrorMessage
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
    gatewayNotes: String?,
    availablePaymentMethods: List<PaymentMethod>,
    selectedPaymentMethod: PaymentMethod,
    updateSelectedPaymentMethod: (PaymentMethod) -> Unit = {},
    onLoader: (nextStage: () -> Unit) -> Unit = {},
    onSuccessUpdateFlowStage: () -> Unit = {},
    onMandatorySignature: () -> Unit = {},
    onFailureUpdateFlowStage: () -> Unit = {},
    updateFailureMessage: (String?) -> Unit = {},
    onChangeAmount: () -> Unit,
    startTapAndPay: Boolean = false,
    updateLatestTransaction: (id: String) -> Unit,
    cart: Cart? = null,
) {
    val context = LocalContext.current
    val currency = getTransactionCurrency(context)
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
            "TapToPay gate check: startTapAndPay=$startTapAndPay, selected=${selectedPaymentMethod.text}, inProduction=$inProduction, developerOptionsEnabled=${DeveloperOptions.isEnabled(context)}, nfcEnabled=${nfcState.second}, nfcState=${nfcState.first}"
        )

        if (inProduction && DeveloperOptions.isEnabled(context)) {
            AppLogger.warn("TapToPay blocked: developer options are enabled on production build")
            showDeveloperOptionsEnabled = true
        } else if (inProduction && !Nfc.getStatus(context).second) {
            AppLogger.warn("TapToPay blocked: NFC is disabled on production build")
            showNFCNotEnabled = true
        } else {
            updateFailureMessage(null)
            AppAnalytics.tapToPayInitiated(amount = amountToCharge, currency = currency)
            AppLogger.debug("TapToPay checks passed, launching Tap_ChargeMoney")
            Tap_ChargeMoney(
                navController = navController,
                amountToCharge = amountToCharge,
                gatewayNotes = gatewayNotes,
                onLoader = onLoader,
                onSuccessUpdateFlowStage = onSuccessUpdateFlowStage,
                onMandatorySignature = onMandatorySignature,
                onFailureUpdateFlowStage = onFailureUpdateFlowStage,
                onFailureMessage = updateFailureMessage,
                updateLatestTransaction = updateLatestTransaction,
                cartState = cart
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
                        onSelected = {
                            AppAnalytics.criticalButtonClick(
                                buttonName = "select_payment_method_${it.text.lowercase().replace(" ", "_")}",
                                screen = "receive_money"
                            )
                            updateSelectedPaymentMethod(it)
                        },
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
    gatewayNotes: String?,

    onLoader: (nextStage: () -> Unit) -> Unit = {},
    onSuccessUpdateFlowStage: () -> Unit = {},
    onMandatorySignature: () -> Unit = {},
    onFailureUpdateFlowStage: () -> Unit = {},
    onFailureMessage: (String) -> Unit = {},
    updateLatestTransaction: (id: String) -> Unit = {},
    cartState: Cart? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var paymentLoader by remember { mutableStateOf(false) }
    var showProcessingScreen by remember { mutableStateOf(false) }
    var transactionDetailsText by remember { mutableStateOf("") }
    var hasLaunchedPayment by remember { mutableStateOf(false) }
    val authDetails = DPSharedPreferences.getAuthDetails(context)
    AppLogger.debug("TapToPay entry: amount=$amountToCharge, authAvailable=${authDetails != null}")

    if (authDetails == null) {
        AppLogger.warn("TapToPay aborted: auth details not found, redirecting to auth")
        Toast.makeText(
            context, "Your session has expired. Please log in again to continue.", Toast.LENGTH_LONG
        ).show()
        DPSharedPreferences.clearSharedPreferences(context)
        navController.navigate(Screens.AuthCheck.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    
    val merchant: MutableMap<String, String> = mutableMapOf<String, String>()
    val decodedJwtPayloadJson = decodeJwtPayload(authDetails!!.data.token.idToken)
    val currency = getTransactionCurrency(context)

    merchant["dasmid"] = getTapPayDasmid(context)
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
                AppAnalytics.profileDownloadOrActivation(
                    profileId = DPSharedPreferences.getMerchantProfileId(context) ?:"",
                    stage = "activation",
                    result = "completed"
                )
                if (it.value.tranType == TranType.SALE) {
                    completedSaleTranId = it.value.tranId
                    completedSalePosReference = it.value.posReference
                    completedSaleRequestId = it.value.actions.firstOrNull()?.requestId
                }

                AppAnalytics.paymentStatus(
                    status = it.value.tranStatus.name,
                    paymentType = "SOFTPOS",
                    transactionId = completedSaleTranId
                )

                transactionDetailsText =
                    "Transaction of $$amountToCharge was successful. POS Reference Transaction ID returned by MineSec is: $completedSalePosReference."
                AppLogger.debug(
                    "TapToPay MineSec sale extracted: saleTranId=$completedSaleTranId, salePosReference=$completedSalePosReference, saleRequestId=$completedSaleRequestId"
                )


                val paymentStatusRequest = createPaymentRequest(it.value)
                AppLogger.debug("TapToPay webhook payload built: $paymentStatusRequest")

                scope.launch {
                    showProcessingScreen = true
                    AppLogger.debug("TapToPay webhook call started")
                    try {
                        val paymentStatusResponse =
                            sendWebHookNotification(context = context, request = paymentStatusRequest, it.value.tranStatus)

                        showProcessingScreen = false
                        AppLogger.debug("TapToPay webhook response: success=$paymentStatusResponse, tranId=${paymentStatusRequest.tranId}")
                        if (paymentStatusResponse) {
                            updateLatestTransaction(paymentStatusRequest.tranId.toString())
                            AppLogger.debug("TapToPay flow moving to SUCCESS stage for tranId=${paymentStatusRequest.tranId}")

                            val cvmLimit = DPSharedPreferences.getCvmLimit(context)
                            val amount = amountToCharge.toDoubleOrNull() ?: 0.0
                            if (amount >= cvmLimit) {
                                AppLogger.debug("TapToPay: Amount $amount >= CVM Limit $cvmLimit, moving to MANDATORY_SIGNATURE")
                                onLoader { onMandatorySignature() }
                            } else {
                                onLoader { onSuccessUpdateFlowStage() }
                            }
                        } else {
                            updateLatestTransaction(paymentStatusRequest.tranId.toString())
                            AppLogger.warn("TapToPay flow moving to FAILURE stage due to false webhook response for tranId=${paymentStatusRequest.tranId}")
                            onFailureMessage("Tap to Pay failed while confirming payment status. Please retry.")
                            onLoader {
                                onFailureUpdateFlowStage()
                            }
                        }
                    } catch (e: Exception) {
                        AppLogger.error("TapToPay webhook exception for tranId=${paymentStatusRequest.tranId}: ${e.message}", e)
                        AppAnalytics.paymentStatus(
                            status = "FAILED",
                            paymentType = "SOFTPOS",
                            transactionId = paymentStatusRequest.tranId
                        )
                        showProcessingScreen = false
                        updateLatestTransaction(paymentStatusRequest.tranId.toString())
                        onFailureMessage(e.message ?: "Tap to Pay failed during payment confirmation.")
                        onLoader {
                            onFailureUpdateFlowStage()
                        }
                    }
                }
            }

            is WrappedResult.Failure -> {
                val mineSecFailureMessage =
                    it.message ?: it.extra?.get("message")?.toString() ?: "Tap to Pay was cancelled or declined."
                AppLogger.error(
                    "TapToPay MineSec failure: message=${it.message}, extraMessage=${it.extra?.get("message")}, raw=$it"
                )
                AppAnalytics.paymentStatus(
                    status = it.toString(),
                    paymentType = "SOFTPOS",
                    transactionId = null
                )
                onFailureMessage(mineSecFailureMessage)
                Toast.makeText(context, mineSecFailureMessage, Toast.LENGTH_LONG).show()
                onLoader {
                    onFailureUpdateFlowStage()
                }
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

    val paymentMethod = com.paymentoptions.pos.services.apiService.PaymentMethod(type = "daspay")


    MyAlertDialog(
        showDialog = paymentLoader,
        text = "Loading Tap to Pay...Please wait a moment.",
        actionButtonText = "Try Again",
        type = AlertDialogType.LOADER,
        showActionButton = false,
        onActionFn = {})

    if (!hasLaunchedPayment) {
        //prof_01KH8NQC4PVFKRNH31ZPC2QJNN
        val profileId = DPSharedPreferences.getMerchantProfileId(context) ?: ""
        hasLaunchedPayment = true
        AppLogger.debug("TapToPay launching MineSec payment API call with profileId=$profileId")

        scope.launch {
            paymentLoader = true
            AppLogger.debug("TapToPay loader enabled")
            try {
                var productId = ""
                if(cartState != null) {
                    try {
                        var payByLinkRequest = getPayByLinkRequest(
                            "Tap to Pay ",
                            cartState, currency
                        )
                        val dasmid = getTapPayDasmid(context)

                        var payByLinkResponse = payByLink(context, payByLinkRequest, dasmid)

                        println("payByLinkResponse: $payByLinkResponse")

                        if (payByLinkResponse != null && payByLinkResponse.data.ProductID.isNotBlank()) {
                            AppLogger.debug("TapToPay payment request added Product id $productId for $payByLinkResponse")
                            productId = payByLinkResponse.data.ProductID
                        }
                        DPSharedPreferences.clearSavedCart(context)
                    } catch (exception: Exception) {
                        val errorMessage = parseApiErrorMessage(exception, "Tap to Pay failed to start. Please retry.")

                        AppLogger.error("TapToPay error during payment setup: $errorMessage", errorMessage)
                    }

                }

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
                    cartID = productId
                )
                AppLogger.debug("TapToPay payment request prepared: amount=${paymentRequest.amount}, currency=${paymentRequest.currency}, merchantId=${paymentRequest.merchant_id}, tz=${paymentRequest.time_zone}")


                val paymentResponse: PaymentResponse? = payment(context, paymentRequest)
                AppLogger.debug("TapToPay payment API response: $paymentResponse")
                if (paymentResponse == null) {
                    AppLogger.warn("TapToPay payment API returned null (likely auth/session issue)")
                    Toast.makeText(
                        context,
                        "Your session has expired. Please log in again to continue.",
                        Toast.LENGTH_LONG
                    ).show()
                    DPSharedPreferences.clearSharedPreferences(context)
                    navController.navigate(Screens.AuthCheck.route) {
                        popUpTo(0) { inclusive = true }
                    }
                    return@launch
                }

                paymentResponse?.let {
                    AppLogger.debug("TapToPay payment API parsed response: success=${it.success}, transactionId=${it.transaction_details.id}")
                    if (it.success) {
                        AppAnalytics.profileDownloadOrActivation(
                            profileId = profileId,
                            stage = "activation",
                            result = "started"
                        )
                        AppLogger.debug(
                            "TapToPay launching MineSec SALE with profileId=$profileId, posReference=${it.transaction_details.id}, amount=$amountToCharge, currency=${getTransactionCurrency(context)}"
                        )
                        launcher.launch(
                            PoiRequest.ActionNew(
                                tranType = TranType.SALE,
                                amount = Amount(
                                    BigDecimal(amountToCharge),
                                    Currency.getInstance(getTransactionCurrency(context)),
                                ),
                                profileId = profileId,
                                description = gatewayNotes?.trim(),
                                posReference = it.transaction_details.id
                            )
                        )
                    } else {
                        AppLogger.warn("TapToPay payment API returned success=false; MineSec launch skipped")
                        onFailureMessage("Unable to initialize Tap to Pay (status ${it.status_code}). Please try again.")
                        onLoader {
                            onFailureUpdateFlowStage()
                        }
                    }
                }
            } catch (e: retrofit2.HttpException) {
                AppLogger.error("TapToPay HTTP error during payment setup: ${e.message}", e)
                val errorMessage = parseApiErrorMessage(e, "Tap to Pay failed to start. Please retry.")
                AppAnalytics.paymentStatus(
                    status = "FAILED",
                    paymentType = "SOFTPOS",
                    transactionId = null
                )
                onFailureMessage(errorMessage)
                onLoader {
                    onFailureUpdateFlowStage()
                }
            }
            catch (e: Exception) {
                AppLogger.error("TapToPay fatal error during payment setup: ${e.message}", e)
                val errorMessage = parseApiErrorMessage(e, "Tap to Pay failed to start. Please retry.")
                AppAnalytics.paymentStatus(
                    status = "FAILED",
                    paymentType = "SOFTPOS",
                    transactionId = null
                )
                onFailureMessage(errorMessage)
                onLoader {
                    onFailureUpdateFlowStage()
                }
//                DPSharedPreferences.clearSharedPreferences(context)
//                navController.navigate(Screens.AuthCheck.route) {
//                    popUpTo(0) { inclusive = true }
//                }
            } finally {
                paymentLoader = false
                AppLogger.debug("TapToPay loader disabled")
            }
        }
    }
}