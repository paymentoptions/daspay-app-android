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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.services.apiService.TransactionListResponse
import com.paymentoptions.pos.services.apiService.endpoints.transactionsList
import com.paymentoptions.pos.ui.composables._components.CustomCircularProgressIndicator
import com.paymentoptions.pos.ui.composables.screens.dashboard.Transactions
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.Orange10
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary900
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.ceil

@Composable
fun BottomSectionContent(navController: NavController) {
    val context = LocalContext.current
    var receivalAmount: Float by remember { mutableFloatStateOf(0.0f) }
    val currency = "JPY"
    var apiResponseAvailable by remember { mutableStateOf(false) }
    var transactionList by remember { mutableStateOf<TransactionListResponse?>(null) }
    var take: Int by remember { mutableIntStateOf(10) }
    var currentPage: Int by remember { mutableIntStateOf(1) }
    var maxPage: Int by remember { mutableIntStateOf(1) }
    val dateStringFormatted: String = SimpleDateFormat("dd MMMM, YYYY").format(Date())
    var showInsights by remember { mutableStateOf(true) }

    var filters = mapOf<String, String>(
        "Today" to "Today",
        "Week" to "Week",
        "Month" to "Month",
        "Custom" to "Custom",
    )

    var selectedFilterKey by remember { mutableStateOf(filters.iterator().next().key) }
    var selectedFilterValue by remember { mutableStateOf(filters.iterator().next().value) }

    fun updateReceivalAmount(newAmount: Float) {
        receivalAmount = newAmount
    }

    LaunchedEffect(currentPage) {
        apiResponseAvailable = false
        try {
            val skip = (currentPage - 1) * take
            transactionList = transactionsList(context, take, skip)

            if (transactionList == null) {
                maxPage = 0
            } else {
                maxPage =
                    ceil(transactionList!!.data.total_count.toDouble() / take.toDouble()).toInt()
            }
            apiResponseAvailable = true
        } catch (e: Exception) {

        }
        apiResponseAvailable = true
    }

    if (apiResponseAvailable) Column(
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

            CustomDropdown(filters, selectedFilterKey, onFilterChange = { key, value ->
                selectedFilterKey = key
                selectedFilterValue = value
            }, icon = Icons.Default.CalendarMonth)

            Row(
                Modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBackgroundColor)
                    .clickable(onClick = {
                        showInsights = !showInsights
                    }),
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
            modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Receival for the day",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = primary900,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = dateStringFormatted,
                style = AppTheme.typography.footnote,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            primary100.copy(alpha = 0.5f), fontWeight = FontWeight.Light
                        )
                    ) { append("$currency ") }

                    withStyle(SpanStyle(primary100)) { append(receivalAmount.toString()) }
                },
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = primary100,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (showInsights) Insights(
                transactions = transactionList?.data?.records, updateReceivalAmount = {
                    updateReceivalAmount(it)
                }) else Transactions(
                navController,
                transactions = transactionList?.data?.records,
                updateReceivalAmount = {
                    updateReceivalAmount(it)
                })
        }
    }
    else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CustomCircularProgressIndicator(null, Orange10)
        }
    }
}