package com.paymentoptions.pos.ui.composables.screens.transactionshistory

import CustomDropdown
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.navigation.NavController
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.services.apiService.TransactionListResponse
import com.paymentoptions.pos.services.apiService.endpoints.transactionsList
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.CustomCircularProgressIndicator
import com.paymentoptions.pos.ui.composables._components.DatePickerModal
import com.paymentoptions.pos.ui.composables.screens.dashboard.Transactions
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.Orange10
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.primary900
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Date
import kotlin.math.ceil


@Composable
fun BottomSectionContent(navController: NavController) {
    val context = LocalContext.current
    var receivalAmount: Float by remember { mutableFloatStateOf(0.0f) }
    var currency by remember { mutableStateOf("") }
    var apiResponseAvailable by remember { mutableStateOf(false) }
    var transactionList by remember { mutableStateOf<TransactionListResponse?>(null) }

    var take: Int by remember { mutableIntStateOf(50) }
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
        "Custom" to "Custom",
    )

    var selectedFilter by remember { mutableStateOf<Map.Entry<String, String>>(filters.entries.first()) }

    if (selectedFilter.key == "Custom") {
        if (fromDateCustomFilter == null) DatePickerModal(title = "Start Date", {

            if (it == null) {
                fromDateCustomFilter = null
                toDateCustomFilter = null
                selectedFilter = filters.entries.first()
            } else fromDateCustomFilter = it

        }, { selectedFilter = filters.entries.first() })
        else if (toDateCustomFilter == null) DatePickerModal(title = "End Date", {
            if (it == null) {
                fromDateCustomFilter = null
                toDateCustomFilter = null
                selectedFilter = filters.entries.first()
            } else toDateCustomFilter = it
        }, { selectedFilter = filters.entries.first() })
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
            transactionList = transactionsList(context, take, skip)

            if (transactionList == null) maxPage = 0
            else maxPage =
                ceil(transactionList!!.data.total_count.toDouble() / transactionList!!.data.total_count.toDouble()).toInt()

            apiResponseAvailable = true
        } catch (e: Exception) {

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
                receivalForText = "Receival for the week"
                receivalForTimePeriodText = SimpleDateFormat("MMMM YYYY").format(Date())
                receivalAmount = 0.0f

            }

            "Month" -> {
                receivalForText = "Receival for the month"
                receivalForTimePeriodText = SimpleDateFormat("MMMM YYYY").format(Date())
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
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CustomCircularProgressIndicator(null, Orange10)
        }
    } else {
        currency = transactionList?.data?.records?.first()?.CurrencyCode ?: ""

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Transaction History",
                modifier = Modifier.align(alignment = Alignment.Start),
                style = AppTheme.typography.titleNormal
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                CustomDropdown(
                    filters,
                    selectedFilter,
                    onFilterChange = { selectedFilter = it },
                    icon = Icons.Default.CalendarMonth
                )

                Row(
                    Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBackgroundColor)
                        .clickable(onClick = { showInsights = !showInsights }),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Show list",
                        modifier = Modifier
                            .padding(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (!showInsights) Color.White.copy(alpha = 0.9f) else Color.Transparent)
                            .padding(4.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Show bar graph",
                        modifier = Modifier
                            .padding(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (showInsights) Color.White.copy(alpha = 0.9f) else Color.Transparent)
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
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
                    currency = currency, amount = receivalAmount.toString(), fontSize = 36.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                var transactions = transactionList?.data?.records?.filter {

                    var filterIn = when (selectedFilter.key) {
                        "Today" -> dayFilterFn(it)
                        "Week" -> weekFilterFn(it)
                        "Month" -> monthFilterFn(it)
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

                if (showInsights) Insights(
                    transactions = transactions?.toTypedArray(),
                    currency = currency,
                    updateReceivalAmount = {
                        updateReceivalAmount(it)
                    }) else Transactions(
                    navController,
                    transactions = transactions?.toTypedArray(),
                    updateReceivalAmount = {
                        updateReceivalAmount(it)
                    })
            }
        }
    }
}

fun dayFilterFn(transaction: TransactionListDataRecord): Boolean {
    val today = OffsetDateTime.now().toLocalDateTime()
    val transactionDate = OffsetDateTime.parse(transaction.Date).toLocalDateTime()

    return today.dayOfMonth == transactionDate.dayOfMonth && today.month == transactionDate.month && today.year == transactionDate.year
}

fun weekFilterFn(transaction: TransactionListDataRecord): Boolean {
    val today = OffsetDateTime.now().toLocalDateTime()
    val transactionDate = OffsetDateTime.parse(transaction.Date).toLocalDateTime()

    return transactionDate > today.minusWeeks(2)
}

fun monthFilterFn(transaction: TransactionListDataRecord): Boolean {
    val today = OffsetDateTime.now().toLocalDateTime()
    val transactionDate = OffsetDateTime.parse(transaction.Date).toLocalDateTime()

    return today.month == transactionDate.month && today.year == transactionDate.year
}

fun customFilterFn(
    transaction: TransactionListDataRecord,
    startDate: Long,
    endDate: Long,
): Boolean {
    val transactionDate = OffsetDateTime.parse(transaction.Date).toInstant().epochSecond * 1000

    return transactionDate >= startDate && transactionDate <= endDate
}