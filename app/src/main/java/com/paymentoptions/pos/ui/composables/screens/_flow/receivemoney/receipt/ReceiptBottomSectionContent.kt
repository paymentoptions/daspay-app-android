package com.paymentoptions.pos.ui.composables.screens._flow.receivemoney.receipt

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.buttons.Email
import com.paymentoptions.pos.ui.composables._components.buttons.EmailButton
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.buttons.ScanButton
import com.paymentoptions.pos.ui.composables._components.buttons.ShareButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.green500
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.utils.modifiers.dashedBorder
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun ReceiptBottomSectionContent(
    navController: NavController,
    transaction: TransactionListDataRecord?,
    signatureBitmap: Bitmap?,
    signatureDate: Date,
    enableScrolling: Boolean = false,
) {
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
                .fillMaxWidth()
                .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SelectionContainer {
                Text(
                    text = "tx# " + transaction?.uuid,
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
                text = "No.8, Victoria Road, Here’s your receipt",
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
                    text = "AMEX",
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
                    text = transaction?.Date.toString(),
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

        HorizontalDivider(modifier = Modifier.fillMaxWidth())

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
                currency = transaction?.CurrencyCode.toString(),
                amount = transaction?.amount.toString(),
                fontSize = 20.sp,
                color = primary500
            )
        }

        HorizontalDivider(modifier = Modifier.fillMaxWidth())


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


        HorizontalDivider(modifier = Modifier.fillMaxWidth())

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
                    transaction?.uuid.toString(),
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
                    transaction?.status.toString(),
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
                    transaction?.uuid.toString(),
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
                    text = "Email", email = Email(), modifier = Modifier
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
                    text = "Share", modifier = Modifier
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
                )
            }

            Spacer(modifier = Modifier.height(20.dp))


            FilledButton(
                text = "Print Receipt",
                onClick = { },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .height(39.dp)
                    .scale(0.7f)
            )


        }

    }

}