package com.paymentoptions.pos.ui.composables.screens.dashboard

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.paymentoptions.pos.R
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.TransactionListDataRecord
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.ui.theme.red300
import com.paymentoptions.pos.ui.theme.red500
import com.paymentoptions.pos.ui.theme.refundColor
import com.paymentoptions.pos.utils.TransactionAction
import com.paymentoptions.pos.utils.TransactionColors
import com.paymentoptions.pos.utils.getAmountSign
import com.paymentoptions.pos.utils.getAvailableAction
import com.paymentoptions.pos.utils.getStatusColor
import com.paymentoptions.pos.utils.getTransactionIcon
import org.jetbrains.compose.resources.painterResource
import com.paymentoptions.pos.utils.getTransactionTypeLabel
import com.paymentoptions.pos.utils.timeAgo
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Date

var TRANSACTION_TO_BE_REFUNDED: TransactionListDataRecord? = null

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSummary(
    navController: NavController,
    transaction: TransactionListDataRecord,
) {
    val context = LocalContext.current
    val dateString = transaction.Date   //"2025-04-23T03:38:57.349+00:00"
    val dateTime = OffsetDateTime.parse(dateString)
    val date: Date = Date.from(dateTime.toInstant())
    val dateStringFormatted = SimpleDateFormat("dd MMMM, YYYY").format(date)
    val timeAgoString = dateTime.toInstant().toEpochMilli().timeAgo()
    var showAvailableAction by remember { mutableStateOf(false) }

    val hourPart = SimpleDateFormat("hh:mm:ss a").format(date)
    val borderRadius = 20.dp
    val haptics = LocalHapticFeedback.current
    var offsetX by remember { mutableStateOf(0f) }

    val statusColor = getStatusColor(transaction)
    val amountSign = getAmountSign(transaction)
    val transactionIcon = getTransactionIcon(transaction)
    val availableAction = getAvailableAction(transaction)
    val transactionTypeLabel = getTransactionTypeLabel(transaction)

    AppLogger.debug("TransactionSummary availableAction: $availableAction, amountSignIn :"
            + "$amountSign , transactionTypeLabel: $transactionTypeLabel, statusColor: $statusColor, "
            + "transactionIcon: $transactionIcon")

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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
        horizontalArrangement = Arrangement.Absolute.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(
                topStart = borderRadius,
                topEnd = borderRadius,
                bottomStart =  borderRadius,
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
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX > 200f) {
                                // Prevent right swipe - do nothing
                                showAvailableAction = false
                                offsetX = 0f
                            } else if (offsetX < -200f) {
                                // Left swipe - check if action is available
                                if (availableAction != TransactionAction.NONE) {
                                    showAvailableAction = true
                                } else {
                                    // Show toast that no action is available
                                    Toast.makeText(
                                        context,
                                        "No action available for this transaction",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            offsetX = 0f // reset position
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            // Only allow left swipe (negative drag)
                            if (availableAction != TransactionAction.NONE) {
                                // Allow both directions for visual feedback, but only left swipe triggers action
                                offsetX += dragAmount
                            } else if (dragAmount < 0) {
                                // Allow left drag for triggering toast
                                offsetX += dragAmount
                            }
                            // Prevent right swipe by not updating offsetX when dragAmount > 0 and no action
                        }
                    )
                }
                .combinedClickable(onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.ToggleOn)
                    val transactionJson = Json.encodeToString(transaction)
                    navController.navigate(Screens.TransactionDetails.createRoute(transactionJson))
                }, onLongClick = {
//                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
//                    onLongClick(transaction.TransactionID.toString())
                })
                .weight(if (showAvailableAction) 8.5f else 1f)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Transaction Icon with status-based color
                if (!showAvailableAction) Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(transactionIcon),
                        contentDescription = "Transaction Icon",
                        tint = statusColor
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(8f), verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Date and time row
                    Text(
                        dateStr, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = purple50
                    )

                    // Transaction Type - ID (e.g., "SALE - 54268" or "REFUND - 52345")
                    Text(
                        text = "${transactionTypeLabel.uppercase()} - ${transaction.TransactionID}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = primary500
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Amount Column
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
                        text = formattedAmount,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                }
            }
        }



        // Action Button - Show based on available action (REFUND or VOID)
        if (showAvailableAction && availableAction != TransactionAction.NONE) {
            val actionColor = when (availableAction) {
                TransactionAction.VOID -> TransactionColors.Yellow
                TransactionAction.REFUND -> refundColor
                else -> red500
            }
            val actionBackgroundColor = when (availableAction) {
                TransactionAction.VOID -> TransactionColors.Yellow.copy(alpha = 0.2f)
                TransactionAction.REFUND -> refundColor.copy(alpha = 0.1f)
                else -> red300.copy(alpha = 0.2f)
            }
            val actionLabel = when (availableAction) {
                TransactionAction.VOID -> "Void"
                TransactionAction.REFUND -> "Refund"
                else -> ""
            }
            val actionIcon = when (availableAction) {
                TransactionAction.VOID -> R.drawable.void_icon
                TransactionAction.REFUND -> R.drawable.refund_icon
                else -> R.drawable.refund
            }

            Column(
                modifier = Modifier
                    .background(actionBackgroundColor, shape = RoundedCornerShape(8.dp))
                    .weight(2f)
                    .padding(10.dp)
                    .clickable(onClick = {
                        showAvailableAction = false
                        when(availableAction){
                            TransactionAction.VOID -> {
                                navigateToVoidAction(transaction)
                            }
                            TransactionAction.REFUND -> {
                                navigateToRefundAction(transaction)
                            }
                            else -> {}
                        }
                    }),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter = painterResource(actionIcon),
                    tint = actionColor,
                    contentDescription = actionLabel,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = actionLabel,
                    color = actionColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}