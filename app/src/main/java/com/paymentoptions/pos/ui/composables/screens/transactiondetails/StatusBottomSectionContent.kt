package com.paymentoptions.pos.ui.composables.screens.transactiondetails

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.R
import com.paymentoptions.pos.device.DPSharedPreferences
import com.paymentoptions.pos.logger.AppLogger
import coil3.compose.AsyncImage
import com.paymentoptions.pos.services.apiService.AquirerResponse
import com.paymentoptions.pos.services.apiService.PaymentDetailsResponse
import com.paymentoptions.pos.services.apiService.SignatureData
import com.paymentoptions.pos.services.apiService.endpoints.getSignature
import com.paymentoptions.pos.services.apiService.endpoints.paymentDetails
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.NoteChip
import com.paymentoptions.pos.ui.composables._components.buttons.Email
import com.paymentoptions.pos.ui.composables._components.buttons.EmailButton
import com.paymentoptions.pos.ui.composables._components.buttons.ScanButton
import com.paymentoptions.pos.ui.composables._components.buttons.ShareButton
import com.paymentoptions.pos.ui.composables._components.images.PaymentQrCodeImage
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.layout.sectioned.LOGO_HEIGHT_IN_DP
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.green500
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.utils.TransactionColors
import com.paymentoptions.pos.utils.formatToPrecisionString
import com.paymentoptions.pos.utils.generateQrCode
import com.paymentoptions.pos.utils.modifiers.dashedBorder
import com.paymentoptions.pos.utils.modifiers.shimmerEffect
import com.paymentoptions.pos.utils.safeParseOffsetDateTime
import com.paymentoptions.pos.utils.AppJson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, ExperimentalComposeApi::class
)
@Composable
fun StatusBottomSectionContent(
    navController: NavController,
    transactionId: String,
    enableScrolling: Boolean = false,
    updateDetailsScreenType: (TransactionDetailsScreenType) -> Unit,
    title: String,
    amount: String,
    dateString: String,
    referenceId: String,
    aggregator : String
) {
    AppLogger.debug("StatusBottomSectionContent is called with amount = $amount, dateString = $dateString, referenceId = $referenceId, aggregator = $aggregator")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showQrCodeBottomSheetExpanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var paymentDetailsLatestResponse by remember { mutableStateOf<PaymentDetailsResponse?>(null) }
    var transactionAquirerResponse by remember { mutableStateOf<AquirerResponse?>(AquirerResponse()) }
    var isLoading by remember { mutableStateOf(true) }
    var signatureData by remember { mutableStateOf<SignatureData?>(null) }
    var isSignatureLoading by remember { mutableStateOf(true) }


    LaunchedEffect(Unit) {
        isLoading = true
        paymentDetailsLatestResponse = try {
            paymentDetails(
                context = context,
                paymentId = transactionId
            )
        } catch (e: Exception) {
            null
        }
        isLoading = false
    }

    LaunchedEffect(transactionId) {
        if (transactionId.isNotBlank()) {
            isSignatureLoading = true
            signatureData = try {
                getSignature(context = context, uuid = transactionId)?.data
            } catch (e: Exception) {
                AppLogger.error("GetSignature error: ${e.message}")
                null
            }
            isSignatureLoading = false
        }
    }


    if (paymentDetailsLatestResponse != null)
        transactionAquirerResponse =
            paymentDetailsLatestResponse?.data?.AcquirerResponse?.firstOrNull()?.let {
                AppJson.decodeFromString<AquirerResponse>(
                    it
                )
            }

    val transactionDetailUrl = "https://dev.paymentoptions.com/daspay-transaction-details/$transactionId"

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
                painter = painterResource(R.drawable.logo),
                contentDescription = "DASPay Logo",
                tint = primary500,
                modifier = Modifier
                    .height(LOGO_HEIGHT_IN_DP.div(1.5f))
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

//            val linkQrBitmap = generateQrCode("http://www.google.com")
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

    // Show shimmer loading while API is loading
    if (isLoading) {
        ReceiptShimmerLoading()
    } else {
        val formattedAmount = String.format(Locale.US, "%.2f", amount.toDoubleOrNull() ?: 0.0)
        val currency = paymentDetailsLatestResponse?.data?.CurrencyCode ?: DPSharedPreferences.getTransactionCurrency(context)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Status and Amount Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 30.dp, bottom = 20.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TransactionColors.Green,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = primary500,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                CurrencyText(
                    currency = currency,
                    amount = formattedAmount
                )
           }

            // Transaction Details Card
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = com.paymentoptions.pos.ui.theme.containerBackgroundGradientBrush,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .verticalScroll(state = rememberScrollState(), enabled = enableScrolling),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP))

                // Transaction Details Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TransactionDetailRow(
                        label = "Reference No",
                        value = referenceId
                    )

                    TransactionDetailRow(
                        label = "Date",
                        value = try {
                            val utcDateTime = safeParseOffsetDateTime(dateString)
                            val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy")
                            utcDateTime.format(formatter)
                        } catch (e: Exception) {
                            "N/A"
                        }
                    )

                    TransactionDetailRow(
                        label = "Aggregator",
                        value = aggregator
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                    color = Color.LightGray.copy(alpha = 0.2f)
                )

                // Share Section
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
                            text = "Scan",
                            modifier = Modifier
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

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ── Signature Section ──────────────────────────────────────
                if (isSignatureLoading) {
                    // Shimmer placeholder while loading
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(8.dp))
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
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    val signatureUrl = signatureData?.signatureURL?.toString()
                    if (signatureData?.imageExists == true && !signatureUrl.isNullOrBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
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
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptContentForPDF(
    paymentDetailsLatestResponse: PaymentDetailsResponse?,
    signatureBitmap: Bitmap?,
    signatureDate: Date,
) {
    var transactionAquirerResponse by remember { mutableStateOf<AquirerResponse?>(AquirerResponse()) }

    val pdfDateFormatted = try {
        val dateString = paymentDetailsLatestResponse?.data?.Date.toString()
        val timezoneId = paymentDetailsLatestResponse?.data?.TransactionTimezone.toString()

        val utcDateTime = java.time.OffsetDateTime.parse(dateString)
        val transactionZoneId = java.time.ZoneId.of(timezoneId)
        val localDateTime = utcDateTime.atZoneSameInstant(transactionZoneId)

        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss (z)")
        localDateTime.format(formatter)
    } catch (e: Exception) {
        paymentDetailsLatestResponse?.data?.Date.toString()
    }

    val pdfMerchantAddressFormatted = try {
        val address = paymentDetailsLatestResponse?.data?.PrimaryAddress
        if (address == null) {
            "Address unavailable"
        } else {
            val parts = listOfNotNull(
                address.Line1?.takeIf { it.isNotBlank() },
                address.Line2?.takeIf { it.isNotBlank() },
                address.Line3?.takeIf { it.isNotBlank() },
                address.Line4?.takeIf { it.isNotBlank() },
                address.Locality?.takeIf { it.isNotBlank() },
                address.Region?.takeIf { it.isNotBlank() },
                address.PostCode?.takeIf { it.isNotBlank() },
                address.Country?.takeIf { it.isNotBlank() }
            )
            parts.joinToString(", ").ifEmpty { "Address unavailable" }
        }
    } catch (e: Exception) {
        "Address unavailable"
    }

    if (paymentDetailsLatestResponse != null)
        transactionAquirerResponse =
            paymentDetailsLatestResponse.data.AcquirerResponse.firstOrNull()?.let {
                AppJson.decodeFromString<AquirerResponse>(
                    it
                )
            }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
//                text = "tx ${paymentDetailsLatestResponse?.data?.TransactionID}",
                text = "tx# ${paymentDetailsLatestResponse?.data?.TransactionRefID}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = purple50
            )
        }

        Text(
//            text = "Payment Options",
            text = paymentDetailsLatestResponse?.data?.Merchant.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = primary500
        )

        Text(
//            text = "9 Tamasek Boulevard, Suntec City Tower 2 19-02 Singapore 038989",
            text = pdfMerchantAddressFormatted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = purple50
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Payment",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = primary500
            )
            Text(
//                text = paymentDetailsLatestResponse?.data?.Scheme.toString(),
                text = transactionAquirerResponse?.paymentMethod.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = primary500
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
//                text = "Approved",
                paymentDetailsLatestResponse?.data?.Status.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = primary500
            )
            Text(
//                text = paymentDetailsLatestResponse?.data?.Date.toString(),
                text = pdfDateFormatted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = purple50
            )
        }

        Text(
//            text = "10:25T",
            text = "**** **** **** " + transactionAquirerResponse?.accountLast4.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = primary500
        )

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = Color.LightGray.copy(alpha = 0.2f)
        )

        // Total Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = primary500
            )
            CurrencyText(
                currency = paymentDetailsLatestResponse?.data?.CurrencyCode.toString(),
                amount = paymentDetailsLatestResponse?.data?.Amount.formatToPrecisionString(),
                fontSize = 20.sp,
                color = primary500
            )
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = Color.LightGray.copy(alpha = 0.2f)
        )

        // Transaction Details
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "MID",
                    style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal),
                    fontSize = 14.sp
                )
                Text(
//                    text = transactionAquirerResponse?.primaryMid.toString(),
                    text = paymentDetailsLatestResponse?.data?.DASMID.toString(),
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
                    "TID",
                    style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal),
                    fontSize = 14.sp
                )
                Text(
//                    text = transactionAquirerResponse?.primaryTid.toString(),
                    text = paymentDetailsLatestResponse?.data?.TerminalID.toString(),
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
                    "Batch",
                    style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal),
                    fontSize = 14.sp
                )
                Text(
                    text = transactionAquirerResponse?.batchNo.toString(),
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
                    "Trace",
                    style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal),
                    fontSize = 14.sp
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
                    "RRN",
                    style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal),
                    fontSize = 14.sp
                )
                Text(
                    text = transactionAquirerResponse?.rrn.toString(),
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
                    "Approval Code",
                    style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal),
                    fontSize = 14.sp
                )
                Text(
                    text = transactionAquirerResponse?.approvalCode.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = primary500
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = Color.LightGray.copy(alpha = 0.2f)
        )

        // Additional Information
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Additional Information",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = primary500
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    "TRANSACTION ID",
                    style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
//                    paymentDetailsLatestResponse?.data?.TransactionID.toString(),
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
                    "STATE",
                    style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal),
                    fontSize = 14.sp
                )
                Text(
//                    paymentDetailsLatestResponse?.data?.Status.toString(),
                    text = transactionAquirerResponse?.tranStatus.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = green500
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "ATC",
                    style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal),
                    fontSize = 14.sp
                )
                Text(
                    text = transactionAquirerResponse?.atc.toString(),
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
                    "TVR",
                    style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal),
                    fontSize = 14.sp
                )
                Text(
                    text = transactionAquirerResponse?.tvr.toString(),
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
                    "APP NAME",
                    style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal),
                    fontSize = 14.sp
                )
                Text(
                    text = transactionAquirerResponse?.appName.toString(),
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
                    "AID",
                    style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal),
                    fontSize = 14.sp
                )
                Text(
                    text = transactionAquirerResponse?.aid.toString(),
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
                    "TC",
                    style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal),
                    fontSize = 14.sp
                )
                Text(
                    text = transactionAquirerResponse?.tc.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = primary500
                )
            }
        }

        // Signature
        if (signatureBitmap != null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxWidth()
                        .height(150.dp)
                        //.aspectRatio(16 / 9f)
                        .dashedBorder(color = Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
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
                                withStyle(SpanStyle(purple50, fontWeight = FontWeight.Medium)) {
                                    append("Signing at ")
                                }
                                withStyle(SpanStyle(primary500)) {
                                    append(SimpleDateFormat("dd MMMM, YYYY").format(signatureDate))
                                }
                            },
                            style = AppTheme.typography.footnote
                        )
                    }
                    Image(
                        bitmap = signatureBitmap.asImageBitmap(),
                        contentDescription = "Customer signature",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptShimmerLoading() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Section 1 - Transaction Info shimmer
        Column(
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth()
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Transaction ID shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )

            // Merchant name shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )

            // Address shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Payment row shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }

            // Status shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )

            // Date shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )

            // Card number shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = Color.LightGray.copy(alpha = 0.2f)
        )

        // Section 2 - Total shimmer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = Color.LightGray.copy(alpha = 0.2f)
        )

        // Section 3 - Details shimmer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(6) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = Color.LightGray.copy(alpha = 0.2f)
        )

        // Section 4 - Signature shimmer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerEffect()
            )
        }

        // Section 5 - Share buttons shimmer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .shimmerEffect()
                )
            }
        }
    }
}

@Composable
private fun TransactionDetailRow(
    label: String,
    value: String,
    valueColor: Color = primary500
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = AppTheme.typography.footnote.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp
            ),
            modifier = Modifier.padding(end = 8.dp)
        )

        Text(
            text = value,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}
