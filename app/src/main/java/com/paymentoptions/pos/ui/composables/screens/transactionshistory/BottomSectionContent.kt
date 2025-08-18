package com.paymentoptions.pos.ui.composables.screens.transactionshistory

import MyDropdown
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.services.apiService.endpoints.transactionList
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.DateRangePickerModal
import com.paymentoptions.pos.ui.composables._components.MyCircularProgressIndicator
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.composables.screens.dashboard.Transactions
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.innerShadow
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.formatToPrecisionString
import com.paymentoptions.pos.utils.modifiers.conditional
import com.paymentoptions.pos.utils.modifiers.innerShadow
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Date
import java.util.Locale
import kotlin.math.ceil


@Composable
fun BottomSectionContent(navController: NavController, enableScrolling: Boolean = false) {
    val context = LocalContext.current
    var receivalAmount: Float by remember { mutableFloatStateOf(0.0f) }
    var currency by remember { mutableStateOf("") }
    var apiResponseAvailable by remember { mutableStateOf(false) }
    var transactions by remember { mutableStateOf<List<TransactionListDataRecord>>(listOf()) }
    val scrollState = rememberScrollState()

    var take: Int by remember { mutableIntStateOf(Int.MAX_VALUE) }
    var currentPage: Int by remember { mutableIntStateOf(1) }
    var maxPage: Int by remember { mutableIntStateOf(1) }

    var showInsights by remember { mutableStateOf(true) }
    var fromDateCustomFilter by remember { mutableStateOf<Long?>(null) }
    var toDateCustomFilter by remember { mutableStateOf<Long?>(null) }

    var receivalForText by remember { mutableStateOf("Receival for the day") }
    var receivalForTimePeriodText by remember { mutableStateOf("") }

    var filters = mapOf<String, String>(
        "Today" to "Today",
        "Week" to "Week",
        "Month" to "Month",
        "Custom" to "Custom Date Range",
    )

    var selectedFilter by remember { mutableStateOf<Map.Entry<String, String>>(filters.entries.first()) }

    if (selectedFilter.key == "Custom") {
        if (fromDateCustomFilter == null) DateRangePickerModal(
            title = "Start Date",
            { startDateMillis, endDateMillis ->
                if (startDateMillis == null || endDateMillis == null) {
                    fromDateCustomFilter = null
                    toDateCustomFilter = null
                    selectedFilter = filters.entries.first()
                } else {
                    fromDateCustomFilter = startDateMillis
                    toDateCustomFilter = endDateMillis
                }
            },
            { selectedFilter = filters.entries.first() })
    } else {
        fromDateCustomFilter = null
        toDateCustomFilter = null
    }

    fun updateReceivalAmount(newAmount: Float) {
        receivalAmount = newAmount
    }

    LaunchedEffect(currentPage) {
        apiResponseAvailable = false
        try {
            val skip = (currentPage - 1) * take
            val transactionListFromAPI = transactionList(context, take, skip)

            if (transactionListFromAPI != null) {
                maxPage =
                    ceil(transactionListFromAPI.data.total_count.toDouble() / take.toDouble()).toInt()

                transactions = transactions.plus(transactionListFromAPI.data.records)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error fetching next page from API", Toast.LENGTH_SHORT).show()

            if (e.toString().contains("HTTP 401")) navController.navigate(Screens.SignIn.route) {
                popUpTo(0) { inclusive = true }
            }
        } finally {
            apiResponseAvailable = true
        }
    }

    LaunchedEffect(selectedFilter, fromDateCustomFilter, toDateCustomFilter) {
        when (selectedFilter.key) {
            "Today" -> {
                receivalForText = "Receival for the day"
                receivalForTimePeriodText = SimpleDateFormat("dd MMMM, YYYY").format(Date())
                receivalAmount = 0.0f
            }

            "Week" -> {
                val today = OffsetDateTime.now().toLocalDateTime()
                val sevenDaysAgo = today.minusDays(7)

                receivalForText = "Receival for the week"
                receivalForTimePeriodText = "${sevenDaysAgo.dayOfMonth} ${
                    sevenDaysAgo.month.toString().lowercase()
                        .replaceFirstChar { it.titlecase(Locale.ROOT) }
                }, ${sevenDaysAgo.year} to ${today.dayOfMonth} ${
                    today.month.toString().lowercase()
                        .replaceFirstChar { it.titlecase(Locale.ROOT) }
                }, ${today.year}"
                receivalAmount = 0.0f
            }

            "Month" -> {
                val today = OffsetDateTime.now().toLocalDateTime()
                val thirtyDaysAgo = today.minusDays(30)

                receivalForText = "Receival for the month"
                receivalForTimePeriodText = run {
                    today.month.toString()
                    "${thirtyDaysAgo.dayOfMonth} ${
                        thirtyDaysAgo.month.toString().lowercase()
                            .replaceFirstChar { it.titlecase(Locale.ROOT) }
                    }, ${thirtyDaysAgo.year} to ${today.dayOfMonth} ${
                        today.month.toString().lowercase()
                            .replaceFirstChar { it.titlecase(Locale.ROOT) }
                    }, ${today.year}"
                }
                receivalAmount = 0.0f
            }

            "Custom" -> {
                receivalForText = "Receival for the period"
                receivalAmount = 0.0f


                if (fromDateCustomFilter != null && toDateCustomFilter != null) {
                    val simpleDateFormat = SimpleDateFormat("dd MMMM YYYY")

                    receivalForTimePeriodText =
                        "${simpleDateFormat.format(fromDateCustomFilter)} to ${
                            simpleDateFormat.format(toDateCustomFilter)
                        }"
                } else receivalForTimePeriodText = ""
            }
        }
    }

    if (!apiResponseAvailable) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            MyCircularProgressIndicator()
        }
    } else {
        currency = transactions.firstOrNull()?.CurrencyCode ?: ""

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Transaction History",
                modifier = Modifier
                    .align(alignment = Alignment.Start)
                    .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                style = AppTheme.typography.titleNormal
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                    .fillMaxWidth()
                    .height(46.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,

                ) {

                MyDropdown(
                    navController,
                    filters,
                    selectedFilter,
                    onFilterChange = { selectedFilter = it },
                    icon = Icons.Default.CalendarMonth,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(205.dp)
                )

                Row(
                    Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBackgroundColor)
                        .innerShadow(
                            color = innerShadow,
                            blur = 10.dp,
                            spread = 10.dp,
                            cornersRadius = 8.dp,
                            offsetX = 0.dp,
                            offsetY = 0.dp
                        )
                        .clickable(onClick = { showInsights = !showInsights })
                        .zIndex(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Show list",
                        modifier = Modifier
                            .padding(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (!showInsights) Color.White else Color.Transparent)
                            .padding(4.dp)
                            .zIndex(2f)
                    )
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Show bar graph",
                        modifier = Modifier
                            .padding(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (showInsights) Color.White else Color.Transparent)
                            .padding(4.dp)
                            .zIndex(2f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .padding(horizontal = if (showInsights) DEFAULT_BOTTOM_SECTION_PADDING_IN_DP else 0.dp)
                    .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = receivalForText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primary900,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = receivalForTimePeriodText,
                    style = AppTheme.typography.footnote,
                )

                Spacer(modifier = Modifier.height(4.dp))

                CurrencyText(currency = currency, amount = receivalAmount.formatToPrecisionString())

                Spacer(modifier = Modifier.height(16.dp))

                var filteredTransactions = transactions.filter {

                    var filterIn = when (selectedFilter.key) {
                        "Today" -> todayFilterFn(it)
                        "Week" -> last7DaysFilterFn(it)
                        "Month" -> last30DaysFilterFn(it)
                        "Custom" -> {
                            if (fromDateCustomFilter != null && toDateCustomFilter != null) customFilterFn(
                                it,
                                startDate = fromDateCustomFilter!!,
                                endDate = toDateCustomFilter!!
                            )
                            else false
                        }

                        else -> false
                    }
                    filterIn
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .conditional(enableScrolling) { verticalScroll(scrollState) }) {

                    if (showInsights) Insights(
                        transactions = filteredTransactions,
                        currency = currency,
                        updateReceivalAmount = {
                            updateReceivalAmount(it)
                        }) else Transactions(
                        navController, transactions = filteredTransactions, updateReceivalAmount = {
                            updateReceivalAmount(it)
                        })
                }

            }
        }
    }
}

fun todayFilterFn(transaction: TransactionListDataRecord): Boolean {
    val today = OffsetDateTime.now().toLocalDateTime()
    val transactionDate = OffsetDateTime.parse(transaction.Date).toLocalDateTime()

    return today.dayOfMonth == transactionDate.dayOfMonth && today.month == transactionDate.month && today.year == transactionDate.year
}

//fun thisMonthFilterFn(transaction: TransactionListDataRecord): Boolean {
//    val today = OffsetDateTime.now().toLocalDateTime()
//    val transactionDate = OffsetDateTime.parse(transaction.Date).toLocalDateTime()
//
//    return today.month == transactionDate.month && today.year == transactionDate.year
//}

fun dayFilterFn(transaction: TransactionListDataRecord, dayDifference: Long = 1): Boolean {
    val today = OffsetDateTime.now().toLocalDateTime()
    val transactionDate = OffsetDateTime.parse(transaction.Date).toLocalDateTime()

    return transactionDate > today.minusDays(dayDifference)
}

fun last7DaysFilterFn(transaction: TransactionListDataRecord): Boolean {
    return dayFilterFn(transaction, 7)
}

fun last30DaysFilterFn(transaction: TransactionListDataRecord): Boolean {
    return dayFilterFn(transaction, 30)
}

fun customFilterFn(
    transaction: TransactionListDataRecord,
    startDate: Long,
    endDate: Long,
): Boolean {
    val transactionDate = OffsetDateTime.parse(transaction.Date).toInstant().epochSecond * 1000
    return transactionDate >= startDate && transactionDate <= endDate
}