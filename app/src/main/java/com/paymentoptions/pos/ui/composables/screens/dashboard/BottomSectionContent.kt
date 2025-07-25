package com.paymentoptions.pos.ui.composables.screens.dashboard

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.services.apiService.endpoints.transactionList
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.MyCircularProgressIndicator
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.formatToPrecisionString
import com.paymentoptions.pos.utils.modifiers.conditional
import kotlin.math.ceil

@Composable
fun BottomSectionContent(navController: NavController, enableScrolling: Boolean = false) {
    val context = LocalContext.current
    var receivalAmount: Float by remember { mutableFloatStateOf(0.0f) }
    var currency by remember { mutableStateOf("") }
    var apiResponseAvailable by remember { mutableStateOf(false) }
    var viewAll by remember { mutableStateOf(false) }
    var transactions by remember { mutableStateOf<List<TransactionListDataRecord>>(listOf()) }
    var take: Int by remember { mutableIntStateOf(10) }
    var currentPage: Int by remember { mutableIntStateOf(1) }
    var maxPage: Int by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()
    val scrollingEndReached by remember {
        derivedStateOf {
            scrollState.value == scrollState.maxValue
        }
    }
    var totalTransactionCount by remember { mutableIntStateOf(10) }

    fun nextPageHandler() {
        if (currentPage < maxPage) currentPage++
    }

    LaunchedEffect(viewAll) {
        take = if (viewAll) totalTransactionCount else 10
        currentPage = 1
        transactions = listOf<TransactionListDataRecord>()
    }

    LaunchedEffect(currentPage, take) {
        apiResponseAvailable = false
        try {
            val skip = (currentPage - 1) * take
            val transactionListFromAPI = transactionList(context, take, skip)

            if (transactionListFromAPI != null) {
                maxPage =
                    ceil(transactionListFromAPI.data.total_count.toDouble() / take.toDouble()).toInt()

                totalTransactionCount = transactionListFromAPI.data.total_count
                transactions = transactions.plus(transactionListFromAPI.data.records)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error fetching next page from API", Toast.LENGTH_SHORT).show()
        } finally {
            apiResponseAvailable = true
//            val scrollToIndex = if (currentPage >= 2) (currentPage - 2) * take else 0
//            println("scrollToIndex: $scrollToIndex")
//            scrollState.scrollTo(
//                scrollToIndex * 20
//
//            )
        }

    }

    if (scrollingEndReached && !viewAll) LaunchedEffect(Unit) {
        nextPageHandler()
        scrollState.scrollTo(0)
    }

    fun updateReceivalAmount(newAmount: Float) {
        receivalAmount = newAmount
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        currency = transactions.firstOrNull()?.CurrencyCode ?: ""

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Receival for the day",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = primary900,
                modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
            )

            Spacer(modifier = Modifier.height(4.dp))

            CurrencyText(
                currency = currency,
                amount = receivalAmount.formatToPrecisionString(),
                modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
            )

            Spacer(modifier = Modifier.height(16.dp))

            FilledButton(
                text = "View Insights",
                onClick = { navController.navigate(Screens.TransactionHistory.route) },
                modifier = Modifier
                    .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                    .width(160.dp)
                    .height(37.dp),
            )

            Spacer(Modifier.height(20.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = if (viewAll) "All Transactions" else "Recent Transactions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primary500,
                )

                SuggestionChip(border = borderThin, onClick = { viewAll = !viewAll }, label = {
                    Text(
                        text = if (viewAll) "View recent" else "View All",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                })
            }
        }

        if (!apiResponseAvailable) {
            MyCircularProgressIndicator()
        } else Column(
            modifier = Modifier
                .fillMaxWidth()
                .conditional(enableScrolling) { verticalScroll(scrollState) }) {

            Transactions(
                navController, transactions = transactions, updateReceivalAmount = {
                    updateReceivalAmount(it)
                })
        }
    }
}