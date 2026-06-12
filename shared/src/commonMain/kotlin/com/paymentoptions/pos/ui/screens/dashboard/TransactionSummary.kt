package com.paymentoptions.pos.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.TransactionListDataRecord
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.navigation.Screens
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
import com.paymentoptions.pos.utils.getTransactionTypeLabel
import com.paymentoptions.pos.utils.timeAgo
import com.paymentoptions.pos.showToast
import com.paymentoptions.pos.utils.formatEpochMillis
import com.paymentoptions.pos.utils.parseIsoDateToMillis
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.refund_icon
import paymentoptionspos.shared.generated.resources.void_icon
import paymentoptionspos.shared.generated.resources.refund


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSummary(
    navController: NavController,
    transaction: TransactionListDataRecord,
) {
    val dateString = transaction.Date
    val millis = parseIsoDateToMillis(dateString)
    val dateStringFormatted = formatEpochMillis(millis, "dd MMMM, YYYY")
    val timeAgoString = millis.timeAgo()
    var showAvailableAction by remember { mutableStateOf(false) }

    val hourPart = formatEpochMillis(millis, "hh:mm:ss a")
    val borderRadius = 20.dp
    val haptics = LocalHapticFeedback.current
    var offsetX by remember { mutableStateOf(0f) }

    val statusColor = getStatusColor(transaction)
    val amountSign = getAmountSign(transaction)
    val transactionIcon = getTransactionIcon(transaction)
    val availableAction = getAvailableAction(transaction)
    val transactionTypeLabel = getTransactionTypeLabel(transaction)

    // Format the amount with sign - simple precision formatting
    val amountValue = transaction.amount.toFloat()
    val formattedAmount = if(amountValue == 0f){
        "0.00"
    } else {
        // Manual formatting for 2 decimal places to avoid java.lang.String.format
        val absoluteAmount = if (amountValue < 0) -amountValue else amountValue
        val integralPart = absoluteAmount.toInt()
        val fractionalPart = ((absoluteAmount - integralPart) * 100).toInt()
        val fractionalStr = if (fractionalPart < 10) "0$fractionalPart" else fractionalPart.toString()
        val baseFormatted = "$integralPart.$fractionalStr"
        
        when (amountSign) {
            "+" -> "+$baseFormatted"
            "-" -> "-$baseFormatted"
            else -> baseFormatted
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
        val transactionJson = Json.encodeToString(transaction)
        navController.navigate(Screens.TransactionAction.createRoute(transactionJson, "VOID"))
    }

    fun navigateToRefundAction(transaction: TransactionListDataRecord){
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
            shape = RoundedCornerShape(borderRadius),
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
                                showAvailableAction = false
                                offsetX = 0f
                            } else if (offsetX < -200f) {
                                if (availableAction != TransactionAction.NONE) {
                                    showAvailableAction = true
                                } else {
                                    showToast("No action available for this transaction")
                                }
                            }
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (availableAction != TransactionAction.NONE) {
                                offsetX += dragAmount
                            } else if (dragAmount < 0) {
                                offsetX += dragAmount
                            }
                        }
                    )
                }
                .clickable {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) // Changed from ToggleOn which is not always available in KMP
                    val transactionJson = Json.encodeToString(transaction)
                    navController.navigate(Screens.TransactionDetails.createRoute(transactionJson))
                }
                .weight(if (showAvailableAction) 8.5f else 1f)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
                    Text(
                        dateStr, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = purple50
                    )

                    Text(
                        text = "${transactionTypeLabel.uppercase()} - ${transaction.TransactionID}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = primary500
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

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
                TransactionAction.VOID -> Res.drawable.void_icon
                TransactionAction.REFUND -> Res.drawable.refund_icon
                else -> Res.drawable.refund
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
