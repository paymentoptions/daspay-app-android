package com.paymentoptions.pos.ui.screens.transactionshistory

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.paymentoptions.pos.device.DPStorageManager.getKeyValue
import com.paymentoptions.pos.device.DPStorageManager.getTransactionCurrency
import com.paymentoptions.pos.device.DPStorageManager.saveKeyValue
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.endpoints.insights
import com.paymentoptions.pos.network.InsightsResponseDataRecord
import com.paymentoptions.pos.storage.AppStorage
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.DateRangePickerModal
import com.paymentoptions.pos.ui.composables._components.MyDropdown
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.innerShadow
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.formatToPrecisionString
import com.paymentoptions.pos.utils.getDeviceIdentifier
import com.paymentoptions.pos.utils.modifiers.DashboardStatsShimmer
import com.paymentoptions.pos.utils.modifiers.TransactionListShimmer
import com.paymentoptions.pos.utils.modifiers.conditional
import com.paymentoptions.pos.utils.modifiers.innerShadow
import com.paymentoptions.pos.utils.formatEpochMillis
import com.paymentoptions.pos.utils.parseIsoDateToMillis
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus

@Composable
fun BottomSectionContent(navController: NavController, enableScrolling: Boolean = false, showBarChart: Boolean) {
    val selectedFilterStorageKey = "transaction_history_selected_filter"
    var receivalAmount: Float by remember { mutableFloatStateOf(0.0f) }
    var currency by remember { mutableStateOf(getTransactionCurrency()) }
    var apiResponseAvailable by remember { mutableStateOf(false) }
    var transactions by remember { mutableStateOf<List<InsightsResponseDataRecord>>(listOf()) }
    val scrollState = rememberScrollState()

    var showBarChart by remember { mutableStateOf(showBarChart) }
    var fromDateCustomFilter by remember { mutableStateOf<Long?>(null) }
    var toDateCustomFilter by remember { mutableStateOf<Long?>(null) }

    var receivalForText by remember { mutableStateOf("Receival for the day") }
    var receivalForTimePeriodText by remember { mutableStateOf("") }

    var startMillis by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds())}
    var endMillis by remember { mutableStateOf( Clock.System.now().toEpochMilliseconds())}

    val filters = mapOf(
        "Today" to "Today",
        "Week" to "Week",
        "Month" to "Month",
        "Custom" to "Custom Date Range",
    )

    val persistedFilterKey = remember {
        getKeyValue(selectedFilterStorageKey)
    }

    var selectedFilter by remember {
        mutableStateOf(
            filters.entries.firstOrNull { it.key == persistedFilterKey } ?: filters.entries.first()
        )
    }

    if (selectedFilter.key == "Custom") {
        if (fromDateCustomFilter == null) DateRangePickerModal(
            title = "Start Date",
            onDateSelected = { startDateMillis, endDateMillis ->
                if (startDateMillis == null || endDateMillis == null) {
                    fromDateCustomFilter = null
                    toDateCustomFilter = null
                    selectedFilter = filters.entries.first()
                    saveKeyValue(selectedFilterStorageKey, selectedFilter.key)
                } else {
                    fromDateCustomFilter = startDateMillis
                    toDateCustomFilter = endDateMillis
                }
            },
            onDismiss = {
                selectedFilter = filters.entries.first()
                saveKeyValue(selectedFilterStorageKey, selectedFilter.key)
            })
    } else {
        fromDateCustomFilter = null
        toDateCustomFilter = null
    }

    fun updateReceivalAmount(newAmount: Float) {
        receivalAmount = newAmount
    }

    LaunchedEffect(selectedFilter, fromDateCustomFilter, toDateCustomFilter) {
        val now = Clock.System.now()
        val timeZone = TimeZone.currentSystemDefault()

        when (selectedFilter.key) {
            "Today" -> {
                apiResponseAvailable = false
                receivalForText = "Receival for the day"
                receivalForTimePeriodText = formatEpochMillis(now.toEpochMilliseconds(), "dd MMMM, YYYY")
                receivalAmount = 0.0f
                startMillis = now.toEpochMilliseconds()
                endMillis = now.toEpochMilliseconds()
            }

            "Week" -> {
                val aWeekAgo = now.minus(7, DateTimeUnit.DAY, timeZone)
                receivalForText = "Receival for the week"
                val startStr = formatEpochMillis(aWeekAgo.toEpochMilliseconds(), "dd MMM, YYYY")
                val endStr = formatEpochMillis(now.toEpochMilliseconds(), "dd MMM, YYYY")
                receivalForTimePeriodText = "$startStr to $endStr"
                receivalAmount = 0.0f
                startMillis = aWeekAgo.toEpochMilliseconds()
                endMillis = now.toEpochMilliseconds()
            }

            "Month" -> {
                val aMonthAgo = now.minus(1, DateTimeUnit.MONTH, timeZone)
                receivalForText = "Receival for the month"
                val startStr = formatEpochMillis(aMonthAgo.toEpochMilliseconds(), "dd MMM, YYYY")
                val endStr = formatEpochMillis(now.toEpochMilliseconds(), "dd MMM, YYYY")
                receivalForTimePeriodText = "$startStr to $endStr"
                receivalAmount = 0.0f
                startMillis = aMonthAgo.toEpochMilliseconds()
                endMillis = now.toEpochMilliseconds()
            }

            "Custom" -> {
                receivalForText = "Receival for the period"
                receivalAmount = 0.0f

                if (fromDateCustomFilter != null && toDateCustomFilter != null) {
                    val startStr = formatEpochMillis(fromDateCustomFilter!!, "dd MMMM YYYY")
                    val endStr = formatEpochMillis(toDateCustomFilter!!, "dd MMMM YYYY")
                    receivalForTimePeriodText = "$startStr to $endStr"
                    startMillis = fromDateCustomFilter!!
                    endMillis = toDateCustomFilter!!
                } else receivalForTimePeriodText = ""
            }
        }

        try {
            val deviceNumber = AppStorage.deviceNumber ?: getDeviceIdentifier()
            val uniqueCode = AppStorage.tokenCode ?: ""
            
            val startDateStr = formatEpochMillis(startMillis, "YYYY/MM/dd") + " 00:00:00"
            val endDateStr = formatEpochMillis(endMillis, "YYYY/MM/dd") + " 23:59:59"

            val insightsResponse = insights(
                deviceNumber = deviceNumber,
                uniqueCode = uniqueCode,
                startDate = startDateStr,
                endDate = endDateStr,
            )

            if (insightsResponse != null) transactions = insightsResponse.data.records

            AppLogger.debug("insights Response -->: $insightsResponse")
        } catch (e: Exception) {
            AppLogger.debug("insights Error -->: $e")
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
                    onFilterChange = {
                        selectedFilter = it
                        saveKeyValue(selectedFilterStorageKey, it.key)
                    },
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

                    if (showBarChart) {
                         TransactionsGroupedBarChart(
                            navController = navController,
                            transactions = transactions,
                            startDateMillis = startMillis,
                            endDateMillis = endMillis,
                            currency = currency,
                            updateReceivalAmount = {
                                updateReceivalAmount(it)
                            }
                        )
                    } else {
                        Transactions(
                            navController, transactions = transactions,
                            updateReceivalAmount = {
                                updateReceivalAmount(it)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
expect fun TransactionsGroupedBarChart(
    navController: NavController,
    transactions: List<InsightsResponseDataRecord>,
    currency: String,
    updateReceivalAmount: (Float) -> Unit,
    startDateMillis: Long,
    endDateMillis: Long,
)
