package com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow

import MyDialog
import android.content.Intent
import android.graphics.Bitmap
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import co.yml.charts.common.extensions.isNotNull
import com.paymentoptions.pos.device.DeveloperOptions
import com.paymentoptions.pos.device.Nfc
import com.paymentoptions.pos.device.getTransactionCurrency
import com.paymentoptions.pos.device.screenRatioToDp
import com.paymentoptions.pos.services.apiService.PayByLinkRequest
import com.paymentoptions.pos.services.apiService.PayByLinkRequestProduct
import com.paymentoptions.pos.services.apiService.PayByLinkResponse
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.services.apiService.endpoints.payByLink
import com.paymentoptions.pos.ui.composables._components.MyCircularProgressIndicator
import com.paymentoptions.pos.ui.composables._components.NoteChip
import com.paymentoptions.pos.ui.composables._components.buttons.Email
import com.paymentoptions.pos.ui.composables._components.buttons.EmailButton
import com.paymentoptions.pos.ui.composables._components.buttons.ScanButton
import com.paymentoptions.pos.ui.composables._components.buttons.ShareButton
import com.paymentoptions.pos.ui.composables._components.images.CreditCardImage
import com.paymentoptions.pos.ui.composables._components.images.PayByLinkImage
import com.paymentoptions.pos.ui.composables._components.images.PaymentQrCodeImage
import com.paymentoptions.pos.ui.composables._components.images.PaymentTapToPayImage
import com.paymentoptions.pos.ui.composables._components.images.cardpayment.AmexImage
import com.paymentoptions.pos.ui.composables._components.images.cardpayment.JcbImage
import com.paymentoptions.pos.ui.composables._components.images.cardpayment.MastercardImage
import com.paymentoptions.pos.ui.composables._components.images.cardpayment.VisaImage
import com.paymentoptions.pos.ui.composables._components.images.qrpayment.ApplePayImage
import com.paymentoptions.pos.ui.composables._components.images.qrpayment.GrabPayImage
import com.paymentoptions.pos.ui.composables._components.images.qrpayment.QrPayment2
import com.paymentoptions.pos.ui.composables._components.images.qrpayment.QrPayment3
import com.paymentoptions.pos.ui.composables._components.images.qrpayment.QrPayment4
import com.paymentoptions.pos.ui.composables._components.images.qrpayment.QrPayment6
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout
import com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow.chargemoney.ChargeMoneyBottomSectionContent
import com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow.inputnoney.InputMoneyBottomSectionContent
import com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow.receipt.ReceiptBottomSectionContent
import com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow.transactionfailed.TransactionFailedBottomSectionContent
import com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow.transactionsuccessful.TransactionSuccessfulBottomSectionContent
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.ui.theme.red300
import com.paymentoptions.pos.utils.PaymentMethod
import com.paymentoptions.pos.utils.cashPaymentMethod
import com.paymentoptions.pos.utils.paymentMethods
import com.paymentoptions.pos.utils.qrCodePaymentMethod
import com.paymentoptions.pos.utils.tapPaymentMethod
import com.paymentoptions.pos.utils.viaLinkPaymentMethod
import java.text.SimpleDateFormat
import java.util.Date

fun formatAmount(input: String): String {
    if (input.isEmpty()) return "0.00"
    val cents = input.toLong()
    val dollars = cents / 100
    val centPortion = (cents % 100).toString().padStart(2, '0')
    return "$dollars.$centPortion"
}

@Composable
fun ReceiveMoneyFlow(
    navController: NavController,
    initialReceiveMoneyFlowStage: ReceiveMoneyFlowStage = ReceiveMoneyFlowStage.INPUT_MONEY,
) {
    val context = LocalContext.current
    val currency = getTransactionCurrency(context)

    val enableScrollingInsideBottomSectionContent = false
    val scrollState = rememberScrollState()

    var receiveMoneyFlowStage by remember {
        mutableStateOf<ReceiveMoneyFlowStage>(
            initialReceiveMoneyFlowStage
        )
    }
    var amountToChargeState by remember { mutableStateOf("") }
    var noteState by remember { mutableStateOf("") }

    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod>(paymentMethods.first()) }
    var nfcStatusPair by remember { mutableStateOf(Nfc.getStatus(context)) }

    var showDeveloperOptionsEnabled by remember { mutableStateOf(false) }
    var showNFCNotPresent by remember { mutableStateOf(false) }
    var showNFCNotEnabled by remember { mutableStateOf(false) }

    var transaction: TransactionListDataRecord? = null
    var signatureBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var signatureDate by remember { mutableStateOf(Date()) }
    var signaturePath by remember { mutableStateOf(Path()) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            //block runs when lifecycle event happens
            if (event == Lifecycle.Event.ON_RESUME) {
                //when app resumes check NFC status again
                val currentNfcStatus = Nfc.getStatus(context)
                if (currentNfcStatus.second) {
                    showNFCNotEnabled = false //hide the dialog
                }
            }
        }
        //adding observer to the lifecycel
        lifecycleOwner.lifecycle.addObserver(observer)
        //removing the observer when the screen is closed
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    if (!nfcStatusPair.first) {
        showNFCNotPresent = true
        tapPaymentMethod.setIsEnabled(false)
    }

    fun updateFlowStage(newFoodOrderFlowStage: ReceiveMoneyFlowStage) {
        receiveMoneyFlowStage = newFoodOrderFlowStage
    }

    when (receiveMoneyFlowStage) {
        ReceiveMoneyFlowStage.INPUT_MONEY -> SectionedLayout(
            navController = navController,
            bottomSectionMaxHeightRatio = 0.95f,
            bottomBarContent = BottomBarContent.TOGGLE_BUTTON,
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
                bottomBarContent = BottomBarContent.TOGGLE_BUTTON,
                bottomSectionPaddingInDp = 0.dp,
                enableScrollingOfBottomSectionContent = false,
                imageBelowLogo = {
                    val paymentMethodIndices = paymentMethods.map { it.text }
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
                                    initialOffsetX = { fullWidth -> fullWidth }
                                ) + fadeIn(animationSpec = tween(animationDuration)))
                                    .togetherWith(
                                        slideOutHorizontally(
                                            animationSpec = tween(animationDuration),
                                            targetOffsetX = { fullWidth -> -fullWidth }
                                        ) + fadeOut(animationSpec = tween(animationDuration))
                                    )
                            } else {
                                (slideInHorizontally(
                                    animationSpec = tween(animationDuration),
                                    initialOffsetX = { fullWidth -> -fullWidth }
                                ) + fadeIn(animationSpec = tween(animationDuration)))
                                    .togetherWith(
                                        slideOutHorizontally(
                                            animationSpec = tween(animationDuration),
                                            targetOffsetX = { fullWidth -> fullWidth }
                                        ) + fadeOut(animationSpec = tween(animationDuration))
                                    )
                            }
                        }
                    ) { paymentMethod ->
                    Column(
                        modifier = Modifier
                            .height(screenRatioToDp(0.5f))
                            .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        //when (selectedPaymentMethod) {
                        when (paymentMethod) {
                            tapPaymentMethod -> {
                                val currentNfcStatus = Nfc.getStatus(context)

                                if (DeveloperOptions.isEnabled(context)) showDeveloperOptionsEnabled =
                                    true
                                //else if (!nfcStatusPair.second) showNFCNotEnabled = true
                                else if (!currentNfcStatus.second) {//check the fresh status
                                    showNFCNotEnabled = true
                                }

                                MyDialog(
                                    showDialog = false, //showDeveloperOptionsEnabled,
                                    title = "Caution",
                                    text = "You need to disable developer options to proceed further.",
                                    acceptButtonText = "Developer Options",
                                    cancelButtonText = "Cancel",
                                    onAcceptFn = {
                                        showDeveloperOptionsEnabled = false
                                        val intent =
                                            Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                                        context.startActivity(intent)
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
                                        showNFCNotEnabled = false
                                        val intent = Intent(Settings.ACTION_NFC_SETTINGS)
                                        context.startActivity(intent)
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
                                        .height(230.dp)
                                        .clip(
                                            shape = RoundedCornerShape(16.dp)
                                        )
//                                        .clickable{
//                                            Tap_ChargeMoney(navController=navController, amountToCharge = formatAmount(amountToChargeState))
//                                        }
                                )

                                Text(
                                    text = "Tap To Pay",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.height(60.dp)
                                ) {
                                    VisaImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(8.dp))
                                            .weight(1f)
                                    )

                                    MastercardImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(8.dp))
                                            .weight(1f)
                                    )

                                    AmexImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(8.dp))
                                            .weight(1f)
                                    )

                                    JcbImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(8.dp))
                                            .weight(1f)
                                    )
                                }
                            }

                            qrCodePaymentMethod -> {

                                Text(
                                    text = "Scan QR Code",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center,
                                )

                                PaymentQrCodeImage(
                                    modifier = Modifier
                                        .padding(horizontal = 20.dp)
                                        .fillMaxWidth()
                                        //.height(240.dp)
                                        .height(220.dp)
                                        .clip(
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.height(50.dp)
                                ) {
                                    GrabPayImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .weight(1f)
                                    )

                                    QrPayment2(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .weight(1f)
                                    )

                                    QrPayment3(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .weight(1f)
                                    )

                                    QrPayment4(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .weight(1f)
                                    )

                                    ApplePayImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .weight(1f)
                                    )

                                    QrPayment6(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .weight(1f)
                                    )
                                }

                                NoteChip(
                                    text = "Ask customer to scan with GrabPay",
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                                )
                            }

                            cashPaymentMethod -> {
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

                                var payByLinkRequest = PayByLinkRequest(
                                    PBLLinkName = "PayByLink Test",
                                    ExpiryDate = SimpleDateFormat("YYYY-dd MMMM, YYYY HH:mm:ss").format(
                                        Date()
                                    ), //Date().toString(),
                                    Product = listOf<PayByLinkRequestProduct>(
                                        PayByLinkRequestProduct(
                                            Currency = currency,
                                            Name = "No Name",
                                            Quantity = 1,
                                            Price = 100f,
                                            TotalPrice = "100"
                                        )
                                    )
                                )
                                var payByLinkResponse by remember {
                                    mutableStateOf<PayByLinkResponse?>(
                                        null
                                    )
                                }
                                var payByLinkApiResponseLoading by remember { mutableStateOf(false) }

                                LaunchedEffect(Unit) {
                                    try {
                                        payByLinkApiResponseLoading = true
                                        payByLinkResponse = payByLink(context, payByLinkRequest)

                                        println("payByLinkResponse: $payByLinkResponse")

                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Error generating payment link...",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } finally {
                                        payByLinkApiResponseLoading = false
                                    }
                                }

                                if (payByLinkApiResponseLoading) MyCircularProgressIndicator()
                                else if (payByLinkResponse.isNotNull()) {

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
                                                .height(100.dp)
                                                .clip(
                                                    shape = RoundedCornerShape(16.dp)
                                                )
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
                                                text = "https://daspay/" + payByLinkResponse!!.data.ID,
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
                                                text = "Email",
                                                email = Email(),
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
                                                    //.padding(horizontal = 10.dp, vertical = 20.dp)
                                                    .padding(horizontal = 10.dp, vertical = 16.dp)
                                            )

                                            ShareButton(
                                                text = "Share",
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
                                                    //.padding(horizontal = 10.dp, vertical = 20.dp)
                                                    .padding(horizontal = 10.dp, vertical = 16.dp)
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
                                                    //.padding(horizontal = 10.dp, vertical = 20.dp)
                                                    .padding(horizontal = 10.dp, vertical = 16.dp)
                                            )
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
                    selectedPaymentMethod = selectedPaymentMethod,
                    updateSelectedPaymentMethod = { selectedPaymentMethod = it },
                    updateFlowStage = { updateFlowStage(it as ReceiveMoneyFlowStage) },
                    onChangeAmount = { updateFlowStage(ReceiveMoneyFlowStage.INPUT_MONEY) })

            }
        }

        ReceiveMoneyFlowStage.TRANSACTION_FAILED -> {
            SectionedLayout(
                navController = navController,
                bottomBarContent = BottomBarContent.NAVIGATION_BAR,
                bottomSectionPaddingInDp = 0.dp,
                bottomSectionMaxHeightRatio = 0.95f,
                imageBelowLogo = {
                    CreditCardImage(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(
                                shape = RoundedCornerShape(16.dp)
                            )
                    )
                },
                enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent
            ) {
                TransactionFailedBottomSectionContent(
                    navController,
                    enableScrolling = enableScrollingInsideBottomSectionContent,
                    amountToCharge = formatAmount(amountToChargeState),
                    transaction = transaction,
                    updateFlowStage = { updateFlowStage(it) })
            }
        }

        ReceiveMoneyFlowStage.TRANSACTION_SUCCESSFUL -> {

            SectionedLayout(
                navController = navController,
                bottomBarContent = BottomBarContent.NAVIGATION_BAR,
                bottomSectionPaddingInDp = 0.dp,
                bottomSectionMaxHeightRatio = 0.95f,
                imageBelowLogo = {
                    CreditCardImage(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(
                                shape = RoundedCornerShape(16.dp)
                            )
                    )
                },
                enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent
            ) {
                TransactionSuccessfulBottomSectionContent(
                    navController,
                    enableScrolling = enableScrollingInsideBottomSectionContent,
                    transaction = transaction,
                    amountToCharge = formatAmount(amountToChargeState),
                    signatureBitmap = signatureBitmap,
                    signatureDate = signatureDate,
                    updateFlowStage = { updateFlowStage(it) })
            }
        }

        ReceiveMoneyFlowStage.DIGITAL_SIGNATURE -> {
            SectionedLayout(
                navController = navController,
                bottomBarContent = BottomBarContent.NOTHING,
                bottomSectionPaddingInDp = 0.dp,
                bottomSectionMinHeightRatio = 0.95f,
                bottomSectionMaxHeightRatio = 0.95f,
                enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent,
            ) {
                TakeDigitalSignatureBottomSectionContent(
                    navController,
                    enableScrolling = enableScrollingInsideBottomSectionContent,
                    signaturePath = signaturePath,
                    signatureDate = signatureDate,
                    updateSignature = { path, bitmap, signDate ->
                        signaturePath = path
                        signatureBitmap = bitmap
                        signatureDate = signDate
                    },
                    updateFlowStage = { updateFlowStage(it) })
            }
        }

        ReceiveMoneyFlowStage.RECEIPT -> {
            SectionedLayout(
                navController = navController,
                bottomBarContent = BottomBarContent.NOTHING,
                bottomSectionPaddingInDp = 0.dp,
                bottomSectionMinHeightRatio = 0.95f,
                bottomSectionMaxHeightRatio = 0.95f,
                enableScrollingOfBottomSectionContent = false,
            ) {
                ReceiptBottomSectionContent(
                    navController,
                    enableScrolling = true,
                    transaction = transaction,
                    signatureBitmap = signatureBitmap,
                    signatureDate = signatureDate,
                )
            }
        }
    }
}
