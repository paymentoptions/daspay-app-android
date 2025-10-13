package com.paymentoptions.pos.ui.composables.screens.transactionshistory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.R
import com.paymentoptions.pos.services.apiService.InsightsResponseDataRecord
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.ui.theme.red300
import com.paymentoptions.pos.ui.theme.red500
import com.paymentoptions.pos.utils.timeAgo
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Date

@Composable
fun TransactionSummary(
    navController: NavController,
    transaction: InsightsResponseDataRecord,
    longClickedTransactionId: String = "",
    onLongClick: (String) -> Unit = {},
    onSwipeLeft: (String) -> Unit = {},
    onSwipeRight: (String) -> Unit = {},
) {
    val transactionAmount = transaction.amount.toFloat()
    val isTransactionAmountPositive = transactionAmount > 0
    val isCardTransaction = transaction.paymentMethod == "CARDPAYMENT"
    val dateString = transaction.TransactionDate   //"2025-04-23T03:38:57.349+00:00"
    val dateTime = OffsetDateTime.parse(dateString)
    val date: Date = Date.from(dateTime.toInstant())
    val dateStringFormatted = SimpleDateFormat("dd MMMM, YYYY").format(date)
    val timeAgoString = dateTime.toInstant().toEpochMilli().timeAgo()
    var isLongClicked = longClickedTransactionId == transaction.uuid.toString()

    val hourPart = SimpleDateFormat("hh:mm:ss a").format(date)
    val borderRadius = 20.dp
    var offsetX by remember { mutableStateOf(0f) }

    val statusColor = when {
        transaction.TransactionType.uppercase() == "REFUND" -> Color(0xFFFC8D3E)  // Orange
        transaction.status.uppercase() == "SUCCESSFUL" -> Color(0xFF22C55E)  // Green
        else -> Color(0xFFD52121)  // Red
    }

    val dateStr = buildAnnotatedString {
        withStyle(
            SpanStyle(
                purple50, fontWeight = FontWeight.Medium, fontSize = 12.sp
            )
        ) { append(dateStringFormatted) }

        append("  ")

        withStyle(
            SpanStyle(
                color = purple50, fontSize = 12.sp, fontWeight = FontWeight.Medium,
            )
        ) { append(if (timeAgoString.endsWith("seconds ago")) timeAgoString else hourPart) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isLongClicked) 0.dp else DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
        horizontalArrangement = Arrangement.Absolute.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(
                topStart = if (isLongClicked) 0.dp else borderRadius,
                topEnd = borderRadius,
                bottomStart = if (isLongClicked) 0.dp else borderRadius,
                bottomEnd = borderRadius
            ),
            modifier = Modifier
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(borderRadius),
                    ambientColor = Color(0xFF64B5F6).copy(alpha = 0.8f),
                    spotColor = Color(0xFF2196F3).copy(alpha = 0.6f)
                )
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(onDragEnd = {
                        if (offsetX > 200f) {
                            // Swiped
                            onSwipeRight(transaction.uuid.toString())
                        } else if (offsetX < -200f) {
                            // Swiped Left
                            onSwipeLeft(transaction.uuid.toString())
                        }
                        offsetX = 0f // reset position
                    }, onHorizontalDrag = { _, dragAmount ->
                        offsetX += dragAmount
                    })
                }
                .combinedClickable(onClick = {}, onLongClick = {
//                    onLongClick(transaction.uuid.toString())
                })
                .weight(if (isLongClicked) 8f else 1f)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (!isLongClicked) Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBackgroundColor), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(if (isCardTransaction) R.drawable.icon_card else R.drawable.icon_money),
                        contentDescription = "Icon",
                        tint = statusColor
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(8f), verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        dateStr, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = purple50
                    )
                    Text(
                        text = "Txn ID - ${transaction.ID}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = primary500
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(3f)
                ) {
                    Text(
                        transaction.CurrencyCode,
                        textAlign = TextAlign.End,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )

                    Text(
                        text = if (isTransactionAmountPositive) "+${transaction.amount}" else transaction.amount.toString(),
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                }
            }
        }


        if (isLongClicked) Column(
            modifier = Modifier
                .padding(end = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                .background(
                    red300.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)
                )
                .padding(6.dp)
                .weight(2f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.refund),
                tint = red500,
                contentDescription = "Refund",
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Refund", color = red500, fontSize = 11.sp, fontWeight = FontWeight.Medium
            )
        }
    }
}