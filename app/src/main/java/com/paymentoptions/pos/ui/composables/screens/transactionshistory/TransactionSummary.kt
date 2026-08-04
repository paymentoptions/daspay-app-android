// This file is moved to a common TransactionSummary class, keeping it for reference if needed
//package com.paymentoptions.pos.ui.composables.screens.transactionshistory
//
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.combinedClickable
//import androidx.compose.foundation.gestures.detectHorizontalDragGestures
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.SpanStyle
//import androidx.compose.ui.text.buildAnnotatedString
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.withStyle
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.google.gson.Gson
//import com.paymentoptions.pos.R
//import com.paymentoptions.pos.services.apiService.InsightsResponseDataRecord
//import com.paymentoptions.pos.services.apiService.toTransactionListDataRecord
//import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
//import com.paymentoptions.pos.ui.composables.navigation.Screens
//import com.paymentoptions.pos.ui.theme.iconBackgroundColor
//import com.paymentoptions.pos.ui.theme.primary500
//import com.paymentoptions.pos.ui.theme.purple50
//import com.paymentoptions.pos.ui.theme.red300
//import com.paymentoptions.pos.ui.theme.red500
//import com.paymentoptions.pos.utils.formatToPrecisionString
//import com.paymentoptions.pos.utils.timeAgo
//import java.text.SimpleDateFormat
//import java.time.OffsetDateTime
//import java.util.Date
//import com.paymentoptions.pos.ClientHeadlessImpl
//import com.theminesec.lib.dto.common.Amount
//import com.theminesec.lib.dto.poi.PoiRequest
//import com.theminesec.lib.dto.transaction.TranType
//import com.theminesec.sdk.headless.HeadlessActivity
//import com.theminesec.sdk.headless.model.WrappedResult
//import androidx.compose.runtime.remember
//import com.paymentoptions.pos.logger.AppLogger
//import java.math.BigDecimal
//import java.util.Currency
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import android.content.Context
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalHapticFeedback
//import com.paymentoptions.pos.services.apiService.endpoints.refund
//import com.paymentoptions.pos.services.apiService.endpoints.void
//import com.paymentoptions.pos.utils.getAmountSign
//import com.paymentoptions.pos.utils.getAvailableAction
//import com.paymentoptions.pos.utils.getStatusColor
//import com.paymentoptions.pos.utils.getTransactionIcon
//import com.paymentoptions.pos.utils.getTransactionTypeLabel
//
//@Composable
//fun TransactionSummary(
//    navController: NavController,
//    transaction: InsightsResponseDataRecord,
//    longClickedTransactionId: String = "",
//    onLongClick: (String) -> Unit = {},
//    onSwipeLeft: (String) -> Unit = {},
//    onSwipeRight: (String) -> Unit = {},
//) {
//    val transactionAmount = transaction.amount.toFloat()
//    val context = LocalContext.current
//    val isTransactionAmountPositive = transactionAmount > 0
//    val isCardTransaction = transaction.paymentMethod == "CARDPAYMENT"
//    val dateString = transaction.TransactionDate   //"2025-04-23T03:38:57.349+00:00"
//    val dateTime = OffsetDateTime.parse(dateString)
//    val date: Date = Date.from(dateTime.toInstant())
//    val dateStringFormatted = SimpleDateFormat("dd MMMM, YYYY").format(date)
//    val timeAgoString = dateTime.toInstant().toEpochMilli().timeAgo()
//    var isLongClicked by remember { mutableStateOf(false) } //longClickedTransactionId == transaction.TransactionID.toString()
//
//    val hourPart = SimpleDateFormat("hh:mm:ss a").format(date)
//    val borderRadius = 20.dp
//    val haptics = LocalHapticFeedback.current
//    var offsetX by remember { mutableStateOf(0f) }
//
//    // Use helper functions for dynamic styling
//    val transactionListDataRecord = transaction.toTransactionListDataRecord()
//    val statusColor = getStatusColor(transactionListDataRecord)
//    val amountSign = getAmountSign(transactionListDataRecord)
//    val transactionIcon = getTransactionIcon(transactionListDataRecord)
//    val availableAction = getAvailableAction(transactionListDataRecord)
//    val transactionTypeLabel = getTransactionTypeLabel(transactionListDataRecord)
//
//    AppLogger.debug("TransactionSummary availableAction: $availableAction, amountSignIn :"
//            + "$amountSign , transactionTypeLabel: $transactionTypeLabel, statusColor: $statusColor, "
//            + "transactionIcon: $transactionIcon")
//
//
//    val dateStr = buildAnnotatedString {
//        withStyle(
//            SpanStyle(
//                purple50, fontWeight = FontWeight.Medium, fontSize = 12.sp
//            )
//        ) { append(dateStringFormatted) }
//
//        append("  ")
//
//        withStyle(
//            SpanStyle(
//                color = purple50, fontSize = 12.sp, fontWeight = FontWeight.Medium,
//            )
//        ) { append(if (timeAgoString.endsWith("seconds ago")) timeAgoString else hourPart) }
//    }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = if (isLongClicked) 0.dp else DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
//        horizontalArrangement = Arrangement.Absolute.spacedBy(10.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//
//        Card(
//            colors = CardDefaults.cardColors(containerColor = Color.White),
//            border = BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.1f)),
//            shape = RoundedCornerShape(
//                topStart = if (isLongClicked) 0.dp else borderRadius,
//                topEnd = borderRadius,
//                bottomStart = if (isLongClicked) 0.dp else borderRadius,
//                bottomEnd = borderRadius
//            ),
//            modifier = Modifier
//                .shadow(
//                    elevation = 16.dp,
//                    shape = RoundedCornerShape(borderRadius),
//                    ambientColor = Color(0xFF64B5F6).copy(alpha = 0.8f),
//                    spotColor = Color(0xFF2196F3).copy(alpha = 0.6f)
//                )
//                .pointerInput(Unit) {
//                    detectHorizontalDragGestures(onDragEnd = {
//                        if (offsetX > 200f) {
//                            // Swiped
//                            onSwipeRight(transaction.uuid.toString())
//                        } else if (offsetX < -200f) {
//                            // Swiped Left
//                            onSwipeLeft(transaction.uuid.toString())
//                        }
//                        offsetX = 0f // reset position
//                    }, onHorizontalDrag = { _, dragAmount ->
//                        offsetX += dragAmount
//                    })
//                }
//                .combinedClickable(onClick = {
//                    val transactionJson = Gson().toJson(transactionListDataRecord)
//                    navController.navigate(Screens.TransactionDetails.createRoute(transactionJson))
//                }, onLongClick = {
////                    onLongClick(transaction.uuid.toString())
//                })
//                .weight(if (isLongClicked) 8f else 1f)
//        ) {
//            Row(
//                modifier = Modifier.padding(12.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                if (!isLongClicked) Box(
//                    modifier = Modifier
//                        .size(44.dp)
//                        .clip(RoundedCornerShape(8.dp))
//                        .background(iconBackgroundColor), contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        painter = painterResource(if (isCardTransaction) R.drawable.icon_card else R.drawable.icon_money),
//                        contentDescription = "Icon",
//                        tint = statusColor
//                    )
//                }
//
//                Spacer(modifier = Modifier.width(10.dp))
//
//                Column(
//                    modifier = Modifier.weight(8f), verticalArrangement = Arrangement.spacedBy(4.dp)
//                ) {
//                    Text(
//                        dateStr, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = purple50
//                    )
//                    Text(
//                        text = "Txn ID - ${transaction.ID}",
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 14.sp,
//                        color = primary500
//                    )
//                }
//
//                Spacer(modifier = Modifier.width(4.dp))
//
//                Column(
//                    horizontalAlignment = Alignment.End,
//                    verticalArrangement = Arrangement.spacedBy(4.dp),
//                    modifier = Modifier.weight(3f)
//                ) {
//                    Text(
//                        transaction.CurrencyCode,
//                        textAlign = TextAlign.End,
//                        color = statusColor,
//                        fontSize = 12.sp,
//                        fontWeight = FontWeight.Medium,
//                    )
//
//                    Text(
//                        text = if (isTransactionAmountPositive) "+${transaction.amount.formatToPrecisionString()}" else transaction.amount.formatToPrecisionString(),
//                        color = statusColor,
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 16.sp,
//                        maxLines = 1
//                    )
//                }
//            }
//        }
//
//        val launcher = rememberLauncherForActivityResult(
//            HeadlessActivity.contract(ClientHeadlessImpl::class.java)
//        ) {
//            when (it) {
//                is WrappedResult.Success -> {
//                    AppLogger.debug("inThis Launche response: ${it.toString()}")
//                    CoroutineScope(Dispatchers.IO).launch {
//                        try {
//                            // Call refund or VOID API based on user action
//                            if(it.value.tranType == TranType.REFUND){
//                                val response = refund(
//                                    context = context,
//                                    transactionId = transaction.uuid,
//                                    merchantId = transaction.DASMID ?: "",
//                                    transaction = it.value
//                                )
//                                if (response != null) {
//                                    AppLogger.debug("Refund successful: $response")
//                                } else {
//                                    AppLogger.error("Refund failed - null response")
//                                }
//                            } else {
//                                val response = void(
//                                    context = context,
//                                    transactionId = transaction.uuid,
//                                    merchantId = transaction.DASMID ?: "",
//                                    transaction = it.value
//                                )
//                                if (response != null) {
//                                    AppLogger.debug("Void successful: $response")
//                                } else {
//                                    AppLogger.error("Void failed - null response")
//                                }
//                            }
//                        } catch (e: Exception) {
//                            AppLogger.error("Error processing transaction: ${e.message}", e)
//                        }
//                    }
//                }
//
//                is WrappedResult.Failure -> {
//                    AppLogger.error("inThis Launcher failure ---->: $it")
//                }
//            }
//        }
//
//
//
//        fun doVoid(transaction: InsightsResponseDataRecord){
//            AppLogger.debug("full transaction object: $transaction")
//            launcher.launch(input = PoiRequest.ActionVoid(transaction.AcquirerTransactionID!!))
//        }
//
//
////        {"tranId":"tran_01KGKT044SGRKQM9SS9K6146TD","tranType":"SALE","tranStatus":"APPROVED","amount":{"v
////            alue":"9.00","currency":"HKD"},
//
//        fun doRefund(transaction: InsightsResponseDataRecord){
//            AppLogger.debug("full transaction object: $transaction")
//            launcher.launch(input = PoiRequest.ActionLinkedRefund(
//                transaction.AcquirerTransactionID!!,
//                Amount(BigDecimal(transaction.amount.toString()), Currency.getInstance(transaction.CurrencyCode))
//            ))
//        }
//
//
//
//        if (isLongClicked) Column(
//            modifier = Modifier
//                .padding(end = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
//                .background(
//                    red300.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)
//                )
//                .padding(6.dp)
//                .weight(2f)
//                .clickable(onClick = {
//                    doVoid(transaction)
//                })
//            ,
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(4.dp)
//        ) {
//            Icon(
//                painter = painterResource(R.drawable.refund),
//                tint = red500,
//                contentDescription = "Refund",
//                modifier = Modifier.size(20.dp)
//            )
//            Text(
//                text = "Refund", color = red500, fontSize = 11.sp, fontWeight = FontWeight.Medium
//            )
//        }
//    }
//}