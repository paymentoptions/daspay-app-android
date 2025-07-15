package com.paymentoptions.pos.ui.composables.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Money
import androidx.compose.material.icons.outlined.Refresh
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.green200
import com.paymentoptions.pos.ui.theme.green500
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.primary200
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.red300
import com.paymentoptions.pos.ui.theme.red500
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Date

var TRANSACTION_TO_BE_REFUNDED: TransactionListDataRecord? = null

@Composable
fun TransactionSummary(
    navController: NavController,
    transaction: TransactionListDataRecord,
    longClickedTransactionId: String = "",
    onLongClick: (String) -> Unit = {},
) {

    val transactionAmount = transaction.amount.toFloat()
    val isTransactionAmountPositive = transactionAmount > 0
    val isCardTransaction = transaction.PaymentType == "CARDPAYMENT"
    val dateString = transaction.Date   //"2025-04-23T03:38:57.349+00:00"
    val dateTime = OffsetDateTime.parse(dateString)
    val date: Date = Date.from(dateTime.toInstant())
    val dateStringFormatted = SimpleDateFormat("dd MMMM YYYY").format(date)
    var isLongClicked = longClickedTransactionId == transaction.TransactionID.toString()

//    val niceDateStr: String? = DateUtils.getRelativeTimeSpanString(
//        now = date.getTime(),
//        time = Calendar.getInstance().getTimeInMillis(),
//        DateUtils.MINUTE_IN_MILLIS
//    )

    val borderRadius = 20.dp
    val haptics = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isLongClicked) 0.dp else DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
        horizontalArrangement = Arrangement.Absolute.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Card(
            colors = CardDefaults.cardColors().copy(containerColor = Color.White),
            border = borderThin,
            shape = RoundedCornerShape(
                topStart = if (isLongClicked) 0.dp else borderRadius,
                topEnd = borderRadius,
                bottomStart = if (isLongClicked) 0.dp else borderRadius,
                bottomEnd = borderRadius
            ),
            modifier = Modifier
                .shadow(
                    elevation = if (isLongClicked) 16.dp else 4.dp, shape = RoundedCornerShape(
                        topStart = if (isLongClicked) 0.dp else borderRadius,
                        topEnd = borderRadius,
                        bottomStart = if (isLongClicked) 0.dp else borderRadius,
                        bottomEnd = borderRadius
                    ), ambientColor = Color(0xFFD8F0FF)
                )
                .combinedClickable(onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.ToggleOn)
                }, onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick(transaction.TransactionID.toString())
                })
                .weight(if (isLongClicked) 8f else 1f)
        ) {

            Row(
                modifier = Modifier.padding(22.dp),
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
                        imageVector = if (isCardTransaction) Icons.Outlined.CreditCard else Icons.Outlined.Money,
                        contentDescription = "Icon"
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(8f)) {
                    Text(
                        dateStringFormatted,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = primary200
                    )
                    Text(
                        text = (if (isCardTransaction) "Txn ID - " else "Cash Id - ") + transaction.TransactionID.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = primary500
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(3f)) {
                    Text(
                        transaction.CurrencyCode,
                        textAlign = TextAlign.End,
                        color = if (isTransactionAmountPositive) green200 else Color.Red
                    )

                    Text(
                        text = if (isTransactionAmountPositive) "+${transaction.amount}" else transaction.amount,
                        color = if (isTransactionAmountPositive) green500 else Color.Red,
                        fontWeight = FontWeight.Bold,
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
                .padding(8.dp)
                .weight(if (isLongClicked) 2f else 0.1f)
                .clickable {
                    navController.navigate(Screens.Refund.route)
                    TRANSACTION_TO_BE_REFUNDED = transaction
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Outlined.Refresh,
                tint = red500,
                contentDescription = "Refund",
            )

            Text(
                text = "Refund", color = red500, fontSize = 12.sp, fontWeight = FontWeight.Medium
            )
        }
    }
}