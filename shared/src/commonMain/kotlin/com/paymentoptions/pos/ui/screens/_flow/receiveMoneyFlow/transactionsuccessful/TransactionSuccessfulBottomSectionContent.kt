package com.paymentoptions.pos.ui.screens._flow.receiveMoneyFlow.transactionsuccessful
import paymentoptionspos.shared.generated.resources.logo
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res

import com.paymentoptions.pos.formatDate
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.device.DPStorageManager.getTransactionCurrency
import coil3.compose.AsyncImage
import kotlinx.serialization.json.Json
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.endpoints.paymentDetails
import com.paymentoptions.pos.network.endpoints.getSignature
import com.paymentoptions.pos.network.AquirerResponse
import com.paymentoptions.pos.network.PaymentDetailsResponse
import com.paymentoptions.pos.network.SignatureData
import com.paymentoptions.pos.utils.modifiers.shimmerEffect
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.NoteChip
import com.paymentoptions.pos.ui.composables._components.ScreenTitleWithCloseButton
import com.paymentoptions.pos.ui.composables._components.buttons.Email
import com.paymentoptions.pos.ui.composables._components.buttons.EmailButton
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.buttons.OutlinedButton
import com.paymentoptions.pos.ui.composables._components.buttons.ScanButton
import com.paymentoptions.pos.ui.composables._components.buttons.ShareButton
import com.paymentoptions.pos.ui.composables._components.images.PaymentQrCodeImage
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.navigation.Screens
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.containerBackgroundGradientBrush
import com.paymentoptions.pos.ui.theme.green500
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.utils.generateQrCode
import com.paymentoptions.pos.utils.formatToPrecisionString
import com.paymentoptions.pos.utils.modifiers.conditional
import com.paymentoptions.pos.utils.modifiers.dashedBorder
import com.paymentoptions.pos.utils.safeParseDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSuccessfulBottomSectionContent(
    navController: NavController,
    transactionId: String,
    enableScrolling: Boolean = false,
    signatureBitmap: ImageBitmap?,
    signatureDate: Instant,
    updateFlowToDigitalSignature: () -> Unit = {},
    updateFlowToReceipt: () -> Unit = {},
) {
    val currency = getTransactionCurrency()
    val scrollState = rememberScrollState()
    var paymentDetailsLatestResponse by remember { mutableStateOf<PaymentDetailsResponse?>(null) }
    var transactionAquirerResponse by remember { mutableStateOf<AquirerResponse?>(AquirerResponse()) }
    var signatureData by remember { mutableStateOf<SignatureData?>(null) }
    var isSignatureLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        paymentDetailsLatestResponse = try {
            paymentDetails(paymentId = transactionId)
        } catch (e: Exception) {
            null
        }
    }

    LaunchedEffect(transactionId) {
        if (transactionId.isNotBlank()) {
            isSignatureLoading = true
            signatureData = try {
                getSignature(uuid = transactionId)?.data
            } catch (e: Exception) {
                AppLogger.error("GetSignature error: ${e.message}")
                null
            }
            isSignatureLoading = false
        }
    }

    if (paymentDetailsLatestResponse != null) {
        val rawAcquirerResponse = paymentDetailsLatestResponse?.data?.AcquirerResponse?.firstOrNull()
        transactionAquirerResponse = if (!rawAcquirerResponse.isNullOrBlank()) {
            runCatching<AquirerResponse> {
                Json { ignoreUnknownKeys = true }.decodeFromString(rawAcquirerResponse)
            }.getOrNull() ?: AquirerResponse()
        } else {
            AquirerResponse()
        }
    }

    val transactionUuid = paymentDetailsLatestResponse?.data?.TransactionRefID
    val transactionDetailUrl = if (!transactionUuid.isNullOrEmpty()) {
        "${DPStorageManager.getTransactionDetailsUrl()}/$transactionUuid"
    } else {
        null
    }

    val dateString =
        paymentDetailsLatestResponse?.data?.Date ?: Clock.System.now()
            .toString()
    val dateTime = safeParseDateTime(dateString)
    val dateStringFormatted: String = formatDate(dateTime, "dd MMMM YYYY")

    var showQrCodeBottomSheetExpanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showQrCodeBottomSheetExpanded) ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = { showQrCodeBottomSheetExpanded = false },
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
                    .height(
                        com.paymentoptions.pos.ui.composables.layout.sectioned.LOGO_HEIGHT_IN_DP.div(
                            1.5f
                        )
                    )
                    .align(Alignment.Center)
            )

            IconButton(
                modifier = Modifier.align(alignment = Alignment.CenterEnd), onClick = {
                    showQrCodeBottomSheetExpanded = false
                }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val linkQrBitmap = generateQrCode(transactionDetailUrl ?: "")

            PaymentQrCodeImage(
                qrBitmap = linkQrBitmap,
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
            .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
            .padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        ScreenTitleWithCloseButton(
            navController = navController,
            fontSize = 8.sp,
            onClose = { navController.navigate(Screens.Dashboard.route) {
                popUpTo(Screens.AuthCheck.route) { inclusive = true }
            } })

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = 20.dp.times(-1))
        ) {
            Text(
                text = "Transaction Successful",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = green500,
            )

            AppLogger.debug("paymentDetailsLatestResponse amount: ${paymentDetailsLatestResponse?.data?.Amount}")
            CurrencyText(
                currency = currency,
                amount = paymentDetailsLatestResponse?.data?.Amount.formatToPrecisionString()
            )

            Row(
                modifier = Modifier.scale(0.7f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    text = "Refund",
                    onClick = { },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                )

                Spacer(modifier = Modifier.width(10.dp))

                FilledButton(
                    text = "View Full Receipt",
                    onClick = { updateFlowToReceipt() },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = containerBackgroundGradientBrush, shape = RoundedCornerShape(20.dp)
                )
                .conditional(enableScrolling) { verticalScroll(scrollState) },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Spacer(modifier = Modifier.height(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        "Reference No.", style = AppTheme.typography.footnote.copy(
                            fontWeight = FontWeight.Normal, fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Text(
                        paymentDetailsLatestResponse?.data?.TransactionRefID.toString(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Date", style = AppTheme.typography.footnote.copy(
                            fontWeight = FontWeight.Normal, fontSize = 14.sp
                        )
                    )

                    Text(
                        dateStringFormatted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Trace", style = AppTheme.typography.footnote.copy(
                            fontWeight = FontWeight.Normal, fontSize = 14.sp
                        )
                    )

                    Text(
                        text = transactionAquirerResponse?.trace.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Approval", style = AppTheme.typography.footnote.copy(
                            fontWeight = FontWeight.Normal, fontSize = 14.sp
                        )
                    )

                    Text(
                        text = transactionAquirerResponse?.approvalCode.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500
                    )
                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Aggregator", style = AppTheme.typography.footnote.copy(
                            fontWeight = FontWeight.Normal, fontSize = 14.sp
                        )
                    )

                    Text(
//                        paymentDetailsLatestResponse?.data?.Scheme.toString(),
                        transactionAquirerResponse?.paymentMethod.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                color = Color.LightGray.copy(alpha = 0.2f)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Share with Others",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primary900,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EmailButton(
                        text = "Email",
                        email = Email(
                            subject = "Your DASPay Transaction Receipt",
                            text = transactionDetailUrl ?: "Transaction details unavailable"
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                2.dp,
                                color = primary100.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(Color.White)
                            .padding(horizontal = 10.dp, vertical = 20.dp)
                    )

                    ShareButton(
                        text = "Share",
//                        shareContent = shareableSuccessText,
                        shareContent = transactionDetailUrl ?: "Transaction details unavailable",
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                2.dp,
                                color = primary100.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(Color.White)
                            .padding(horizontal = 10.dp, vertical = 20.dp)
                    )

                    ScanButton(
                        text = "Scan", modifier = Modifier
                            .weight(1f)
                            .border(
                                2.dp,
                                color = primary100.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(Color.White)
                            .padding(horizontal = 10.dp, vertical = 20.dp)
                            .clickable { showQrCodeBottomSheetExpanded = true }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                //Show Signature
                if (signatureBitmap != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
//                            .height(200.dp)
                            .dashedBorder(color = Color.LightGray, shape = RoundedCornerShape(8.dp))
                            .padding(4.dp)
//                            .clickable { updateFlowStage(ReceiveMoneyFlowStage.DIGITAL_SIGNATURE) }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Digital Signature",
                                fontSize = 12.sp,
                                color = primary500,
                                fontWeight = FontWeight.Normal
                            )

                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            purple50, fontWeight = FontWeight.Medium
                                        )
                                    ) { append("Signing at: ") }

                                    withStyle(SpanStyle(primary500)) {
                                        append(
                                            formatDate(signatureDate, "dd MMMM, YYYY")
                                        )
                                    }
                                }, style = AppTheme.typography.footnote
                            )

                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16 / 9f)
//                                .background(Color.Green)
                        ) {
                            Image(
                                modifier = Modifier.padding(8.dp),//added padding to center signature
//                                modifier = Modifier.scale(0.9f),
                                bitmap = signatureBitmap,
                                contentDescription = "Customer signature",
//                                contentScale = ContentScale.FillBounds,
                                contentScale = ContentScale.Fit,
                                alignment = Alignment.Center,
//                                modifier = Modifier.rotate(270f)
                            )
                        }
                    }

                }

                // ── Server Signature Section ─────────────────────────────
                if (isSignatureLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .shimmerEffect()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                } else {
                    val signatureUrl = signatureData?.signatureURL?.toString()
                    if (signatureData?.imageExists == true && !signatureUrl.isNullOrBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Customer Signature",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primary900
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .dashedBorder(
                                        color = Color.LightGray,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = signatureUrl,
                                    contentDescription = "Transaction Signature",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                FilledButton(
                    text = if (signatureBitmap != null) "View Transaction Details" else "Take Digital Signature",
                    onClick = {
                        if (signatureBitmap != null) {
                            updateFlowToReceipt()
                        } else updateFlowToDigitalSignature()
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .height(39.dp)
                        .scale(0.7f)
                )
            }
        }
    }
}