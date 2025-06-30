package com.paymentoptions.pos.ui.composables.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Money
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.ui.theme.green200
import com.paymentoptions.pos.ui.theme.green500
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary200
import com.paymentoptions.pos.ui.theme.primary500
import java.time.OffsetDateTime
import java.util.Date

@Composable
fun TransactionSummary(transaction: TransactionListDataRecord) {

    val transactionAmount = transaction.amount.toFloat()
    val isTransactionAmountPositive = transactionAmount > 0

    val dateString = transaction.Date   //"2025-04-23T03:38:57.349+00:00"
    val dateTime = OffsetDateTime.parse(dateString)
    val date: Date = Date.from(dateTime.toInstant())

    val isCardTransaction = true

    Card(
        colors = CardDefaults.cardColors().copy(
            containerColor = Color.White,
        ),
        border = BorderStroke(1.dp, primary100.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(32.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Surface(
                shape = RoundedCornerShape(20.dp), modifier = Modifier
                    .width(60.dp)
                    .height(60.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(primary100.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,

                    ) {
                    Icon(
                        imageVector = if (isCardTransaction) Icons.Outlined.CreditCard else Icons.Outlined.Money,
                        contentDescription = "Card",
                        modifier = Modifier.background(Color.Transparent),
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(8f)) {
                Text(
                    date.toString(),
                    fontWeight = FontWeight.Light,
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

            Spacer(modifier = Modifier.width(8.dp))

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
}