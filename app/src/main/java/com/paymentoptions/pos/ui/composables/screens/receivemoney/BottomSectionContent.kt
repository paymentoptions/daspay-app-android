package com.paymentoptions.pos.ui.composables.screens.receivemoney

import MyDialog
import android.app.Activity
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.Address
import com.paymentoptions.pos.services.apiService.PaymentMethod
import com.paymentoptions.pos.services.apiService.PaymentRequest
import com.paymentoptions.pos.services.apiService.PaymentResponse
import com.paymentoptions.pos.services.apiService.PaymentReturnUrl
import com.paymentoptions.pos.services.apiService.endpoints.payment
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.inputs.DashedBorderInput
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.noBorder
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary600
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.decodeJwtPayload
import com.paymentoptions.pos.utils.getDasmidFromToken
import com.paymentoptions.pos.utils.getDeviceIpAddress
import com.paymentoptions.pos.utils.getDeviceTimeZone
import com.paymentoptions.pos.utils.getKeyFromToken
import com.paymentoptions.pos.utils.modifiers.conditional
import com.paymentoptions.pos.utils.modifiers.noRippleClickable
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
fun BottomSectionContent(navController: NavController, enableScrolling: Boolean = false) {
    val scrollState = rememberScrollState()
    var showNFCNotPresent by remember { mutableStateOf(false) }
    var showTransactionStatus by remember { mutableStateOf(false) }
    var showNFCNotEnabled by remember { mutableStateOf(false) }
    var paymentLoader by remember { mutableStateOf(false) }
    var showDeveloperOptionsEnabled by remember { mutableStateOf(false) }
    var rawInput by remember { mutableStateOf("") }
    var posReferenceId by remember { mutableStateOf("-") }
    var transactionDetailsText by remember { mutableStateOf("") }

    fun formatAmount(input: String): String {
        if (input.isEmpty()) return "0.00"
        val cents = input.toLong()
        val dollars = cents / 100
        val centPortion = (cents % 100).toString().padStart(2, '0')
        return "$dollars.$centPortion"
    }

    val formattedAmount = formatAmount(rawInput)

    val onKeyPress: (String) -> Unit = { key ->
        when (key) {
            "←" -> {
                if (rawInput.isNotEmpty()) rawInput = rawInput.dropLast(1)
            }

            else -> {
                // Avoid too many digits
                if (rawInput.length < 9) rawInput += key
            }
        }
    }

    val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("00", "0", "←"),
    )
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    var showReceiveMoney by remember { mutableStateOf(true) }

    val authDetails = SharedPreferences.getAuthDetails(context)

    val noteState = rememberTextFieldState()

    if (authDetails == null) {
        Toast.makeText(context, "Token invalid! Please login again.", Toast.LENGTH_LONG).show()
        navController.navigate(Screens.SignIn.route) {
            popUpTo(0) { inclusive = true }
        }
        return
    }

    val merchant: MutableMap<String, String> = mutableMapOf<String, String>()
    val decodedJwtPayloadJson = decodeJwtPayload(authDetails.data.token.idToken)
    val currency = "HKD"


    merchant["dasmid"] = getDasmidFromToken(decodedJwtPayloadJson)
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
                    "Transaction of $$formattedAmount was successful. POS Reference Transaction ID returned by MineSec is: $completedSalePosReference."
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
                    context, "Transaction of $$formattedAmount was failed", Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    MyDialog(
        showDialog = showDeveloperOptionsEnabled,
        title = "Error",
        text = "You need to disable developer options to proceed further.",
        acceptButtonText = "Exit",
        cancelButtonText = "Developer Options",
        onAcceptFn = {
            showDeveloperOptionsEnabled = false
            activity?.finish()
        },
        onDismissFn = {
            showDeveloperOptionsEnabled = false
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            context.startActivity(intent)
//            activity?.finish()
        },
    )

    MyDialog(
        showDialog = showNFCNotPresent,
        title = "NFC Required",
        text = "Your device does not support NFC.",
        acceptButtonText = "Exit",
        cancelButtonText = "Cancel",
        onAcceptFn = {
            showNFCNotPresent = false
            activity?.finish()
        },
        onDismissFn = { showNFCNotPresent = false },
    )

    MyDialog(
        showDialog = showNFCNotEnabled,
        title = "NFC Required",
        text = "This feature needs NFC. Please enable it in your device settings.",
        acceptButtonText = "Go to Settings",
        cancelButtonText = "Cancel",
        onAcceptFn = {
            showNFCNotEnabled = false
            val intent = Intent(Settings.ACTION_NFC_SETTINGS)
            context.startActivity(intent)
        },
        onDismissFn = { showNFCNotEnabled = false },
    )


    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .padding(bottom = 0.dp)
                .background(iconBackgroundColor),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "Receive Money",
                modifier = Modifier
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (showReceiveMoney) Color.White.copy(alpha = 0.9f) else Color.Transparent)
                    .padding(10.dp)
                    .weight(1f)
                    .noRippleClickable(enabled = !showReceiveMoney) {
                        showReceiveMoney = true
                    },
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = primary600
            )
            Text(
                text = "Pre-Authorize",
                modifier = Modifier
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (!showReceiveMoney) Color.White.copy(alpha = 0.9f) else Color.Transparent)
                    .padding(10.dp)
                    .weight(1f)
                    .noRippleClickable(enabled = showReceiveMoney) {
                        showReceiveMoney = false
                    },
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = primary600
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (showReceiveMoney) "Receive Money" else "Pre-Authorize",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = primary900,
            )

            Spacer(modifier = Modifier.height(8.dp))
            CurrencyText(currency = currency, amount = formattedAmount)
        }

        Spacer(modifier = Modifier.height(16.dp))

        DashedBorderInput(
            state = noteState,
            modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            placeholder = "Add a note (optional)",
            maxLength = 50,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .fillMaxWidth()
                .background(Color.White)
                .conditional(enableScrolling) {
                    weight(1f).verticalScroll(scrollState)
                }) {

            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(70.dp)
                                .border(
                                    2.dp, primary100.copy(alpha = 0.2f), RoundedCornerShape(8.dp)
                                )
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .clickable { onKeyPress(key) }, contentAlignment = Alignment.Center
                        ) {
                            if (key == "←") {
                                Icon(
                                    Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "Back",
                                    tint = primary500,
                                )
                            } else {
                                Text(
                                    key,
                                    color = primary500,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item {
                SuggestionChip(
                    border = noBorder,
                    colors = SuggestionChipDefaults.suggestionChipColors()
                        .copy(containerColor = primary100.copy(alpha = 0.15f)),
                    onClick = { rawInput = "1000" },
                    label = {
                        CurrencyText(currency = currency, amount = "10.00", fontSize = 16.sp)
                    })
            }

            items(20) {
                SuggestionChip(
                    border = noBorder,
                    colors = SuggestionChipDefaults.suggestionChipColors()
                        .copy(containerColor = primary100.copy(alpha = 0.15f)),
                    onClick = { rawInput = "${(it + 1) * 50}00" },
                    label = {
                        CurrencyText(
                            currency = currency, amount = "${(it + 1) * 50}", fontSize = 16.sp
                        )
                    })
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        FilledButton(
            text = "Charge",
            disabled = rawInput.isEmpty() || rawInput.toLong() <= 0,
            isLoading = paymentLoader,
            modifier = Modifier
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .fillMaxWidth()
                .height(59.dp),
            onClick = {

                //Toast.makeText(context, "Under development", Toast.LENGTH_SHORT).show()

                //Disabled

                if (!DeveloperOptions.isEnabled(context)) {
                    showDeveloperOptionsEnabled = true
                } else {
                    var nfcStatusPair = Nfc.getStatus(context)

                    if (!nfcStatusPair.first) {
                        showNFCNotPresent = true
                    } else if (!nfcStatusPair.second) {
                        showNFCNotEnabled = true
                    } else {
                        val uuid: UUID = UUID.randomUUID()
                        posReferenceId = uuid.toString()

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

                        val paymentMethod = PaymentMethod(
                            type = "daspay"
                        )

                        val paymentRequest = PaymentRequest(
                            amount = formattedAmount,
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
                        //Log.d(")
                        scope.launch {
                            paymentLoader = true

                            try {

                                val paymentResponse: PaymentResponse? =
                                    payment(context, paymentRequest)
                                println("paymentResponse: $paymentResponse")

                                if (paymentResponse == null) {
                                    Toast.makeText(
                                        context,
                                        "2: Token invalid! Please login again.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.navigate(Screens.SignIn.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }

                                paymentResponse?.let {
                                    if (it.success) {
                                        launcher.launch(
                                            PoiRequest.ActionNew(
                                                tranType = TranType.SALE,
                                                amount = Amount(
                                                    BigDecimal(formattedAmount),
                                                    Currency.getInstance("USD"),
                                                ),
                                                profileId = "prof_01HYYPGVE7VB901M40SVPHTQ0V",
                                                posReference = it.transaction_details.id
                                            )
                                        )
                                    }
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
                        }
                    }
                }
            })
    }
}
