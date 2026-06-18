package com.paymentoptions.pos.ui.screens._flow.receiveMoneyFlow
import paymentoptionspos.shared.generated.resources.logo
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res

import com.paymentoptions.pos.ui.composables._components.dialogs.MyDialog
import com.paymentoptions.pos.OnResume
import com.paymentoptions.pos.openDeveloperSettings
import com.paymentoptions.pos.openNfcSettings
import androidx.compose.ui.graphics.ImageBitmap
import com.paymentoptions.pos.device.GeoRestrictionManager
import com.paymentoptions.pos.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.analytics.AnalyticsHelper
import com.paymentoptions.pos.device.isDeveloperOptionsEnabled
import com.paymentoptions.pos.device.getNfcStatus
import com.paymentoptions.pos.device.ScreenRatioToDp
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.device.DPStorageManager.getApms
import com.paymentoptions.pos.device.DPStorageManager.getTransactionCurrency
import com.paymentoptions.pos.formatDate
import com.paymentoptions.pos.network.endpoints.payByLink
import com.paymentoptions.pos.network.endpoints.paymentDetails
import com.paymentoptions.pos.network.ApiHttpException
import com.paymentoptions.pos.network.PayByLinkRequest
import com.paymentoptions.pos.network.PayByLinkRequestProduct
import com.paymentoptions.pos.network.PayByLinkResponse
import com.paymentoptions.pos.network.PaymentDetailsResponse
import com.paymentoptions.pos.network.SignatureData
import com.paymentoptions.pos.ui.composables._components.MyCircularProgressIndicator
import com.paymentoptions.pos.ui.composables._components.NoteChip
import com.paymentoptions.pos.ui.composables._components.buttons.Email
import com.paymentoptions.pos.ui.composables._components.buttons.EmailButton
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.buttons.ScanButton
import com.paymentoptions.pos.ui.composables._components.buttons.ShareButton
import com.paymentoptions.pos.ui.composables._components.images.PayByLinkImage
import com.paymentoptions.pos.ui.composables._components.images.PaymentQrCodeImage
import com.paymentoptions.pos.ui.composables._components.images.PaymentTapToPayImage
import com.paymentoptions.pos.ui.composables._components.paymentimagerow.PaymentApmsRow
import com.paymentoptions.pos.ui.composables._components.paymentimagerow.PaymentSchemesRow
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.layout.sectioned.LOGO_HEIGHT_IN_DP
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout
import com.paymentoptions.pos.ui.screens._flow.receiveMoneyFlow.chargemoney.ChargeMoneyBottomSectionContent
import com.paymentoptions.pos.ui.screens._flow.receiveMoneyFlow.inputnoney.InputMoneyBottomSectionContent
import com.paymentoptions.pos.ui.screens._flow.receiveMoneyFlow.receipt.ReceiptBottomSectionContent
import com.paymentoptions.pos.ui.screens._flow.receiveMoneyFlow.transactionfailed.TransactionFailedBottomSectionContent
import com.paymentoptions.pos.ui.screens._flow.receiveMoneyFlow.transactionsuccessful.TransactionSuccessfulBottomSectionContent
import com.paymentoptions.pos.ui.screens.status.MessageForStatusScreen
import com.paymentoptions.pos.ui.screens.status.StatusScreen
import com.paymentoptions.pos.ui.screens.status.StatusScreenType
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.ui.theme.red300
import com.paymentoptions.pos.utils.PaymentMethod
import com.paymentoptions.pos.utils.cashPaymentMethod
import com.paymentoptions.pos.utils.generateQrCode
import com.paymentoptions.pos.utils.inProduction
import com.paymentoptions.pos.utils.isUnauthorizedError
import com.paymentoptions.pos.utils.paymentMethods
import com.paymentoptions.pos.utils.showSessionExpiredAndNavigateToFingerprint
import com.paymentoptions.pos.utils.qrCodePaymentMethod
import com.paymentoptions.pos.utils.tapPaymentMethod
import com.paymentoptions.pos.utils.viaLinkPaymentMethod
import kotlinx.coroutines.IO
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.seconds

fun formatAmount(input: String): String {
    if (input.isEmpty()) return "0.00"
    val cents = input.toLong()
    val dollars = cents / 100
    val centPortion = (cents % 100).toString().padStart(2, '0')
    return "$dollars.$centPortion"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveMoneyFlow(
    navController: NavController,
    initialReceiveMoneyFlowStage: ReceiveMoneyFlowStage = ReceiveMoneyFlowStage.INPUT_MONEY,
) {
    val currency = getTransactionCurrency()
    var failureProceedFlag by remember { mutableStateOf(false) }
    var successProceedFlag by remember { mutableStateOf(false) }
    val enableScrollingInsideBottomSectionContent = false
    val scrollState = rememberScrollState()
    var latestTransactionId by remember { mutableStateOf<String?>(null) }

    var receiveMoneyFlowStage by remember {
        mutableStateOf(initialReceiveMoneyFlowStage)
    }
    var amountToChargeState by remember { mutableStateOf("") }
    var noteState by remember { mutableStateOf("") }

    var nfcStatusPair by remember { mutableStateOf(getNfcStatus()) }

    var showDeveloperOptionsEnabled by remember { mutableStateOf(false) }
    var showNFCNotEnabled by remember { mutableStateOf(false) }

    var signatureBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var signatureDate by remember { mutableStateOf(Clock.System.now()) }
    var signaturePath by remember { mutableStateOf(Path()) }
    var apms by remember { mutableStateOf(getApms()) }
    var startTapAndPay by remember { mutableStateOf(false) }
    var paymentUrl by remember { mutableStateOf("") }

    var paymentDetailsResponse by remember { mutableStateOf<PaymentDetailsResponse?>(null) }

    OnResume {
        val currentNfcStatus = getNfcStatus()
        nfcStatusPair = currentNfcStatus
        if (currentNfcStatus.second) {
            showNFCNotEnabled = false
        }
        if (!isDeveloperOptionsEnabled()) {
            showDeveloperOptionsEnabled = false
        }
    }

    val availablePaymentMethods = remember(nfcStatusPair) {
        val (isNfcSupported, _) = nfcStatusPair
        if (isNfcSupported) {
            // If NFC is supported (even if disabled), show all payment methods
            paymentMethods()
        } else {
            // If NFC is not supported, filter out the 'Tap' payment method
            paymentMethods().filter { it != tapPaymentMethod }
        }
    }
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod>(availablePaymentMethods.first()) }

    LaunchedEffect(availablePaymentMethods) {
        // If the currently selected method is no longer available, default to the first available one.
        if (selectedPaymentMethod !in availablePaymentMethods) {
            selectedPaymentMethod = availablePaymentMethods.first()
        }
    }

    latestTransactionId?.let {
        LaunchedEffect(latestTransactionId) {
            try {
                paymentDetailsResponse = paymentDetails(
                    paymentId = latestTransactionId.toString()
                )
            } catch (_: Exception) {
                paymentDetailsResponse = null
            }
        }
    }

    OnResume {
        val currentNfcStatus = getNfcStatus()
        nfcStatusPair = currentNfcStatus
        if (currentNfcStatus.second) {
            showNFCNotEnabled = false
        }
        if (!isDeveloperOptionsEnabled()) {
            showDeveloperOptionsEnabled = false
        }
    }

    /*
    if (!nfcStatusPair.first) {
        tapPaymentMethod.setIsEnabled(false)
        showToast("Your device does not support NFC")
    }
    */
    if (availablePaymentMethods.contains(qrCodePaymentMethod) && !apms.hasPayEasy && !apms.hasGooglePay && !apms.hasPayPay && !apms.hasWechatpay && !apms.hasKonbini && !apms.hasAlipay && !apms.hasGCash && !apms.hasDinersClub) {
        qrCodePaymentMethod.setIsEnabled(false)
        showToast("Payment via QR code not supported")
    }

    // Geo-restriction state
    var showGeoRestrictionDialog by remember { mutableStateOf(false) }
    var geoRestrictionMessage by remember { mutableStateOf("") }

    var ignoreGeoDialog by remember { mutableStateOf(false) }

    if (showGeoRestrictionDialog && !ignoreGeoDialog) {
        AlertDialog(
             onDismissRequest = { showGeoRestrictionDialog = false },
             title = { Text("Device Restricted") },
             text = { Text(geoRestrictionMessage) },
             confirmButton = {
                 TextButton(onClick = {
                     showGeoRestrictionDialog = false
                     ignoreGeoDialog = true
                     receiveMoneyFlowStage = ReceiveMoneyFlowStage.CHARGE_MONEY
                 }) {
                     Text("OK")
                 }
             }
         )
    }

    fun updateFlowStage(newFoodOrderFlowStage: ReceiveMoneyFlowStage) {
        // Check geo-restriction before allowing transaction stages
        if (!ignoreGeoDialog && newFoodOrderFlowStage == ReceiveMoneyFlowStage.CHARGE_MONEY) {
            val geoResult = GeoRestrictionManager.checkRestriction()
            if (geoResult.isRestricted) {
                geoRestrictionMessage = geoResult.message
                showGeoRestrictionDialog = true
                return
            }
        }
        receiveMoneyFlowStage = newFoodOrderFlowStage
    }

    when (receiveMoneyFlowStage) {
        ReceiveMoneyFlowStage.INPUT_MONEY -> SectionedLayout(
            navController = navController,
            bottomSectionMaxHeightRatio = 0.95f,
            bottomBarContent = BottomBarContent.NAVIGATION_BAR,
            bottomSectionPaddingInDp = 0.dp,
            enableScrollingOfBottomSectionContent = false
        ) {
            InputMoneyBottomSectionContent(
                navController,
                enableScrolling = true,
                updateAmountToCharge = { amountToChargeState = it },
                amountToCharge = amountToChargeState,
                updateNoteState = { noteState = it },
                updateFlowStage = { updateFlowStage(it) })
        }

        ReceiveMoneyFlowStage.CHARGE_MONEY -> {
            SectionedLayout(
                navController = navController,
                bottomSectionMinHeightRatio = 0.25f,
                bottomBarContent = BottomBarContent.NAVIGATION_BAR,
                bottomSectionPaddingInDp = 0.dp,
                enableScrollingOfBottomSectionContent = false,
                imageBelowLogo = {
//                    val paymentMethodIndices = paymentMethods.map { it.text }
                    val paymentMethodIndices = availablePaymentMethods.map { it.text }
                    AnimatedContent(
                        targetState = selectedPaymentMethod,
                        label = "payment_method_animation",
                        transitionSpec = {
                            // Compare the indexes of the old and new states
                            val initialIndex = paymentMethodIndices.indexOf(initialState.text)
                            val targetIndex = paymentMethodIndices.indexOf(targetState.text)
                            val animationDuration = 750

                            // If the new item is to the right of the old one slide left.
                            if (targetIndex > initialIndex) {
                                (slideInHorizontally(
                                    animationSpec = tween(animationDuration),
                                    initialOffsetX = { fullWidth -> fullWidth }) + fadeIn(
                                    animationSpec = tween(animationDuration)
                                )).togetherWith(
                                    slideOutHorizontally(
                                        animationSpec = tween(animationDuration),
                                        targetOffsetX = { fullWidth -> -fullWidth }) + fadeOut(
                                        animationSpec = tween(animationDuration)
                                    )
                                )
                            } else {
                                (slideInHorizontally(
                                    animationSpec = tween(animationDuration),
                                    initialOffsetX = { fullWidth -> -fullWidth }) + fadeIn(
                                    animationSpec = tween(animationDuration)
                                )).togetherWith(
                                    slideOutHorizontally(
                                        animationSpec = tween(animationDuration),
                                        targetOffsetX = { fullWidth -> fullWidth }) + fadeOut(
                                        animationSpec = tween(animationDuration)
                                    )
                                )
                            }
                        }) { paymentMethod ->
                        Column(
                            modifier = Modifier
                                .height(ScreenRatioToDp(0.5f))
                                .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            //when (selectedPaymentMethod) {
                            when (paymentMethod) {
                                tapPaymentMethod -> {
                                    getNfcStatus()

//                                    if (isDeveloperOptionsEnabled()) showDeveloperOptionsEnabled =
//                                        true
//                                    else if (!nfcStatusPair.second) showNFCNotEnabled = true
//                                    else if (!currentNfcStatusPair.second) showNFCNotEnabled = true

                                    MyDialog(
                                        showDialog = if (inProduction) showDeveloperOptionsEnabled else false,
                                        title = "Caution",
                                        text = "You need to disable developer options to proceed further.",
                                        acceptButtonText = "Developer Options",
                                        cancelButtonText = "Cancel",
                                        onAcceptFn = {
                                            openDeveloperSettings()
                                        },
                                        onDismissFn = {
                                            showDeveloperOptionsEnabled = false
                                            selectedPaymentMethod = qrCodePaymentMethod
                                        },
                                    )

                                    MyDialog(
                                        showDialog = showNFCNotEnabled,
                                        title = "NFC Required",
                                        text = "This feature needs NFC. Please enable it in your device settings.",
                                        acceptButtonText = "Go to Settings",
                                        cancelButtonText = "Cancel",
                                        onAcceptFn = {
                                            openNfcSettings()
                                        },
                                        onDismissFn = {
                                            showNFCNotEnabled = false
                                            selectedPaymentMethod = qrCodePaymentMethod
                                        },
                                    )

                                    PaymentTapToPayImage(
                                        modifier = Modifier
                                            .padding(horizontal = 20.dp)
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .clickable {
                                                if (inProduction)
                                                    if (isDeveloperOptionsEnabled()) {
                                                        showDeveloperOptionsEnabled = true
                                                    } else if (!getNfcStatus().second) {
                                                        showNFCNotEnabled = true
                                                    } else {
                                                        AnalyticsHelper.trackTapToPayInitiated(
                                                            amount = amountToChargeState,
                                                            currency = currency,
                                                        )
                                                        startTapAndPay = true
                                                    }
                                                else {
                                                    AnalyticsHelper.trackTapToPayInitiated(
                                                        amount = amountToChargeState,
                                                        currency = currency,
                                                    )
                                                    startTapAndPay = true
                                                }
                                            })

                                    FilledButton(
                                        text = "Tap here to start Tap To Pay",
                                        onClick = {
                                            if (inProduction)
                                                if (isDeveloperOptionsEnabled()) {
                                                    showDeveloperOptionsEnabled = true
                                                } else if (!getNfcStatus().second) {
                                                    showNFCNotEnabled = true
                                                } else {
                                                    AnalyticsHelper.trackTapToPayInitiated(
                                                        amount = amountToChargeState,
                                                        currency = currency,
                                                    )
                                                    startTapAndPay = true
                                                }
                                            else {
                                                AnalyticsHelper.trackTapToPayInitiated(
                                                    amount = amountToChargeState,
                                                    currency = currency,
                                                )
                                                startTapAndPay = true
                                            }
                                        },
                                        modifier = Modifier
                                            .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                                            .height(59.dp)
                                            .scale(0.8f)
                                    )

                                    PaymentSchemesRow(modifier = Modifier.height(50.dp))
                                }

                                qrCodePaymentMethod -> {

                                    startTapAndPay = false

                                    var qrCodeBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
                                    var qrCodeLoading by remember { mutableStateOf(false) }
                                    var qrCodeError by remember { mutableStateOf<String?>(null) }

                                    // This effect runs the API call once when the screen appears
                                    LaunchedEffect(Unit) {
                                        qrCodeLoading = true
                                        qrCodeError = null
                                        AnalyticsHelper.trackQrPaymentInitiated(
                                            amount = amountToChargeState,
                                            currency = currency,
                                        )
                                        try {
                                            val amountValue =
                                                amountToChargeState.toLongOrNull()?.div(100f) ?: 0f
                                            val request = PayByLinkRequest(
                                                PBLLinkName = "QR Payment",
                                                ExpiryDate = formatDate(Clock.System.now(),"dd MMMM, YYYY HH:mm:ss"),
                                                Product = listOf(
                                                    PayByLinkRequestProduct(
                                                        Currency = currency,
                                                        Name = "POS Sale",
                                                        Quantity = 1,
                                                        Price = amountValue,
                                                        TotalPrice = amountValue.toString()
                                                    )
                                                )
                                            )

                                            val response = payByLink(dasmid = DPStorageManager.getQRDasmid(), request)
                                            if (response != null && response.success) {
                                                val paymentUrl =
                                                    "https://api-dev.paymentoptions.com/paybylink/" + response.data.ProductID
                                                qrCodeBitmap = generateQrCode(paymentUrl)
                                            } else {
                                                qrCodeError = "Failed to generate QR code."
                                            }
                                        } catch (e: Exception) {
                                            AnalyticsHelper.trackApiError(
                                                endpoint = "payByLink(qr)",
                                                statusCode = (e as? ApiHttpException)?.statusCode,
                                                message = e.message ?: "QR payment initiation failed",
                                                throwable = e,
                                            )
                                            qrCodeError =
                                                "Your session has expired. Please log in again to continue."
                                            e.printStackTrace()

                                            if (e.isUnauthorizedError()) {
                                                showSessionExpiredAndNavigateToFingerprint(navController)
                                            }
                                        } finally {
                                            qrCodeLoading = false
                                        }
                                    }

                                    Text(
                                        text = "Scan QR Code",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 18.sp,
                                        textAlign = TextAlign.Center,
                                    )

                                    // This block handles showing the Loader, Error, or QR Code
                                    if (qrCodeLoading) {
                                        MyCircularProgressIndicator(useWhiteLoader = true)
                                    } else if (qrCodeError != null) {
                                        Text(
                                            text = qrCodeError!!,
                                            color = red300,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    } else {
                                        PaymentQrCodeImage(
                                            qrBitmap = qrCodeBitmap, // generated bitmap here
                                            modifier = Modifier
                                                .padding(horizontal = 20.dp)
                                                .fillMaxWidth()
                                                .height(220.dp)
                                                .clip(shape = RoundedCornerShape(16.dp))
                                        )
                                    }

                                    PaymentApmsRow(modifier = Modifier.height(50.dp))

                                    NoteChip(
                                        text = "Ask customer to scan with their payment app",
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                                    )
                                }

                                cashPaymentMethod -> {
                                    startTapAndPay = false

                                    Text(
                                        text = "Please pay cash",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 18.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                viaLinkPaymentMethod -> {
                                    startTapAndPay = false

                                    val amountValue =
                                        amountToChargeState.toLongOrNull()?.div(100f) ?: 0f

                                    val payByLinkRequest = PayByLinkRequest(
                                        PBLLinkName = "PayByLink Test",
                                        ExpiryDate = formatDate(Clock.System.now(),"dd MMMM, YYYY HH:mm:ss"),
                                        Product = listOf(
                                            PayByLinkRequestProduct(
                                                Currency = currency,
                                                Name = "Charge Money Test",
                                                Quantity = 1,
                                                Price = amountValue,
                                                TotalPrice = amountValue.toString()
                                            )
                                        )
                                    )
                                    var payByLinkResponse by remember {
                                        mutableStateOf<PayByLinkResponse?>(null)
                                    }
                                    var payByLinkApiResponseLoading by remember {
                                        mutableStateOf(false)
                                    }
                                    var payByLinkScanCodeBottomSheetExpanded by remember {
                                        mutableStateOf(false)
                                    }
                                    val sheetState = rememberModalBottomSheetState()
                                    //added a state variable to hold the generated QR bitmap
                                    var viaLinkQrBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

                                    LaunchedEffect(Unit) {
                                        try {
                                            payByLinkApiResponseLoading = true
                                            val dasmid =
                                                DPStorageManager.getPayByLinkDasmid()
                                            payByLinkResponse =
                                                payByLink(request = payByLinkRequest, dasmid = dasmid)
                                            if (payByLinkResponse != null && payByLinkResponse!!.success) {
                                                AnalyticsHelper.trackPayByLinkCreated(
                                                    linkId = payByLinkResponse!!.data.ProductID,
                                                    amount = amountToChargeState,
                                                )
//                                              val paymentUrl = "https://daspay/" + payByLinkResponse!!.data.ID
                                                paymentUrl =
                                                    "https://api-dev.paymentoptions.com/paybylink/" + payByLinkResponse!!.data.ProductID
                                                viaLinkQrBitmap = generateQrCode(paymentUrl)
                                            }
                                        } catch (e: Exception) {
                                            AnalyticsHelper.trackApiError(
                                                endpoint = "payByLink(create)",
                                                statusCode = (e as? ApiHttpException)?.statusCode,
                                                message = e.message ?: "Pay by link creation failed",
                                                throwable = e,
                                            )
                                            showToast("Error generating payment link...")
                                        } finally {
                                            payByLinkApiResponseLoading = false
                                        }
                                    }

                                    if (payByLinkApiResponseLoading) MyCircularProgressIndicator(
                                        useWhiteLoader = true
                                    )
                                    else if (payByLinkResponse != null) {

                                        if (payByLinkScanCodeBottomSheetExpanded) ModalBottomSheet(
                                            modifier = Modifier.fillMaxWidth(),
                                            onDismissRequest = {
                                                payByLinkScanCodeBottomSheetExpanded = false
                                            },
                                            sheetState = sheetState,
                                            containerColor = Color.White,
                                            contentColor = primary500,
                                            dragHandle = {}) {

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 20.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(Res.drawable.logo),
                                                    contentDescription = "DASPay Logo",
                                                    tint = primary500,
                                                    modifier = Modifier
                                                        .height(LOGO_HEIGHT_IN_DP.div(1.5f))
                                                        .align(Alignment.Center)
                                                )

                                                IconButton(
                                                    modifier = Modifier.align(alignment = Alignment.CenterEnd),
                                                    onClick = {
                                                        payByLinkScanCodeBottomSheetExpanded = false
                                                    }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Close",
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }
                                            }

                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {

                                                PaymentQrCodeImage(
                                                    qrBitmap = viaLinkQrBitmap,
                                                    modifier = Modifier
                                                        .padding(horizontal = 20.dp)
                                                        .fillMaxWidth()
                                                        .height(220.dp)
                                                        .clip(shape = RoundedCornerShape(16.dp))
                                                )

                                                Spacer(modifier = Modifier.height(10.dp))

                                                NoteChip(
                                                    text = "Scan with your device",
                                                    modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                                                )
                                            }
                                        }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Pay Via Link",
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 18.sp,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            PayByLinkImage(
                                                modifier = Modifier
                                                    .padding(horizontal = 20.dp)
                                                    .fillMaxWidth()
                                                    .height(70.dp)
                                                    .clip(shape = RoundedCornerShape(16.dp))
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            SelectionContainer(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        color = Color(0xFFDCEAFE),
                                                        shape = RoundedCornerShape(11.dp)
                                                    )
                                                    .padding(vertical = 16.dp, horizontal = 12.dp),
                                            ) {
                                                Text(
                                                    text = "https://api-dev.paymentoptions.com/paybylink/" + payByLinkResponse!!.data.ProductID,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 16.sp,
                                                    color = primary900,
                                                    maxLines = 1,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.horizontalScroll(state = rememberScrollState())
                                                )
                                            }


                                            NoteChip(
                                                text = "Share this link with the customer",
                                                color = Color.White
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                EmailButton(
                                                    text = "Email", email = Email(
                                                        subject = "DASPay payment link",
                                                        text = paymentUrl
                                                    ), modifier = Modifier
                                                        .weight(1f)
                                                        .border(
                                                            2.dp,
                                                            color = primary100.copy(alpha = 0.2f),
                                                            shape = RoundedCornerShape(10.dp)
                                                        )
                                                        .background(
                                                            Color.White,
                                                            shape = RoundedCornerShape(10.dp)
                                                        )
                                                        .padding(
                                                            horizontal = 10.dp, vertical = 16.dp
                                                        )
                                                )

                                                ShareButton(
                                                    text = "Share",
                                                    shareContent = paymentUrl,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .border(
                                                            2.dp,
                                                            color = primary100.copy(alpha = 0.2f),
                                                            shape = RoundedCornerShape(10.dp)
                                                        )
                                                        .background(
                                                            Color.White,
                                                            shape = RoundedCornerShape(10.dp)
                                                        )
                                                        .padding(
                                                            horizontal = 10.dp, vertical = 16.dp
                                                        )
                                                )

                                                ScanButton(
                                                    text = "Scan",
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .border(
                                                            2.dp,
                                                            color = primary100.copy(alpha = 0.2f),
                                                            shape = RoundedCornerShape(10.dp)
                                                        )
                                                        .background(
                                                            Color.White,
                                                            shape = RoundedCornerShape(10.dp)
                                                        )
                                                        .padding(
                                                            horizontal = 10.dp, vertical = 16.dp
                                                        )
                                                        .clickable {
                                                            payByLinkScanCodeBottomSheetExpanded =
                                                                true
                                                        })
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "Error generating payment link. Try again after some time...",
                                            color = red300,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 18.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                    }
                                }
                            }
                        }
                    }
                }) {
                ChargeMoneyBottomSectionContent(
                    navController,
                    enableScrolling = false,
                    amountToCharge = formatAmount(amountToChargeState),
                    availablePaymentMethods = availablePaymentMethods,
                    selectedPaymentMethod = selectedPaymentMethod,
                    updateSelectedPaymentMethod = { selectedPaymentMethod = it },
                    onLoader = {
                        updateFlowStage(ReceiveMoneyFlowStage.TRANSACTION_PROCESSING)
                        CoroutineScope(Dispatchers.IO).launch {
                            // Simulate transaction processing delay
                            delay(3.seconds)
                            it()
                        }
                    },
                    onSuccessUpdateFlowStage = { updateFlowStage(ReceiveMoneyFlowStage.TRANSACTION_SUCCESSFUL) },
                    onFailureUpdateFlowStage = { updateFlowStage(ReceiveMoneyFlowStage.TRANSACTION_FAILED) },
                    onChangeAmount = { updateFlowStage(ReceiveMoneyFlowStage.INPUT_MONEY) },
                    startTapAndPay = startTapAndPay,
                    updateLatestTransaction = { latestTransactionId = it })
            }
        }

        ReceiveMoneyFlowStage.TRANSACTION_PROCESSING -> {

            val dataMessage = MessageForStatusScreen(
                text = "Processing...", statusScreenType = StatusScreenType.PROCESSING
            )
            StatusScreen(navController, dataMessage, strategyFn = { })
        }

        ReceiveMoneyFlowStage.TRANSACTION_FAILED -> {
            val dataMessage = MessageForStatusScreen(
                text = "Payment Failed", statusScreenType = StatusScreenType.ERROR
            )
            StatusScreen(navController, dataMessage, strategyFn = {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000)
                    failureProceedFlag = true
                }
            })

            if (failureProceedFlag) SectionedLayout(
                navController = navController,
                bottomBarContent = BottomBarContent.NAVIGATION_BAR,
                bottomSectionPaddingInDp = 0.dp,
                bottomSectionMaxHeightRatio = 0.95f,
                enableScrollingOfBottomSectionContent = false
            ) {
                TransactionFailedBottomSectionContent(
                    navController,
                    enableScrolling = true,
                    transactionId = latestTransactionId.toString(),
                    updateFlowStage = { })
            }
        }

        ReceiveMoneyFlowStage.TRANSACTION_SUCCESSFUL -> {
            val dataMessage = MessageForStatusScreen(
                text = "Payment Successful", statusScreenType = StatusScreenType.SUCCESS
            )
            StatusScreen(
                navController, dataMessage, strategyFn = {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(2.seconds)
                        successProceedFlag = true
                    }
                })

            if (successProceedFlag) SectionedLayout(
                navController = navController,
                bottomBarContent = BottomBarContent.NAVIGATION_BAR,
                bottomSectionPaddingInDp = 0.dp,
                bottomSectionMinHeightRatio = 0.6f,
                enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent
            ) {
                TransactionSuccessfulBottomSectionContent(
                    navController,
                    enableScrolling = enableScrollingInsideBottomSectionContent,
                    transactionId = latestTransactionId.toString(),
                    signatureBitmap = signatureBitmap,
                    signatureDate = signatureDate,
                    updateFlowToDigitalSignature = { updateFlowStage(ReceiveMoneyFlowStage.DIGITAL_SIGNATURE) },
                    updateFlowToReceipt = { updateFlowStage(ReceiveMoneyFlowStage.RECEIPT) })
            }
        }

        ReceiveMoneyFlowStage.DIGITAL_SIGNATURE -> {
            SectionedLayout(
                navController = navController,
                bottomBarContent = BottomBarContent.NAVIGATION_BAR,//NOTHING
                bottomSectionPaddingInDp = 0.dp,
                bottomSectionMinHeightRatio = 0.95f,
                bottomSectionMaxHeightRatio = 0.95f,
                enableScrollingOfBottomSectionContent = false,
            ) {
                TakeDigitalSignatureBottomSectionContent(
                    navController,
                    enableScrolling = false,
                    signaturePath = signaturePath,
                    signatureDate = signatureDate,
                    paymentDetailsResponse = paymentDetailsResponse,
                    updateSignature = { path, bitmap, signDate ->
                        signaturePath = path
                        signatureBitmap = bitmap
                        signatureDate = signDate
                    },
                    updateFlowStageToSuccess = { updateFlowStage(ReceiveMoneyFlowStage.TRANSACTION_SUCCESSFUL) })
            }
        }

        ReceiveMoneyFlowStage.RECEIPT -> {
            SectionedLayout(
                navController = navController,
                bottomBarContent = BottomBarContent.NAVIGATION_BAR,
                bottomSectionPaddingInDp = 0.dp,
                bottomSectionMinHeightRatio = 0.75f,
                bottomSectionMaxHeightRatio = 0.75f,
                enableScrollingOfBottomSectionContent = false,
                enableZigZagContainerForBottomSection = true,
                imageBelowLogo = {
                    Text(
                        text = "Receipt",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }) {
                ReceiptBottomSectionContent(
                    navController,
                    enableScrolling = true,
                    transactionId = latestTransactionId.toString(),
                    signatureBitmap = signatureBitmap,
                    signatureDate = signatureDate,
                )
            }
        }
    }
}



