package com.paymentoptions.pos.ui.composables.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Money
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.borderThinError
import com.paymentoptions.pos.ui.theme.green200
import com.paymentoptions.pos.ui.theme.green500
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.ui.theme.red500
import com.paymentoptions.pos.utils.timeAgo
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Date

@Composable
fun NotificationSummary(transaction: TransactionListDataRecord) {

    val successful = transaction.status == "SUCCESSFUL"
    val transactionAmount = transaction.amount.toFloat()
    val isTransactionAmountPositive = transactionAmount > 0

    val dateString = transaction.Date   //"2025-04-23T03:38:57.349+00:00"
    val dateTime = OffsetDateTime.parse(dateString)
    val date: Date = Date.from(dateTime.toInstant())

    val isCardTransaction = transaction.PaymentType == "CARDPAYMENT"
    val dateStringFormatted: String = SimpleDateFormat("dd MMMM, YYYY").format(date)
    val timeAgoString = dateTime.toInstant().toEpochMilli().timeAgo()

    val borderRadius = 20.dp

    val dateStr = buildAnnotatedString {
        withStyle(
            SpanStyle(
                purple50, fontWeight = FontWeight.Medium, fontSize = 12.sp
            )
        ) { append(dateStringFormatted) }


        append("   ")


        withStyle(
            SpanStyle(
                color = Color(0xFF868686), fontSize = 12.sp, fontWeight = FontWeight.Medium,
            )
        ) { append(timeAgoString) }
    }

    Card(
        colors = CardDefaults.cardColors().copy(
            containerColor = Color.White
        ),
        border = if (successful) borderThin else borderThinError,
        shape = RoundedCornerShape(borderRadius),
        modifier = Modifier.shadow(
            elevation = 4.dp,
            shape = RoundedCornerShape(borderRadius),
            ambientColor = Color(0xFFD8F0FF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBackgroundColor), contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCardTransaction) Icons.Outlined.CreditCard else Icons.Outlined.Money,
                    contentDescription = "Icon"
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(8f), verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        transaction.status.uppercase(),
                        modifier = Modifier.background(green200.copy(alpha = 0.2f)),
                        color = green500
                    )

                    if (false) Text(
                        transaction.TransactionType.uppercase(),
                        modifier = Modifier.background(Color.Yellow.copy(alpha = 0.2f)),
                        color = green500
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    dateStr, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = purple50
                )

                Text(
                    text = (if (isCardTransaction) "Txn ID - " else "Cash Id - ") + transaction.TransactionID.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = primary500
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .weight(3f)
                    .align(Alignment.Bottom)
            ) {
                Text(
                    transaction.CurrencyCode,
                    textAlign = TextAlign.End,
                    color = if (isTransactionAmountPositive) green200 else red500
                )

                Text(
                    text = if (isTransactionAmountPositive) "+${transaction.amount}" else transaction.amount,
                    color = if (isTransactionAmountPositive) green500 else red500,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}