package com.paymentoptions.pos.ui.composables.screens.transactionshistory

import MyDropdown
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
import com.paymentoptions.pos.device.getTransactionCurrency
import com.paymentoptions.pos.services.apiService.InsightsResponseDataRecord
import com.paymentoptions.pos.services.apiService.endpoints.insights
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.DateRangePickerModal
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.innerShadow
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.formatToPrecisionString
import com.paymentoptions.pos.utils.modifiers.DashboardStatsShimmer
import com.paymentoptions.pos.utils.modifiers.TransactionListShimmer
import com.paymentoptions.pos.utils.modifiers.conditional
import com.paymentoptions.pos.utils.modifiers.innerShadow
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


@Composable
fun BottomSectionContent(navController: NavController, enableScrolling: Boolean = false) {
    val context = LocalContext.current
    var receivalAmount: Float by remember { mutableFloatStateOf(0.0f) }
    var currency by remember { mutableStateOf(getTransactionCurrency(context)) }
    var apiResponseAvailable by remember { mutableStateOf(false) }
    var transactions by remember { mutableStateOf<List<InsightsResponseDataRecord>>(listOf()) }
    val scrollState = rememberScrollState()

    var showBarChart by remember { mutableStateOf(false) }
    var fromDateCustomFilter by remember { mutableStateOf<Long?>(null) }
    var toDateCustomFilter by remember { mutableStateOf<Long?>(null) }

    var receivalForText by remember { mutableStateOf("Receival for the day") }
    var receivalForTimePeriodText by remember { mutableStateOf("") }

    var startDate by remember { mutableStateOf(OffsetDateTime.now())}
    var endDate by remember { mutableStateOf( OffsetDateTime.now())}

    val filters = mapOf<String, String>(
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

    LaunchedEffect(selectedFilter, fromDateCustomFilter, toDateCustomFilter) {

        when (selectedFilter.key) {
            "Today" -> {
                apiResponseAvailable = false
                val today = OffsetDateTime.now()

                receivalForText = "Receival for the day"
                receivalForTimePeriodText = SimpleDateFormat("dd MMMM, YYYY").format(Date())
                receivalAmount = 0.0f

                startDate = today
                endDate = today
            }

            "Week" -> {
                val today = OffsetDateTime.now()
                val aWeekAgo = today.minusWeeks(1)

                receivalForText = "Receival for the week"
                receivalForTimePeriodText = "${aWeekAgo.dayOfMonth} ${
                    aWeekAgo.month.toString().lowercase()
                        .replaceFirstChar { it.titlecase(Locale.ROOT) }
                }, ${aWeekAgo.year} to ${today.dayOfMonth} ${
                    today.month.toString().lowercase()
                        .replaceFirstChar { it.titlecase(Locale.ROOT) }
                }, ${today.year}"
                receivalAmount = 0.0f

                startDate = aWeekAgo
                endDate = today
            }

            "Month" -> {
                val today = OffsetDateTime.now()
                val aMonthAgo = today.minusMonths(1)

                receivalForText = "Receival for the month"
                receivalForTimePeriodText = run {
                    today.month.toString()
                    "${aMonthAgo.dayOfMonth} ${
                        aMonthAgo.month.toString().lowercase()
                            .replaceFirstChar { it.titlecase(Locale.ROOT) }
                    }, ${aMonthAgo.year} to ${today.dayOfMonth} ${
                        today.month.toString().lowercase()
                            .replaceFirstChar { it.titlecase(Locale.ROOT) }
                    }, ${today.year}"
                }
                receivalAmount = 0.0f

                startDate = aMonthAgo
                endDate = today
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

                    startDate = OffsetDateTime.ofInstant(
                        Instant.ofEpochMilli(fromDateCustomFilter!!),
                        ZoneId.systemDefault()
                    )

                    endDate = OffsetDateTime.ofInstant(
                        Instant.ofEpochMilli(toDateCustomFilter!!),
                        ZoneId.systemDefault()
                    )
                } else receivalForTimePeriodText = ""
            }
        }

        try {
            val insightsResponse = insights(
                context,
                startDate = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    .replace('-', '/') + " 00:00:00",
                endDate = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    .replace('-', '/') + " 23:59:59",
            )

            if (insightsResponse != null) transactions = insightsResponse.data.records

            println("insights Response -->: $insightsResponse")
        } catch (e: Exception) {
            println("insights Error -->: $e")
        } finally {
            apiResponseAvailable = true
        }
    }

    if (!apiResponseAvailable) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            DashboardStatsShimmer()
            Spacer(modifier = Modifier.height(20.dp))
            TransactionListShimmer(itemCount = 5)
        }
    } else {
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
                verticalAlignment = Alignment.CenterVertically
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
                            blur = 8.dp,
                            spread = 5.dp,
                            cornersRadius = 8.dp,
                            offsetX = 0.dp,
                            offsetY = 0.dp
                        )
                        .zIndex(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Show list",
                        modifier = Modifier
                            .clickable { showBarChart = false }
                            .padding(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (!showBarChart) Color.White else Color.Transparent)
                            .padding(4.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Show bar graph",
                        modifier = Modifier
                            .clickable { showBarChart = true }
                            .padding(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (showBarChart) Color.White else Color.Transparent)
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .padding(horizontal = if (showBarChart) DEFAULT_BOTTOM_SECTION_PADDING_IN_DP else 0.dp)
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

                CurrencyText(
                    currency = currency,
                    amount = receivalAmount.formatToPrecisionString(),
                    fontWeight = FontWeight(980)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .conditional(enableScrolling) { verticalScroll(scrollState) }) {

                    if (showBarChart) TransactionsGroupedBarChart(
                        navController = navController,
                        transactions = transactions,
                        startDate = startDate,
                        endDate = endDate,
                        currency = currency,
                        updateReceivalAmount = {
                            updateReceivalAmount(it)
                        }
//                      Insights(transactions = transactions, currency = currency, updateReceivalAmount = {
//                            updateReceivalAmount(it) })
                    ) else Transactions(
                        navController, transactions = transactions, updateReceivalAmount = {
                            updateReceivalAmount(it)
                        })
                }

            }
        }
    }
}