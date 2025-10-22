package com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow.receipt

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.print.PrintHelper
import com.paymentoptions.pos.R
import com.paymentoptions.pos.services.apiService.PaymentDetailsResponse
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.NoteChip
import com.paymentoptions.pos.ui.composables._components.buttons.Email
import com.paymentoptions.pos.ui.composables._components.buttons.EmailButton
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
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
import com.paymentoptions.pos.utils.formatToPrecisionString
import com.paymentoptions.pos.utils.generateQrCode
import com.paymentoptions.pos.utils.modifiers.dashedBorder
import com.paymentoptions.pos.utils.topdf.ComposePdfExporter
import com.paymentoptions.pos.utils.topdf.PageSize
import com.paymentoptions.pos.utils.topdf.PdfExportProgress
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, ExperimentalComposeApi::class
)
@Composable
fun ReceiptBottomSectionContent(
    navController: NavController,
    paymentDetailsResponse: PaymentDetailsResponse?,
    signatureBitmap: Bitmap?,
    signatureDate: Date,
    enableScrolling: Boolean = false,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showQrCodeBottomSheetExpanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val clipboardManager = LocalClipboardManager.current

    val transactionUuid = paymentDetailsResponse?.data?.TransactionRefID
    val transactionDetailUrl = if (transactionUuid != null) {
        "https://dev.paymentoptions.com/daspay-transaction-details/$transactionUuid"
    } else {
        null
    }

    //Sharable text summary for the failed Transaction
    if (paymentDetailsResponse != null) {
        "Receipt for transaction #${paymentDetailsResponse.data.TransactionID}\nAmount: ${paymentDetailsResponse.data.CurrencyCode}${paymentDetailsResponse.data.Amount}"
    } else {
        "Receipt details are unavailable"
    }

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
                    .height(
                        LOGO_HEIGHT_IN_DP.div(1.5f)
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
    val captureController = rememberCaptureController()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
            .verticalScroll(state = rememberScrollState(), enabled = enableScrolling),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        //Section 1
        Column(
            modifier = Modifier
                .capturable(captureController)  //this captures the view to print
                .background(Color.White)
                .fillMaxWidth()
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SelectionContainer {
                    Text(
                        text = "tx# " + paymentDetailsResponse?.data?.TransactionID,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = purple50
                    )
                }

                Icon(
                    Icons.Default.CopyAll,
                    contentDescription = "Copy transaction id",
                    tint = primary500,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable {
                            clipboardManager.setText(AnnotatedString(paymentDetailsResponse?.data?.TransactionID.toString()))
                        }

                )
            }

            Text(
                text = "Payment Options",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = primary500
            )

            Text(
                text = "9 Tamasek Boulevard, Suntec City Tower 2 # 19-02 Singapore 038989",
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
                    text = paymentDetailsResponse?.data?.Scheme.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = primary500
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Approved",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = primary500
                )
                Text(
                    text = paymentDetailsResponse?.data?.Date.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = purple50
                )
            }

            Text(
                text = "**** **** **** 1025(T)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = primary500
            )
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(), color = Color.LightGray.copy(alpha = 0.2f)
        )

        //Section 2 : Total
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = primary500
            )

            CurrencyText(
                currency = paymentDetailsResponse?.data?.CurrencyCode.toString(),
//                amount = paymentDetailsResponse?.data?.Amount.toString(),
                amount = String.format(Locale.US, "%.2f", paymentDetailsResponse?.data?.Amount ?: 0.0),
                fontSize = 20.sp,
                color = primary500
            )
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(), color = Color.LightGray.copy(alpha = 0.2f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "MID", style = AppTheme.typography.footnote.copy(
                        fontWeight = FontWeight.Normal, fontSize = 14.sp
                    )
                )

                Text(
                    "******7890",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = primary500
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "TID", style = AppTheme.typography.footnote.copy(
                        fontWeight = FontWeight.Normal, fontSize = 14.sp
                    )
                )

                Text(
                    "****5678", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Batch", style = AppTheme.typography.footnote.copy(
                        fontWeight = FontWeight.Normal, fontSize = 14.sp
                    )
                )

                Text(
                    "000017", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Trace", style = AppTheme.typography.footnote.copy(
                        fontWeight = FontWeight.Normal, fontSize = 14.sp
                    )
                )

                Text(
                    "889026", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "RRN", style = AppTheme.typography.footnote.copy(
                        fontWeight = FontWeight.Normal, fontSize = 14.sp
                    )
                )

                Text(
                    "94445675305927",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = primary500
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Approval Code", style = AppTheme.typography.footnote.copy(
                        fontWeight = FontWeight.Normal, fontSize = 14.sp
                    )
                )

                Text(
                    "305927", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(), color = Color.LightGray.copy(alpha = 0.2f)
        )

        //Additional Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "TRANSACTION ID", style = AppTheme.typography.footnote.copy(
                        fontWeight = FontWeight.Normal, fontSize = 14.sp
                    )
                )

                Text(
                    paymentDetailsResponse?.data?.TransactionID.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = primary500
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "STATE", style = AppTheme.typography.footnote.copy(
                        fontWeight = FontWeight.Normal, fontSize = 14.sp
                    )
                )

                Text(
                    paymentDetailsResponse?.data?.Status.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = green500
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "DATE TIME", style = AppTheme.typography.footnote.copy(
                        fontWeight = FontWeight.Normal, fontSize = 14.sp
                    )
                )

                Text(
                    paymentDetailsResponse?.data?.TransactionID.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = primary500
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "ATC", style = AppTheme.typography.footnote.copy(
                        fontWeight = FontWeight.Normal, fontSize = 14.sp
                    )
                )

                Text(
                    "-", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "TVR", style = AppTheme.typography.footnote.copy(
                        fontWeight = FontWeight.Normal, fontSize = 14.sp
                    )
                )

                Text(
                    "040008000",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = primary500
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "APP NAME", style = AppTheme.typography.footnote.copy(
                        fontWeight = FontWeight.Normal, fontSize = 14.sp
                    )
                )

                Text(
                    "A800", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "AID", style = AppTheme.typography.footnote.copy(
                        fontWeight = FontWeight.Normal, fontSize = 14.sp
                    )
                )

                Text(
                    "A000000000250013543",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = primary500
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "TC", style = AppTheme.typography.footnote.copy(
                        fontWeight = FontWeight.Normal, fontSize = 14.sp
                    )
                )

                Text(
                    "110DD9C04027D889",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = primary500
                )
            }
        }

        //Signature
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            //Show Signature
            if (signatureBitmap != null) {
                Column(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f)
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
                                withStyle(
                                    SpanStyle(
                                        purple50, fontWeight = FontWeight.Medium
                                    )
                                ) { append("Signing at: ") }

                                withStyle(SpanStyle(primary500)) {
                                    append(
                                        SimpleDateFormat("dd MMMM, YYYY").format(
                                            signatureDate
                                        )
                                    )
                                }
                            }, style = AppTheme.typography.footnote
                        )
                    }

                    Image(
                        bitmap = signatureBitmap.asImageBitmap(),
                        contentDescription = "Customer signature"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                    text = "Email", email = Email(
                        subject = "Your DASPay Receipt",
//                        text = shareableReceiptText
                        text = transactionDetailUrl ?: "Transaction details unavailable"
                    ), modifier = Modifier
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
//                    shareContent = shareableReceiptText,
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
                        .clickable {
                            showQrCodeBottomSheetExpanded = true
                        })
            }

            Spacer(modifier = Modifier.height(20.dp))

//            bitmap?.let {
//                Image(
//                    modifier = Modifier.fillMaxWidth(),
//                    painter = rememberAsyncImagePainter(bitmap!!.asAndroidBitmap()),
//                    contentScale = ContentScale.FillBounds,
//                    contentDescription = null,
//                )
//            }

            /*FilledButton(
                text = "Print Receipt",
                onClick = {
                    scope.launch {
                        val bitmapAsync = captureController.captureAsync()
                        try {
                            bitmap = bitmapAsync.await()
                            Toast.makeText(
                                context,
                                "Screenshot taken successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            doPhotoPrint(context, bitmap!!.asAndroidBitmap())
                        } catch (error: Throwable) {
                            Toast.makeText(context, "Error taking screenshot", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .height(39.dp)
                    .scale(0.7f)
            )*/
            FilledButton(
                text = "Print Receipt",
                onClick = {
                    scope.launch {
                        try {
                            ComposePdfExporter.export(
                                context = context,
                                fileName = "Receipt_${paymentDetailsResponse?.data?.TransactionID}",
                                pageSize = PageSize.A4,
                                composable = { state ->
                                    LazyColumn(
                                        state = state,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White)
                                    ) {
                                        item {
                                            ReceiptContentForPDF(
                                                paymentDetailsResponse = paymentDetailsResponse,
                                                signatureBitmap = signatureBitmap,
                                                signatureDate = signatureDate
                                            )
                                        }
                                    }
                                },
                                onProgress = { result ->
                                    scope.launch(Dispatchers.Main) {
                                        when (result) {
                                            is PdfExportProgress.Success -> {
                                                try {
                                                    // Direct print intent
                                                    val printIntent = Intent(Intent.ACTION_VIEW).apply {
                                                        setDataAndType(result.output, "application/pdf")
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    }
                                                    context.startActivity(printIntent)
                                                    Toast.makeText(context, "PDF created - Opening...", Toast.LENGTH_SHORT).show()
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Error opening PDF: ${e.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                            is PdfExportProgress.Error -> {
                                                android.util.Log.e("PDF_ERROR", "Error creating PDF", result.exception)
                                                Toast.makeText(
                                                    context,
                                                    "Error: ${result.exception.message ?: "Unknown error"}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("PDF_ERROR", "Caught exception", e)
                            Toast.makeText(context, "Exception: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
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

@Composable
private fun ReceiptContentForPDF(
    paymentDetailsResponse: PaymentDetailsResponse?,
    signatureBitmap: Bitmap?,
    signatureDate: Date
) {
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
                text = "tx ${paymentDetailsResponse?.data?.TransactionID}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = purple50
            )
        }

        Text(
            text = "Payment Options",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = primary500
        )

        Text(
            text = "9 Tamasek Boulevard, Suntec City Tower 2 19-02 Singapore 038989",
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
                text = paymentDetailsResponse?.data?.Scheme.toString(),
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
                text = "Approved",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = primary500
            )
            Text(
                text = paymentDetailsResponse?.data?.Date.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = purple50
            )
        }

        Text(
            text = "10:25T",
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
                currency = paymentDetailsResponse?.data?.CurrencyCode.toString(),
                amount = paymentDetailsResponse?.data?.Amount.formatToPrecisionString(),
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
                Text("MID", style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal), fontSize = 14.sp)
                Text("7890", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("TID", style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal), fontSize = 14.sp)
                Text("5678", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Batch", style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal), fontSize = 14.sp)
                Text("000017", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Trace", style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal), fontSize = 14.sp)
                Text("889026", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("RRN", style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal), fontSize = 14.sp)
                Text("94445675305927", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Approval Code", style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal), fontSize = 14.sp)
                Text("305927", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500)
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("TRANSACTION ID", style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal), fontSize = 14.sp)
                Text(paymentDetailsResponse?.data?.TransactionID.toString(), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("STATE", style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal), fontSize = 14.sp)
                Text(paymentDetailsResponse?.data?.Status.toString(), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = green500)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("ATC", style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal), fontSize = 14.sp)
                Text("-", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("TVR", style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal), fontSize = 14.sp)
                Text("040008000", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("APP NAME", style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal), fontSize = 14.sp)
                Text("A800", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("AID", style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal), fontSize = 14.sp)
                Text("A000000000250013543", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("TC", style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal), fontSize = 14.sp)
                Text("110DD9C04027D889", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500)
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


private fun doPhotoPrint(context: Context, bitmap: Bitmap) {
    PrintHelper(context).apply {
        scaleMode = PrintHelper.SCALE_MODE_FIT
    }.also { printHelper ->
        printHelper.printBitmap("Receipt", bitmap)
    }
}

@Composable
fun PrintToPDF() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    FilledButton(
        text = "Print Receipt",
        onClick = {
            scope.launch {
                ComposePdfExporter.export(
                    context = context,
                    fileName = "Receipt",
                    pageSize = PageSize.A4,
                    spacing = 4,
                    composable = { state ->
                        LazyColumn(
                            state = state,
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .background(Color.Green.copy(0.1f))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Text("Capture Test 1", color = Color.Black)
                                Text("Capture Test 2", color = Color.Black)
                                Text("Capture Test 3", color = Color.Black)
                            }
                        }

                    },
                    onProgress = { result ->
                        when (result) {
                            is PdfExportProgress.Success -> {

                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_STREAM, result.output)
                                    flags += Intent.FLAG_ACTIVITY_NEW_TASK
                                    flags += Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    type = "text/pdf"
                                }
                                val chooser = Intent.createChooser(intent, null)
                                context.startActivity(chooser)

//                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
//                                    putExtra(Intent.EXTRA_STREAM, result.output)
//                                    type = "text/pdf"
//                                }
//                                val shareIntent = Intent.createChooser(sendIntent, null)
//                                startActivity(context, shareIntent, null)

//                                context.startActivity(
//                                    Intent.createChooser(
//                                        Intent().apply {
//                                            action = Intent.ACTION_SENDTO
//                                            putExtra(Intent.EXTRA_STREAM, result.output)
//                                            type = "application/pdf"
//                                        },
//                                        null
//                                    )
//                                )
                            }

                            is PdfExportProgress.Error -> {
//                                toastManager.show(
//                                    result.exception.localizedMessage
//                                        ?: context.getString(
//                                            R.string.unknown_error
//                                        )
//                                )
                            }

                            else -> {}
                        }
                    })
            }
        },
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .height(39.dp)
            .scale(0.7f)
    )
}