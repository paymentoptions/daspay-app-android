package com.paymentoptions.pos.ui.composables.screens.transactionshistory

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.paymentoptions.pos.services.apiService.InsightsResponseDataRecord
import com.paymentoptions.pos.network.toTransactionListDataRecord
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.borderColor
import com.paymentoptions.pos.ui.theme.containerBackgroundGradientBrush
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.formatToPrecisionString
import com.paymentoptions.pos.utils.safeParseOffsetDateTime
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.absoluteValue

// Chart colors matching the design
private val earningsColor = Color(0xFF00C9A7) // Teal/Cyan color
private val refundsColor = Color(0xFFFF6B6B) // Coral red color

/**
 * Enum to represent different chart display modes based on date range
 */
enum class ChartDisplayMode {
    HOURLY,     // Same day - show 2-hour slots
    DAILY,      // ≤ 14 days - show each day
    WEEKLY,     // ≤ 3 months (90 days) - show weeks
    MONTHLY     // > 3 months - show months
}

/**
 * Generates time slot label for hourly mode (2-hour intervals)
 */
private fun generateHourlySlotLabel(startHour: Int): String {
    val endHour = (startHour + 2) % 24
    return String.format(Locale.US, "%02d-%02d", startHour, endHour)
}

/**
 * Generates day label (e.g., "Mon", "Tue", or "15 Jan")
 */
private fun generateDayLabel(date: LocalDate, showDayName: Boolean = true): String {
    return if (showDayName) {
        date.format(DateTimeFormatter.ofPattern("EEE", Locale.US))
    } else {
        date.format(DateTimeFormatter.ofPattern("dd MMM", Locale.US))
    }
}

/**
 * Determines the chart display mode based on date range
 * ≤ 1 day = Hourly (2-hour slots)
 * ≤ 14 days = Daily
 * ≤ 90 days = Weekly
 * > 90 days = Monthly
 */
private fun getChartDisplayMode(startDate: OffsetDateTime, endDate: OffsetDateTime): ChartDisplayMode {
    val daysBetween = ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate())

    return when {
        daysBetween == 0L -> ChartDisplayMode.HOURLY
        daysBetween <= 14L -> ChartDisplayMode.DAILY
        daysBetween <= 90L -> ChartDisplayMode.WEEKLY
        else -> ChartDisplayMode.MONTHLY
    }
}

@Composable
fun TransactionsGroupedBarChart(
    navController: NavController,
    transactions: List<InsightsResponseDataRecord>,
    currency: String,
    updateReceivalAmount: (Float) -> Unit,
    startDate: OffsetDateTime,
    endDate: OffsetDateTime,
) {
    var earningTransactionCount = 0
    var earningAmount = 0.0f
    var refundTransactionCount = 0
    var refundAmount = 0.0f

    // Determine display mode based on date range
    val displayMode = getChartDisplayMode(startDate, endDate)
    val daysBetween = ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate())

    // Calculate number of slots and generate labels based on display mode
    val (numSlots, slotLabels) = when (displayMode) {
        ChartDisplayMode.HOURLY -> {
            // Find the time range from transactions for hourly view
            var minHour = 24
            var maxHour = 0

            transactions.forEach { transaction ->
                if (transaction.status == "SUCCESSFUL") {
                    val date = safeParseOffsetDateTime(transaction.TransactionDate?:"").toLocalDateTime()
                    val hour = date.hour
                    if (hour < minHour) minHour = hour
                    if (hour > maxHour) maxHour = hour
                }
            }

            // Default to business hours if no transactions
            if (minHour > maxHour) {
                minHour = 6
                maxHour = 22
            }

            val startSlotHour = (minHour / 2) * 2
            val endSlotHour = ((maxHour / 2) + 1) * 2
            val slots = maxOf((endSlotHour - startSlotHour) / 2, 6)

            val labels = Array(slots) { index ->
                val slotStartHour = (startSlotHour + (index * 2)) % 24
                generateHourlySlotLabel(slotStartHour)
            }

            Pair(slots, labels)
        }

        ChartDisplayMode.DAILY -> {
            // Show each day in the range
            val days = (daysBetween + 1).toInt()
            val labels = Array(days) { index ->
                val date = startDate.toLocalDate().plusDays(index.toLong())
                date.format(DateTimeFormatter.ofPattern("dd MMM", Locale.US))
            }
            Pair(days, labels)
        }

        ChartDisplayMode.WEEKLY -> {
            // Group by weeks - start from the first Monday on or before startDate
            val startOfWeek = startDate.toLocalDate().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            val endOfWeek = endDate.toLocalDate().with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))
            val weeksBetween = ChronoUnit.WEEKS.between(startOfWeek, endOfWeek).toInt() + 1

            val labels = Array(weeksBetween) { index ->
                val weekStart = startOfWeek.plusWeeks(index.toLong())
                "${weekStart.format(DateTimeFormatter.ofPattern("dd MMM", Locale.US))}"
            }
            Pair(weeksBetween, labels)
        }

        ChartDisplayMode.MONTHLY -> {
            // Group by months
            val startMonth = startDate.toLocalDate().withDayOfMonth(1)
            val endMonth = endDate.toLocalDate().withDayOfMonth(1)
            val monthsBetween = ChronoUnit.MONTHS.between(startMonth, endMonth).toInt() + 1

            val labels = Array(monthsBetween) { index ->
                val month = startMonth.plusMonths(index.toLong())
                month.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.US))
            }
            Pair(monthsBetween, labels)
        }
    }

    // Initialize earnings and refunds for each slot
    val earningsBySlot = FloatArray(numSlots) { 0f }
    val refundsBySlot = FloatArray(numSlots) { 0f }

    // Map to store transactions by slot index for click handling
    val transactionsBySlot = mutableMapOf<Int, MutableList<InsightsResponseDataRecord>>()
    for (i in 0 until numSlots) {
        transactionsBySlot[i] = mutableListOf()
    }

    // Process transactions based on display mode
    transactions.forEach { transaction ->
        if (transaction.status == "SUCCESSFUL") {
            val txnDateTime = safeParseOffsetDateTime(transaction.TransactionDate?:"")
            val txnDate = txnDateTime.toLocalDate()
            val txnHour = txnDateTime.hour

            val slotIndex = when (displayMode) {
                ChartDisplayMode.HOURLY -> {
                    // Find min hour again for slot calculation
                    var minHour = 24
                    transactions.forEach { t ->
                        if (t.status == "SUCCESSFUL") {
                            val h = safeParseOffsetDateTime(t.TransactionDate?:"").hour
                            if (h < minHour) minHour = h
                        }
                    }
                    if (minHour > 23) minHour = 6
                    val startSlotHour = (minHour / 2) * 2
                    val slotHour = (txnHour / 2) * 2
                    (slotHour - startSlotHour) / 2
                }

                ChartDisplayMode.DAILY -> {
                    // Each day gets its own slot
                    ChronoUnit.DAYS.between(startDate.toLocalDate(), txnDate).toInt()
                }

                ChartDisplayMode.WEEKLY -> {
                    // Group by week (Monday to Sunday)
                    val startOfWeek = startDate.toLocalDate().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                    val txnWeekStart = txnDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                    ChronoUnit.WEEKS.between(startOfWeek, txnWeekStart).toInt()
                }

                ChartDisplayMode.MONTHLY -> {
                    // Group by month
                    val startMonth = startDate.toLocalDate().withDayOfMonth(1)
                    val txnMonth = txnDate.withDayOfMonth(1)
                    ChronoUnit.MONTHS.between(startMonth, txnMonth).toInt()
                }
            }

            // Ensure index is within bounds
            if (slotIndex in 0 until numSlots) {
                // Add transaction to the slot's list
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

        // Grouped Bar Chart using MPAndroidChart
        AndroidView(
            modifier = Modifier
                .height(280.dp)
                .fillMaxWidth()
                .background(Color.White),
            factory = { context ->
                BarChart(context).apply {
                    description.isEnabled = false
                    setDrawGridBackground(false)
                    setDrawBarShadow(false)
                    setDrawValueAboveBar(false)
                    setPinchZoom(false)
                    setScaleEnabled(false)
                    isDoubleTapToZoomEnabled = false
                    legend.isEnabled = false
                    setExtraOffsets(16f, 16f, 16f, 16f)
                    setFitBars(true)

                    // Add click listener for bar selection
                    setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                        override fun onValueSelected(e: Entry?, h: Highlight?) {
                            if (e != null && h != null) {
                                val slotIndex = e.x.toInt()
                                val dataSetIndex = h.dataSetIndex // 0 = earnings, 1 = refunds

                                // Get transactions for this slot
                                val slotTransactions = transactionsBySlot[slotIndex]
                                if (!slotTransactions.isNullOrEmpty()) {
                                    // Filter by transaction type based on which bar was clicked
                                    val filteredTransactions = if (dataSetIndex == 0) {
                                        // Earnings bar clicked - get PURCHASE transactions
                                        slotTransactions.filter { it.TransactionType == "PURCHASE" }
                                    } else {
                                        // Refunds bar clicked - get REFUND transactions
                                        slotTransactions.filter { it.TransactionType == "REFUND" }
                                    }

                                    // Navigate to the first transaction details
                                    val transaction = filteredTransactions.firstOrNull() ?: slotTransactions.first()
                                    val transactionListDataRecord = transaction.toTransactionListDataRecord()
                                    val transactionJson = Json.encodeToString(transactionListDataRecord)
                                    navController.navigate(Screens.TransactionDetails.createRoute(transactionJson))
                                }
                            }
                        }

                        override fun onNothingSelected() {
                            // Do nothing
                        }
                    })

                    // X-Axis configuration
                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawGridLines(false)
                        setDrawAxisLine(false)
                        granularity = 1f
                        textColor = AndroidColor.GRAY
                        textSize = 10f
                        labelRotationAngle = 0f
                        setCenterAxisLabels(true)
                        yOffset = 10f
                        valueFormatter = IndexAxisValueFormatter(slotLabels)
                    }

                    // Left Y-Axis configuration
                    axisLeft.apply {
                        setDrawGridLines(true)
                        gridColor = AndroidColor.parseColor("#E8E8E8")
                        setDrawAxisLine(false)
                        setDrawLabels(false)
                        axisMinimum = 0f
                    }

                    // Disable right Y-Axis
                    axisRight.isEnabled = false
                }
            },
            update = { chart ->
                // Create bar entries for earnings and refunds
                val earningsEntries = mutableListOf<BarEntry>()
                val refundsEntries = mutableListOf<BarEntry>()

                for (i in 0 until numSlots) {
                    earningsEntries.add(BarEntry(i.toFloat(), earningsBySlot[i]))
                    refundsEntries.add(BarEntry(i.toFloat(), refundsBySlot[i]))
                }

                // Create datasets with design colors
                val earningsDataSet = BarDataSet(earningsEntries, "Earnings").apply {
                    color = earningsColor.toArgb()
                    setDrawValues(false)
                }

                val refundsDataSet = BarDataSet(refundsEntries, "Refunds").apply {
                    color = refundsColor.toArgb()
                    setDrawValues(false)
                }

                // Create BarData with grouped bars
                val barData = BarData(earningsDataSet, refundsDataSet).apply {
                    barWidth = 0.25f
                }

                chart.data = barData

                // Configure grouping - adjust for better spacing
                val groupSpace = 0.3f
                val barSpace = 0.1f
                val barWidth = 0.25f

                chart.barData.barWidth = barWidth
                chart.xAxis.axisMinimum = 0f
                chart.xAxis.axisMaximum = 0f + chart.barData.getGroupWidth(groupSpace, barSpace) * numSlots
                chart.xAxis.valueFormatter = IndexAxisValueFormatter(slotLabels)
                chart.groupBars(0f, groupSpace, barSpace)

                chart.invalidate()
            }
        )

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Earnings legend
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(earningsColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Earnings",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Refunds legend
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(refundsColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Refunds",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        //Stats
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(20.dp))
                .background(
                    brush = containerBackgroundGradientBrush, shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
                .shadow(
                    elevation = 120.dp,
                    shape = RoundedCornerShape(4.dp),
                    ambientColor = Color.LightGray,
                    spotColor = primary100
                ), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            //Earnings
            Column(
                modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Transactions # $earningTransactionCount",
                        style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal)
                    )

                    Text(
                        currency,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = earningsColor.copy(alpha = 0.5f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Earnings",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500
                    )

                    Text(
                        "+ ${earningAmount.absoluteValue.formatToPrecisionString()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = earningsColor
                    )
                }
            }

            //Refunds
            Column(
                modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Transactions # $refundTransactionCount",
                        style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal)
                    )

                    Text(
                        currency,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = refundsColor.copy(alpha = 0.5f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Refunds",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500
                    )

                    Text(
                        "- ${refundAmount.absoluteValue.formatToPrecisionString()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = refundsColor
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(), color = Color.LightGray.copy(alpha = 0.2f)
            )

            //All Earnings
            Column(
                modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        "All Transactions # ${earningTransactionCount + refundTransactionCount}",
                        style = AppTheme.typography.footnote.copy(fontWeight = FontWeight.Normal)
                    )

                    Text(
                        currency,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary900.copy(alpha = 0.5f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    val netEarningAmount = earningAmount - refundAmount
                    Text(
                        "Net Earnings",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500
                    )

                    Text(
                        "${if (netEarningAmount > 0) "+" else if (netEarningAmount < 0) "-" else ""} ${netEarningAmount.absoluteValue.formatToPrecisionString()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary900
                    )
                }
            }

        }
    }
}