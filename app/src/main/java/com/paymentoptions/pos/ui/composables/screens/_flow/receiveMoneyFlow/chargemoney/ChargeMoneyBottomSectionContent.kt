package com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow.chargemoney

import MyDialog
import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
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
import com.paymentoptions.pos.device.Nfc
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.device.getDasmid
import com.paymentoptions.pos.device.getTransactionCurrency
import com.paymentoptions.pos.services.apiService.Address
import com.paymentoptions.pos.services.apiService.PaymentRequest
import com.paymentoptions.pos.services.apiService.PaymentResponse
import com.paymentoptions.pos.services.apiService.PaymentReturnUrl
import com.paymentoptions.pos.services.apiService.endpoints.payment
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.buttons.OutlinedButton
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
import com.paymentoptions.pos.utils.modifiers.innerShadow
import com.paymentoptions.pos.utils.modifiers.noRippleClickable
import com.paymentoptions.pos.utils.paymentMethods
import com.paymentoptions.pos.utils.qrCodePaymentMethod
import com.theminesec.lib.dto.common.Amount
import com.theminesec.lib.dto.poi.PoiRequest
import com.theminesec.lib.dto.transaction.TranType
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.model.WrappedResult
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Currency
import java.util.UUID

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
    selectedPaymentMethod: PaymentMethod,
    updateSelectedPaymentMethod: (PaymentMethod) -> Unit = {},
    updateFlowStage: (Any) -> Unit = {},
    onChangeAmount: () -> Unit,
) {
    val context = LocalContext.current
    val currency = getTransactionCurrency(context)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
            .verticalScroll(state = rememberScrollState(), enabled = enableScrolling),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (selectedPaymentMethod === qrCodePaymentMethod) {
            Tap_ChargeMoney(navController, amountToCharge)
        }

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
            paymentMethods.filter { it.isEnabled }.forEach {
                PaymentMethodButton(
                    paymentMethod = it,
                    selectedPaymentMethod = selectedPaymentMethod,
                    onSelected = { updateSelectedPaymentMethod(it) },
                    modifier = Modifier.weight(1f)
                )
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
fun Tap_ChargeMoney(navController: NavController, amountToCharge: String) {

    println("amountToCharge: $amountToCharge")
    val context = LocalContext.current
    context as? Activity
    val scope = rememberCoroutineScope()
    var rawInput = ""
    var paymentLoader = false
    var transactionDetailsText by remember { mutableStateOf("") }
    var showTransactionStatus by remember { mutableStateOf(false) }


    val authDetails = SharedPreferences.getAuthDetails(context)

    if (authDetails == null) {
        Toast.makeText(context, "Token invalid! Please login again.", Toast.LENGTH_LONG).show()
        navController.navigate(Screens.SignIn.route) {
            popUpTo(0) { inclusive = true }
        }
        return
    }

    val merchant: MutableMap<String, String> = mutableMapOf<String, String>()
    val decodedJwtPayloadJson = decodeJwtPayload(authDetails.data.token.idToken)
    val currency = getTransactionCurrency(context)

    merchant["dasmid"] = getDasmid(context)
    merchant["name"] = getKeyFromToken(decodedJwtPayloadJson, "name")
    merchant["email"] = getKeyFromToken(decodedJwtPayloadJson, "email")
    merchant["contact"] = getKeyFromToken(decodedJwtPayloadJson, "custom:ContactNo")

    MyDialog(
        showDialog = showTransactionStatus,
        title = "Transaction Status",
        text = transactionDetailsText,
        acceptButtonText = "Ok",
        showCancelButton = false,
        onAcceptFn = { showTransactionStatus = false },
        onDismissFn = { showTransactionStatus = false },
    )


    val launcher = rememberLauncherForActivityResult(
        HeadlessActivity.contract(ClientHeadlessImpl::class.java)
    ) {

        var completedSaleTranId: String? = ""
        var completedSalePosReference: String? = ""
        var completedSaleRequestId: String? = ""


//            viewModel.resetRandomPosReference()
//            viewModel.writeMessage("ActivityResult: $it")
        when (it) {
            is WrappedResult.Success -> {
                if (it.value.tranType == TranType.SALE) {

                    completedSaleTranId = it.value.tranId
                    completedSalePosReference = it.value.posReference
                    completedSaleRequestId = it.value.actions.firstOrNull()?.requestId
                }

                transactionDetailsText =
                    "Transaction of $$amountToCharge was successful. POS Reference Transaction ID returned by MineSec is: $completedSalePosReference."
                showTransactionStatus = true
                Log.d(
                    "Success ->",
                    "completedSaleTranId: $completedSaleTranId | completedSalePosReference: $completedSalePosReference | completedSaleRequestId: $completedSaleRequestId | it: ${it.value}"
                )

                rawInput = ""

//                    if (it.value.tranType == TranType.REFUND) {
//                        completedRefundTranId = it.value.tranId
//                    }
            }

            is WrappedResult.Failure -> {
//                    viewModel.writeMessage("Failed")
                Log.d("Failed ->", "Payment Failed")
                Toast.makeText(
                    context, "Transaction of $$amountToCharge was failed", Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Nfc.getStatus(context)

    /*if (!nfcStatusPair.first) {
        showNFCNotPresent = true
    } else if (!nfcStatusPair.second) {
        showNFCNotEnabled = true
    } else {*/
    val uuid: UUID = UUID.randomUUID()
    uuid.toString()

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

    val shippingAddress = Address(
        country = "IN",
        email = merchant["email"]!!,
        address1 = "Chiyoda1-1",
        phone_number = merchant["contact"]!!,
        city = "Minatoku",
        state = "Tokyoto",
        postal_code = "100001"
    )

    val paymentMethod = com.paymentoptions.pos.services.apiService.PaymentMethod(
        type = "daspay"
    )

    val paymentRequest = PaymentRequest(
        amount = amountToCharge.toString(),
        currency = currency,
        merchant_txn_ref = "TEST00989012878787878787878787",
        customer_ip = getDeviceIpAddress(),
        merchant_id = merchant["dasmid"]!!,
        return_url = paymentReturnUrl,
        billing_address = billingAddress,
        shipping_address = shippingAddress,
        payment_method = paymentMethod,
        time_zone = getDeviceTimeZone()
    )

    scope.launch {
        paymentLoader = true

        try {
            val paymentResponse: PaymentResponse? = payment(context, paymentRequest)
            println("paymentResponse: $paymentResponse")

            if (paymentResponse == null) {
                Toast.makeText(
                    context, "Token invalid! Please login again.", Toast.LENGTH_LONG
                ).show()
                navController.navigate(Screens.SignIn.route) {
                    popUpTo(0) { inclusive = true }
                }
            }

            paymentResponse?.let {
//                if (it.success) {
                    launcher.launch(
                        PoiRequest.ActionNew(
                            tranType = TranType.SALE,
                            amount = Amount(
                                BigDecimal(amountToCharge),
                                Currency.getInstance(currency),
                            ),
//                            profileId = "prof_01HYYPGVE7VB901M40SVPHTQ0V",
                            profileId = "prof_01K36002RM7DMMPHG0QEX3E9BR",
                            posReference = it.transaction_details.id
                        )
                    )
//                }
            }
        } catch (e: Exception) {
            SharedPreferences.clearSharedPreferences(context)
            navController.navigate(Screens.SignIn.route) {
                popUpTo(0) { inclusive = true }
            }

            println("Error: ${e.toString()}")
        } finally {
            paymentLoader = false
        }
        // }
    }
}