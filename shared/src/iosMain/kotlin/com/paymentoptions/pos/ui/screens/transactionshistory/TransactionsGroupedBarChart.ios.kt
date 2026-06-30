package com.paymentoptions.pos.ui.screens.transactionshistory

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.network.InsightsResponseDataRecord
import com.paymentoptions.pos.network.toTransactionListDataRecord
import com.paymentoptions.pos.ui.navigation.Screens
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.borderColor
import com.paymentoptions.pos.ui.theme.containerBackgroundGradientBrush
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.formatEpochMillis
import com.paymentoptions.pos.utils.formatToPrecisionString
import com.paymentoptions.pos.utils.safeParseDateTime
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.absoluteValue

private val earningsColor = Color(0xFF00C9A7)
private val refundsColor = Color(0xFFFF6B6B)

private enum class ChartDisplayMode { HOURLY, DAILY, WEEKLY, MONTHLY }

private fun generateHourlySlotLabel(startHour: Int): String {
    val endHour = (startHour + 2) % 24
    return "${startHour.toString().padStart(2, '0')}-${endHour.toString().padStart(2, '0')}"
}

private fun getChartDisplayMode(startDate: LocalDate, endDate: LocalDate): ChartDisplayMode {
    val days = startDate.daysUntil(endDate)
    return when {
        days == 0 -> ChartDisplayMode.HOURLY
        days <= 14 -> ChartDisplayMode.DAILY
        days <= 90 -> ChartDisplayMode.WEEKLY
        else -> ChartDisplayMode.MONTHLY
    }
}

@Composable
actual fun TransactionsGroupedBarChart(
    navController: NavController,
    transactions: List<InsightsResponseDataRecord>,
    currency: String,
    updateReceivalAmount: (Float) -> Unit,
    startDateMillis: Long,
    endDateMillis: Long,
) {
    val tz = TimeZone.currentSystemDefault()
    val startDate = Instant.fromEpochMilliseconds(startDateMillis).toLocalDateTime(tz).date
    val endDate = Instant.fromEpochMilliseconds(endDateMillis).toLocalDateTime(tz).date

    var earningTransactionCount = 0
    var earningAmount = 0.0f
    var refundTransactionCount = 0
    var refundAmount = 0.0f

    val displayMode = getChartDisplayMode(startDate, endDate)
    val daysBetween = startDate.daysUntil(endDate)

    val (numSlots, slotLabels) = when (displayMode) {
        ChartDisplayMode.HOURLY -> {
            var minHour = 24; var maxHour = 0
            transactions.forEach { t ->
                if (t.status == "SUCCESSFUL") {
                    val h = safeParseDateTime(t.TransactionDate ?: "").toLocalDateTime(tz).hour
                    if (h < minHour) minHour = h
                    if (h > maxHour) maxHour = h
                }
            }
            if (minHour > maxHour) { minHour = 6; maxHour = 22 }
            val startSlot = (minHour / 2) * 2
            val endSlot = ((maxHour / 2) + 1) * 2
            val slots = maxOf((endSlot - startSlot) / 2, 6)
            val labels = Array(slots) { i -> generateHourlySlotLabel((startSlot + i * 2) % 24) }
            Pair(slots, labels)
        }
        ChartDisplayMode.DAILY -> {
            val days = daysBetween + 1
            val labels = Array(days) { i ->
                val d = startDate.plus(i, DateTimeUnit.DAY)
                formatEpochMillis(
                    Instant.fromEpochMilliseconds(0).plus(d.toEpochDays().toLong(), DateTimeUnit.DAY, tz).toEpochMilliseconds(),
                    "dd MMM",
                )
            }
            Pair(days, labels)
        }
        ChartDisplayMode.WEEKLY -> {
            val weeks = (daysBetween / 7) + 1
            val labels = Array(weeks) { i ->
                val d = startDate.plus(i * 7, DateTimeUnit.DAY)
                formatEpochMillis(
                    Instant.fromEpochMilliseconds(0).plus(d.toEpochDays().toLong(), DateTimeUnit.DAY, tz).toEpochMilliseconds(),
                    "dd MMM",
                )
            }
            Pair(weeks, labels)
        }
        ChartDisplayMode.MONTHLY -> {
            val months = startDate.monthsUntil(endDate) + 1
            val labels = Array(months) { i ->
                val d = startDate.plus(i, DateTimeUnit.MONTH)
                formatEpochMillis(
                    Instant.fromEpochMilliseconds(0).plus(d.toEpochDays().toLong(), DateTimeUnit.DAY, tz).toEpochMilliseconds(),
                    "MMM yyyy",
                )
            }
            Pair(months, labels)
        }
    }

    val earningsBySlot = FloatArray(numSlots) { 0f }
    val refundsBySlot = FloatArray(numSlots) { 0f }
    val transactionsBySlot = mutableMapOf<Int, MutableList<InsightsResponseDataRecord>>()
    repeat(numSlots) { transactionsBySlot[it] = mutableListOf() }

    transactions.forEach { transaction ->
        if (transaction.status == "SUCCESSFUL") {
            val txnDateTime = safeParseDateTime(transaction.TransactionDate ?: "").toLocalDateTime(tz)
            val txnDate = txnDateTime.date
            val txnHour = txnDateTime.hour

            val slotIndex = when (displayMode) {
                ChartDisplayMode.HOURLY -> {
                    var minHour = 24
                    transactions.forEach { t ->
                        if (t.status == "SUCCESSFUL") {
                            val h = safeParseDateTime(t.TransactionDate ?: "").toLocalDateTime(tz).hour
                            if (h < minHour) minHour = h
                        }
                    }
                    if (minHour > 23) minHour = 6
                    ((txnHour / 2) * 2 - (minHour / 2) * 2) / 2
                }
                ChartDisplayMode.DAILY -> startDate.daysUntil(txnDate)
                ChartDisplayMode.WEEKLY -> startDate.daysUntil(txnDate) / 7
                ChartDisplayMode.MONTHLY -> startDate.monthsUntil(txnDate)
            }

            if (slotIndex in 0 until numSlots) {
                transactionsBySlot[slotIndex]?.add(transaction)
                when (transaction.TransactionType) {
                    "PURCHASE" -> {
                        earningTransactionCount++
                        earningAmount += transaction.amount
                        earningsBySlot[slotIndex] += transaction.amount
                    }
                    "REFUND" -> {
                        refundTransactionCount++
                        refundAmount += transaction.amount
                        refundsBySlot[slotIndex] += transaction.amount
                    }
                }
            }
        }
    }

    updateReceivalAmount(earningAmount)

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {

        // Canvas bar chart
        val maxValue = (earningsBySlot.toList() + refundsBySlot.toList()).maxOrNull()
            ?.takeIf { it > 0f } ?: 1f

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Color.White),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(transactionsBySlot) {
                        detectTapGestures { tapOffset ->
                            val groupWidth = size.width.toFloat() / numSlots
                            val tappedSlot = (tapOffset.x / groupWidth).toInt().coerceIn(0, numSlots - 1)
                            val slotTxns = transactionsBySlot[tappedSlot]
                            if (!slotTxns.isNullOrEmpty()) {
                                val halfGroup = groupWidth / 2
                                val groupCenter = tappedSlot * groupWidth + halfGroup
                                val isEarnings = tapOffset.x < groupCenter
                                val filtered = if (isEarnings)
                                    slotTxns.filter { it.TransactionType == "PURCHASE" }
                                else
                                    slotTxns.filter { it.TransactionType == "REFUND" }
                                val txn = (filtered.firstOrNull() ?: slotTxns.first())
                                val json = Json.encodeToString(txn.toTransactionListDataRecord())
                                navController.navigate(Screens.TransactionDetails.createRoute(json))
                            }
                        }
                    },
            ) {
                val chartHeight = size.height
                val chartWidth = size.width
                val groupWidth = chartWidth / numSlots
                val barWidth = groupWidth * 0.25f
                val barSpacing = barWidth * 0.5f

                // Grid lines
                val gridCount = 4
                repeat(gridCount + 1) { i ->
                    val y = chartHeight * (1 - i.toFloat() / gridCount)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1.dp.toPx(),
                    )
                }

                for (i in 0 until numSlots) {
                    val cx = (i + 0.5f) * groupWidth

                    val earningH = (earningsBySlot[i] / maxValue) * chartHeight
                    if (earningH > 0) {
                        drawRect(
                            color = earningsColor,
                            topLeft = Offset(cx - barWidth - barSpacing / 2, chartHeight - earningH),
                            size = Size(barWidth, earningH),
                        )
                    }

                    val refundH = (refundsBySlot[i] / maxValue) * chartHeight
                    if (refundH > 0) {
                        drawRect(
                            color = refundsColor,
                            topLeft = Offset(cx + barSpacing / 2, chartHeight - refundH),
                            size = Size(barWidth, refundH),
                        )
                    }
                }
            }

            // X-axis labels
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                val step = maxOf(numSlots / 6, 1)
                for (i in 0 until numSlots) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (i % step == 0) {
                            Text(
                                text = slotLabels.getOrElse(i) { "" },
                                fontSize = 9.sp,
                                color = Color.Gray,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.size(10.dp).clip(CircleShape).background(earningsColor))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Earnings", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.size(10.dp).clip(CircleShape).background(refundsColor))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Refunds", fontSize = 12.sp, color = Color.Gray)
            }
        }

        // Stats
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(20.dp))
                .background(brush = containerBackgroundGradientBrush, shape = RoundedCornerShape(20.dp))
                .padding(20.dp)
                .shadow(elevation = 120.dp, shape = RoundedCornerShape(4.dp), ambientColor = Color.LightGray, spotColor = primary100),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Transactions # $earningTransactionCount", style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal))
                    Text(currency, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = earningsColor.copy(alpha = 0.5f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Earnings", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500)
                    Text("+ ${earningAmount.absoluteValue.formatToPrecisionString()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = earningsColor)
                }
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Transactions # $refundTransactionCount", style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal))
                    Text(currency, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = refundsColor.copy(alpha = 0.5f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Refunds", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500)
                    Text("- ${refundAmount.absoluteValue.formatToPrecisionString()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = refundsColor)
                }
            }

            HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color.LightGray.copy(alpha = 0.2f))

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("All Transactions # ${earningTransactionCount + refundTransactionCount}", style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal))
                    Text(currency, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primary900.copy(alpha = 0.5f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val net = earningAmount - refundAmount
                    Text("Net Earnings", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primary500)
                    Text(
                        "${if (net > 0) "+" else if (net < 0) "-" else ""} ${net.absoluteValue.formatToPrecisionString()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary900,
                    )
                }
            }
        }
    }
}
