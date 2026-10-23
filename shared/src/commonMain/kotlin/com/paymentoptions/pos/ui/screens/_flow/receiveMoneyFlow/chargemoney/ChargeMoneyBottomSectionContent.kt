package com.paymentoptions.pos.ui.screens._flow.receiveMoneyFlow.chargemoney

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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.device.getNfcStatus
import com.paymentoptions.pos.device.isDeveloperOptionsEnabled
import com.paymentoptions.pos.device.openDevelopmentSettings
import com.paymentoptions.pos.device.openNfcSettings
import com.paymentoptions.pos.device.DPStorageManager.getTransactionCurrency
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.payment.TapChargeMoney
import com.paymentoptions.pos.payment.reader.ReaderState
import com.paymentoptions.pos.payment.reader.getTapToPayReader
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.buttons.OutlinedButton
import com.paymentoptions.pos.ui.composables._components.dialogs.MyDialog
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.tapToPay.EnableTapToPayFlow
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.innerShadow
import com.paymentoptions.pos.ui.theme.primary600
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.PaymentMethod
import com.paymentoptions.pos.utils.inProduction
import com.paymentoptions.pos.utils.modifiers.innerShadow
import com.paymentoptions.pos.utils.modifiers.noRippleClickable
import com.paymentoptions.pos.utils.qrCodePaymentMethod
import com.paymentoptions.pos.utils.tapPaymentMethod

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
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            paymentMethod.icon,
            contentDescription = paymentMethod.text,
            tint = primary600,
        )

        Text(
            text = paymentMethod.text,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = primary600,
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
    onFailureUpdateFlowStage: () -> Unit = {},
    onChangeAmount: () -> Unit,
    startTapAndPay: Boolean = false,
    updateLatestTransaction: (id: String) -> Unit,
    updateFailureMessage: (String?) -> Unit = {},
    onMandatorySignature: () -> Unit = {},
) {
    val currency = getTransactionCurrency()
    val nfcState = getNfcStatus()
    val reader = remember { getTapToPayReader() }

    var showDeveloperOptionsEnabled by remember { mutableStateOf(false) }
    var showNFCNotEnabled by remember { mutableStateOf(false) }
    var showEnableTapToPayFlow by remember { mutableStateOf(false) }
    var readerReadyState by remember(startTapAndPay, selectedPaymentMethod) {
        mutableStateOf<ReaderState?>(null)
    }

    MyDialog(
        showDialog = if (inProduction) showDeveloperOptionsEnabled else false,
        title = "Caution",
        text = "You need to disable developer options to proceed further.",
        acceptButtonText = "Developer Options",
        cancelButtonText = "Cancel",
        onAcceptFn = { openDevelopmentSettings() },
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
        onAcceptFn = { openNfcSettings() },
        onDismissFn = {
            showNFCNotEnabled = false
            updateSelectedPaymentMethod(qrCodePaymentMethod)
        },
    )

    if (startTapAndPay && selectedPaymentMethod === tapPaymentMethod) {
        AppLogger.debug(
            "TapToPay gate check: startTapAndPay=$startTapAndPay, selected=${selectedPaymentMethod.text}, inProduction=$inProduction, " +
                    "developerOptionsEnabled=${isDeveloperOptionsEnabled()}, nfcEnabled=${nfcState.second}, nfcState=${nfcState.first}",
        )

        if (inProduction && isDeveloperOptionsEnabled()) {
            AppLogger.warn("TapToPay blocked: developer options are enabled on production build")
            showDeveloperOptionsEnabled = true
        } else if (!inProduction) {
            // Non-production builds skip the reader-readiness gate so Tap to Pay can be
            // exercised without a real/linked reader, matching the prior behaviour.
            updateFailureMessage(null)
            AppLogger.debug("TapToPay checks passed (non-production), launching TapChargeMoney")
            TapChargeMoney(
                navController = navController,
                amountToCharge = amountToCharge,
                gatewayNotes = gatewayNotes,
                onLoader = onLoader,
                onSuccessUpdateFlowStage = onSuccessUpdateFlowStage,
                onMandatorySignature = onMandatorySignature,
                onFailureUpdateFlowStage = onFailureUpdateFlowStage,
                onFailureMessage = { updateFailureMessage(it) },
                updateLatestTransaction = updateLatestTransaction,
            )
        } else {
            LaunchedEffect(startTapAndPay, selectedPaymentMethod) {
                readerReadyState = reader.ensureReadyForPayment()
            }

            when (val state = readerReadyState) {
                null, ReaderState.NotInitialized, ReaderState.Initializing, is ReaderState.Configuring -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.Center)
                            .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                ReaderState.Ready -> {
                    updateFailureMessage(null)
                    AppLogger.debug("TapToPay checks passed, launching TapChargeMoney")
                    TapChargeMoney(
                        navController = navController,
                        amountToCharge = amountToCharge,
                        gatewayNotes = gatewayNotes,
                        onLoader = onLoader,
                        onSuccessUpdateFlowStage = onSuccessUpdateFlowStage,
                        onMandatorySignature = onMandatorySignature,
                        onFailureUpdateFlowStage = onFailureUpdateFlowStage,
                        onFailureMessage = { updateFailureMessage(it) },
                        updateLatestTransaction = updateLatestTransaction,
                    )
                }

                ReaderState.NotLinked -> {
                    AppLogger.warn("TapToPay blocked: account not linked — opening enable flow")
                    showEnableTapToPayFlow = true
                }

                is ReaderState.Failed -> {
                    AppLogger.warn("TapToPay blocked: reader not ready ($state)")
                    showNFCNotEnabled = true
                }
            }
        }
    }

    // The Tap to Pay chip itself is never disabled/hidden while NotLinked (3.7/5.3) — tapping it
    // just opens the same enable flow used by Settings and the post-login prompt (admin) or the
    // staff message (non-admin), handled above and rendered here.
    if (showEnableTapToPayFlow) {
        EnableTapToPayFlow(onDismiss = { showEnableTapToPayFlow = false })
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
            .then(if (enableScrolling) Modifier.verticalScroll(rememberScrollState()) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (availablePaymentMethods.size == 1) {
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
                        offsetY = 0.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                availablePaymentMethods.filter { it.isEnabled }.forEach {
                    PaymentMethodButton(
                        paymentMethod = it,
                        selectedPaymentMethod = selectedPaymentMethod,
                        onSelected = {
                            updateSelectedPaymentMethod(it)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            horizontalAlignment = Alignment.CenterHorizontally,
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
                onClick = onChangeAmount,
            )
        }
    }
}
