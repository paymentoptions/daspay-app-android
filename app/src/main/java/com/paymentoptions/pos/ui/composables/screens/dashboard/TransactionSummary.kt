package com.paymentoptions.pos.ui.composables.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.borderColor
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.borderThinError
import com.paymentoptions.pos.ui.theme.green200
import com.paymentoptions.pos.ui.theme.green500
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.ui.theme.red300
import com.paymentoptions.pos.ui.theme.red500
import com.paymentoptions.pos.utils.timeAgo
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Date

var TRANSACTION_TO_BE_REFUNDED: TransactionListDataRecord? = null

//Swipe / Drag code

//var pointerOffset by remember {
//    mutableStateOf(Offset(0f, 0f))
//}
//Column(
//modifier = Modifier
//.fillMaxSize()
//.pointerInput("dragging") {
//    detectDragGestures { change, dragAmount ->
//        pointerOffset += dragAmount
//    }
//}
//.onSizeChanged {
//    pointerOffset = Offset(it.width / 2f, it.height / 2f)
//}
//.drawWithContent {
//    drawContent()
//    // draws a fully black area with a small keyhole at pointerOffset that’ll show part of the UI.
//    drawRect(
//        Brush.radialGradient(
//            listOf(Color.Transparent, Color.Black),
//            center = pointerOffset,
//            radius = 100.dp.toPx(),
//        )
//    )
//}
//) {
//    // Your composables here
//}

@Composable
fun TransactionSummary(
    navController: NavController,
    transaction: TransactionListDataRecord,
    longClickedTransactionId: String = "",
    onLongClick: (String) -> Unit = {},
    onSwipeLeft: (String) -> Unit = {},   // 👈 added
    onSwipeRight: (String) -> Unit = {}
) {
    val successful = transaction.status == "SUCCESSFUL"
    val transactionAmount = transaction.amount.toFloat()
    val isTransactionAmountPositive = transactionAmount > 0
    val isCardTransaction = transaction.PaymentType == "CARDPAYMENT"
    val dateString = transaction.Date   //"2025-04-23T03:38:57.349+00:00"
    val dateTime = OffsetDateTime.parse(dateString)
    val date: Date = Date.from(dateTime.toInstant())
    val dateStringFormatted = SimpleDateFormat("dd MMMM, YYYY").format(date)
    val timeAgoString = dateTime.toInstant().toEpochMilli().timeAgo()
    var isLongClicked = longClickedTransactionId == transaction.TransactionID.toString()

    val hourPart = SimpleDateFormat("hh:mm:ss a").format(date)
    val borderRadius = 20.dp
    val haptics = LocalHapticFeedback.current
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

        /**Card(
            colors = CardDefaults.cardColors().copy(containerColor = Color.White),
            border = if (successful) borderThin else borderThinError,
            shape = RoundedCornerShape(
                topStart = if (isLongClicked) 0.dp else borderRadius,
                topEnd = borderRadius,
                bottomStart = if (isLongClicked) 0.dp else borderRadius,
                bottomEnd = borderRadius
            ),
            modifier = Modifier
                .shadow(
                    elevation = if (isLongClicked) 2.dp else 1.dp, shape = RoundedCornerShape(
                        topStart = if (isLongClicked) 0.dp else borderRadius,
                        topEnd = borderRadius,
                        bottomStart = if (isLongClicked) 0.dp else borderRadius,
                        bottomEnd = borderRadius
                    ), ambientColor = borderColor
                )
                **/


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
                    //ambientColor = Color.Black.copy(alpha = 0.2f),
                    //spotColor = Color.Black.copy(alpha = 0.2f)
                    ambientColor = Color(0xFF64B5F6).copy(alpha = 0.8f), // More opaque
                    spotColor = Color(0xFF2196F3).copy(alpha = 0.6f)
                )
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX > 200f) {
                                // Swiped

                                onSwipeRight(transaction.TransactionID.toString())
                            } else if (offsetX < -200f) {
                                // Swiped Left

                                onSwipeLeft(transaction.TransactionID.toString())
                            }
                            offsetX = 0f // reset position
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX += dragAmount
                        }
                    )
                }
                .combinedClickable(onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.ToggleOn)
                }, onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick(transaction.TransactionID.toString())
                })
                .weight(if (isLongClicked) 8f else 1f)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                /**if (!isLongClicked) Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBackgroundColor), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(if (isCardTransaction) R.drawable.icon_card else R.drawable.icon_money),
                        contentDescription = "Icon",
                        tint = purple50
                    )
                }**/
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
                        text = (if (isCardTransaction) "Txn ID - " else "Cash Id - ") + transaction.TransactionID.toString(),
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
                    /**Text(
                        transaction.CurrencyCode,
                        textAlign = TextAlign.End,
                        color = if (isTransactionAmountPositive) green200 else Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )

                    Text(
                        text = if (isTransactionAmountPositive) "+${transaction.amount}" else transaction.amount,
                        color = if (isTransactionAmountPositive) green500 else Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1
                    )**/
                    Text(
                        transaction.CurrencyCode,
                        textAlign = TextAlign.End,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )

                    Text(
                        text = if (isTransactionAmountPositive) "+${transaction.amount}" else transaction.amount,
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
                .weight(2f)
                .clickable {
                    navController.navigate(Screens.RefundTransaction.route)
                    TRANSACTION_TO_BE_REFUNDED = transaction
                },
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