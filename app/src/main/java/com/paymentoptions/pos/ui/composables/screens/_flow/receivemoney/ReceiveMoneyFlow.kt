package com.paymentoptions.pos.ui.composables.screens._flow.receivemoney

import MyDialog
import android.content.Intent
import android.graphics.Bitmap
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.TapAndPlay
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.device.Nfc
import com.paymentoptions.pos.device.screenRatioToDp
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.ui.composables._components.images.CreditCardImage
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
import com.paymentoptions.pos.ui.composables.screens._flow.receivemoney.chargemoney.ChargeMoneyBottomSectionContent
import com.paymentoptions.pos.ui.composables.screens._flow.receivemoney.inputnoney.InputMoneyBottomSectionContent
import com.paymentoptions.pos.ui.composables.screens._flow.receivemoney.receipt.ReceiptBottomSectionContent
import com.paymentoptions.pos.ui.composables.screens._flow.receivemoney.transactionfailed.TransactionFailedBottomSectionContent
import com.paymentoptions.pos.ui.composables.screens._flow.receivemoney.transactionsuccessful.TransactionSuccessfulBottomSectionContent
import com.paymentoptions.pos.ui.theme.noBorder
import com.paymentoptions.pos.utils.modifiers.conditional
import java.util.Date

enum class ReceiveMoneyFlowStage {
    INPUT_MONEY, CHARGE_MONEY, TRANSACTION_FAILED, TRANSACTION_SUCCESSFUL, DIGITAL_SIGNATURE, RECEIPT,
}

fun formatAmount(input: String): String {
    if (input.isEmpty()) return "0.00"
    val cents = input.toLong()
    val dollars = cents / 100
    val centPortion = (cents % 100).toString().padStart(2, '0')
    return "$dollars.$centPortion"
}

class PaymentMethod(
    val text: String,
    val icon: ImageVector,
    var isEnabled: Boolean = true,
) {
    fun setIsEnabled(isEnabled: Boolean) {
        this.isEnabled = isEnabled
    }
}

val tapPaymentMethod = PaymentMethod(text = "Tap", icon = Icons.Default.TapAndPlay)
val qrCodePaymentMethod = PaymentMethod(text = "QR Code", icon = Icons.Default.QrCode)
val cashPaymentMethod = PaymentMethod(text = "Cash", icon = Icons.Default.Money)
val viaLinkPaymentMethod = PaymentMethod(text = "Via Link", icon = Icons.Default.Link)

val paymentMethods = listOf(
    tapPaymentMethod, qrCodePaymentMethod, cashPaymentMethod, viaLinkPaymentMethod
)

@Composable
fun ReceiveMoneyFlow(
    navController: NavController,
    initialReceiveMoneyFlowStage: ReceiveMoneyFlowStage = ReceiveMoneyFlowStage.INPUT_MONEY,
) {

    val context = LocalContext.current

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

    var nfcStatusPair = Nfc.getStatus(context)
    var showNFCNotPresent by remember { mutableStateOf(false) }
    var showNFCNotEnabled by remember { mutableStateOf(false) }

    var transaction: TransactionListDataRecord? = null
    var signatureBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var signatureDate by remember { mutableStateOf(Date()) }
    var signaturePath by remember { mutableStateOf(Path()) }


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
            enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent
        ) {
            InputMoneyBottomSectionContent(
                navController,
                enableScrolling = enableScrollingInsideBottomSectionContent,
                updateAmountToCharge = { amountToChargeState = it },
                amountToCharge = amountToChargeState,
                updateNoteState = { noteState = it },
                updateFlowStage = { updateFlowStage(it) })
        }

        ReceiveMoneyFlowStage.CHARGE_MONEY -> {
            SectionedLayout(
                navController = navController,
                bottomSectionMinHeightRatio = 0.35f,
                bottomSectionMaxHeightRatio = 0.35f,
                bottomBarContent = BottomBarContent.TOGGLE_BUTTON,
                bottomSectionPaddingInDp = 0.dp,
                enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent,
                imageBelowLogo = {
                    Column(
                        modifier = Modifier
                            .height(screenRatioToDp(0.5f))
                            .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                            .conditional(!enableScrollingInsideBottomSectionContent) {
                                verticalScroll(scrollState)
                            },
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (selectedPaymentMethod) {
                            tapPaymentMethod -> {
                                if (!nfcStatusPair.second) showNFCNotEnabled = true

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
                                        .height(270.dp)
                                        .clip(
                                            shape = RoundedCornerShape(16.dp)
                                        )
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
                                    modifier = Modifier.height(80.dp)
                                ) {
                                    VisaImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .weight(1f)
                                    )

                                    MastercardImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .weight(1f)
                                    )

                                    AmexImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .weight(1f)
                                    )

                                    JcbImage(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(shape = RoundedCornerShape(16.dp))
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
                                        .height(270.dp)
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

                                AssistChip(
                                    modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                                    onClick = { },
                                    label = {
                                        Text(
                                            "Ask customer to scan with GrabPay",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Light,
                                            color = Color.White
                                        )
                                    },
                                    border = noBorder,
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = Color.LightGray.copy(0.2f)
                                    ),
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.Lightbulb,
                                            contentDescription = "Hint",
                                            tint = Color.Yellow,
                                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                                        )
                                    })

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

                                Text(
                                    text = "Pay via link",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }) {
                ChargeMoneyBottomSectionContent(
                    navController,
                    enableScrolling = enableScrollingInsideBottomSectionContent,
                    amountToCharge = formatAmount(amountToChargeState),
                    selectedPaymentMethod = selectedPaymentMethod,
                    updateSelectedPaymentMethod = { selectedPaymentMethod = it },
                    updateFlowStage = { updateFlowStage(it) })
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
