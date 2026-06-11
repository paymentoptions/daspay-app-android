package com.paymentoptions.pos.ui.composables.screens.transactiondetails

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.paymentoptions.pos.R
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.device.DPStorageManager.getTransactionCurrency
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.endpoints.getSignature
import com.paymentoptions.pos.services.apiService.SignatureData
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.utils.getStatusColor
import com.paymentoptions.pos.utils.getStatusText
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
import com.paymentoptions.pos.ui.composables.layout.sectioned.LOGO_HEIGHT_IN_DP
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.containerBackgroundGradientBrush
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.TransactionAction
import com.paymentoptions.pos.utils.generateQrCode
import com.paymentoptions.pos.utils.getAmountSign
import com.paymentoptions.pos.utils.getAvailableAction
import com.paymentoptions.pos.utils.getTransactionTypeLabel
import com.paymentoptions.pos.utils.modifiers.shimmerEffect
import com.paymentoptions.pos.utils.safeParseOffsetDateTime
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionBottomSectionContent(
    navController: NavController,
    transaction: TransactionListDataRecord,
    updateDetailsScreenType: (TransactionDetailsScreenType) -> Unit,
) {
    AppLogger.debug("transaction to display : $transaction")
    val context = LocalContext.current
    val currency = getTransactionCurrency()
    val scrollState = rememberScrollState()
    val amountValue = transaction?.amount?.toDoubleOrNull() ?: 0.0
    AppLogger.debug("transaction clicked is : $transaction")

    val transactionUuid = transaction?.uuid
    val transactionDetailUrl = if (!transactionUuid.isNullOrEmpty()) {
        "${DPStorageManager.getTransactionDetailsUrl()}/$transactionUuid"
    } else {
        null
    }

    var signatureData by remember { mutableStateOf<SignatureData?>(null) }
    var isSignatureLoading by remember { mutableStateOf(false) }

    LaunchedEffect(transactionUuid) {
        if (!transactionUuid.isNullOrBlank()) {
            isSignatureLoading = true
            signatureData = try {
                getSignature(uuid = transactionUuid)?.data
            } catch (e: Exception) {
                AppLogger.error("TransactionBottomSectionContent getSignature error: ${e.message}")
                null
            }
            isSignatureLoading = false
        }
    }

    val dateString =
        transaction?.Date /*?: paymentDetailsLatestResponse?.data?.Date */ ?: OffsetDateTime.now()
            .toString()
    val dateTime = safeParseOffsetDateTime(dateString)
    val date: Date = Date.from(dateTime.toInstant())
    val dateStringFormatted: String = SimpleDateFormat("dd MMMM yyyy", Locale.US).format(date)
    val timeStringFormatted: String = SimpleDateFormat("hh:mm:ss a", Locale.US).format(date)

    val transactionStatus =
        transaction?.status/* ?: paymentDetailsLatestResponse?.data?.Status*/ ?: ""
    val transactionType =
        transaction?.TransactionType /*?: paymentDetailsLatestResponse?.data?.TransactionType*/
            ?: ""
//    val transactionAmount =
//        transaction?.amount?.toDoubleOrNull() /*?: paymentDetailsLatestResponse?.data?.Amount*/
//            ?: 0.0
//    val transactionCurrencyCode =
//        transaction?.CurrencyCode /*?: paymentDetailsLatestResponse?.data?.CurrencyCode */
//            ?: currency
//    val transactionRefId =
//        transaction?.uuid /*?: paymentDetailsLatestResponse?.data?.TransactionRefID.toString()*/

    val amountSign = getAmountSign(transaction)
    val availableAction = getAvailableAction(transaction)
    val transactionTypeLabel = getTransactionTypeLabel(transaction)



    // Format the amount with sign
    val formattedAmount = if(transaction.amount.toFloat() == 0.toFloat()){
        "0.00"
    } else {
        when (amountSign) {
            "+" -> "+${"%.2f".format(transaction.amount.toFloat())}"
            "-" -> "-${"%.2f".format(transaction.amount.toFloat())}"
            else -> "%.2f".format(transaction.amount.toFloat())
        }
    }

    val statusText = getStatusText(transaction)
    val statusColor = getStatusColor(transaction)

    AppLogger.debug("TransactionSummary availableAction: $availableAction, amountSignIn :"
            + "$amountSign , transactionTypeLabel: $transactionTypeLabel, statusColor: $statusColor")


    fun navigateToVoidAction(transaction: TransactionListDataRecord){
        AppLogger.debug("full transaction object: $transaction")
        val transactionJson = Json.encodeToString(transaction)
        navController.navigate(Screens.TransactionAction.createRoute(transactionJson, "VOID"))
    }

    fun navigateToRefundAction(transaction: TransactionListDataRecord){
        AppLogger.debug("full transaction object: $transaction")
        val transactionJson = Json.encodeToString(transaction)
        navController.navigate(Screens.TransactionAction.createRoute(transactionJson, "REFUND"))
    }

    var showQrCodeBottomSheetExpanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showQrCodeBottomSheetExpanded) ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = { showQrCodeBottomSheetExpanded = false },
        sheetState = sheetState,
        containerColor = Color.White,
        contentColor = primary500,
        dragHandle = {}
    ) {
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
                    .height(
                        LOGO_HEIGHT_IN_DP.div(
                            1.5f
                        )
                    )
                    .align(Alignment.Center)
            )

            IconButton(
                modifier = Modifier.align(alignment = Alignment.CenterEnd),
                onClick = { showQrCodeBottomSheetExpanded = false }
            ) {
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
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
            .padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScreenTitleWithCloseButton(
            navController = navController,
            fontSize = 8.sp,
            onClose = { navController.popBackStack() }
        )


        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = (-20).dp)
        ) {
            Text(
                text = statusText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = statusColor,
            )

            CurrencyText(
                currency = currency,
                amount = formattedAmount
            )


            Row(
                modifier = Modifier.scale(0.7f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(availableAction != TransactionAction.NONE){
                    OutlinedButton(
                        text = if(availableAction == TransactionAction.REFUND)"Refund" else "VOID",
                        onClick = {
                            when(availableAction){
                                TransactionAction.VOID -> navigateToVoidAction(transaction)
                                TransactionAction.REFUND -> navigateToRefundAction(transaction)
                                else ->{}
                            }
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                    )
                }


                Spacer(modifier = Modifier.width(10.dp))

                FilledButton(
                    text = "View Full Receipt",
                    onClick = { updateDetailsScreenType(TransactionDetailsScreenType.RECEIPT_SCREEN) },
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
                    brush = containerBackgroundGradientBrush,
                    shape = RoundedCornerShape(20.dp)
                )
                .verticalScroll(scrollState),
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
                if(transaction?.TransactionID != 0){
                TransactionDetailRow(
                    label = "Transaction ID",
                    value = transaction?.TransactionID.toString()
                )
                    }

                TransactionDetailRow(
                    label = "Date",
                    value = dateStringFormatted
                )

                TransactionDetailRow(
                    label = "Time",
                    value = timeStringFormatted
                )

                TransactionDetailRow(
                    label = "Status",
                    value = transactionStatus,
                    valueColor = statusColor
                )

                TransactionDetailRow(
                    label = "Transaction Type",
                    value = transactionTypeLabel
                )

//                    TransactionDetailRow(
//                        label = "Trace",
//                        value = transactionAquirerResponse?.trace.toString()
//                    )
//
//                    TransactionDetailRow(
//                        label = "Approval Code",
//                        value = transactionAquirerResponse?.approvalCode.toString()
//                    )
//
//                    TransactionDetailRow(
//                        label = "Payment Method",
//                        value = transactionAquirerResponse?.paymentMethod.toString()
//                    )

                TransactionDetailRow(
                    label = "Currency",
                    value = currency
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

                // Signature section (server-side by transaction UUID)
                if (isSignatureLoading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
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
                    }
                } else {
                    val signatureUrl = signatureData?.signatureURL?.toString()
                    if (signatureData?.imageExists == true && !signatureUrl.isNullOrBlank()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
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
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
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
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
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
                fontSize = 14.sp
            ),
            modifier = Modifier.padding(end = 8.dp)
        )

        Text(
            text = value,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

