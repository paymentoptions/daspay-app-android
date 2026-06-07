package com.paymentoptions.pos.ui.composables.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.paymentoptions.pos.device.DPSharedPreferences
import com.paymentoptions.pos.device.DPSharedPreferences.getTransactionCurrency
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.TransactionListV2Request
import com.paymentoptions.pos.network.TransactionListV2RequestFilter
import com.paymentoptions.pos.network.endpoints.transactionListV2
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.formatToPrecisionString
import com.paymentoptions.pos.utils.isScrolledToTheEnd
import com.paymentoptions.pos.utils.isUnauthorizedError
import com.paymentoptions.pos.utils.showSessionExpiredAndNavigateToFingerprint
import com.paymentoptions.pos.utils.modifiers.TransactionListShimmer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

@Composable
fun BottomSectionContent(navController: NavController, enableScrolling: Boolean = false) {
    val context = LocalContext.current
    var receivalAmount by remember { mutableFloatStateOf(0.0f) }
    var currency by remember { mutableStateOf(getTransactionCurrency(context)) }
    var firstPageFetch by remember { mutableStateOf(false) }
    var apiResponseAvailable by remember { mutableStateOf(false) }
    var transactions by remember { mutableStateOf<List<TransactionListDataRecord>>(listOf()) }
    val take by remember { derivedStateOf { 100 } }
    var currentPage by remember { mutableIntStateOf(1) }
    var maxPage by remember { mutableIntStateOf(0) }
    val lazyColumnState = rememberLazyListState()

    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    val dateStart = today.format(dateFormatter) + " 00:00:00"
    val dateEnd = today.format(dateFormatter) + " 23:59:59"

    val filters = listOf(
        TransactionListV2RequestFilter(
            field = "DateStart",
            operator = "eq",
            value = dateStart,
        ),
        TransactionListV2RequestFilter(
            field = "DateEnd",
            operator = "eq",
            value = dateEnd,
            operand = "AND"
        )
    )

    val scrollingEndReached by remember {
        derivedStateOf { lazyColumnState.isScrolledToTheEnd() }
    }

    var totalTransactionCount by remember { mutableIntStateOf(take) }

    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    fun nextPageHandler() {
        if (currentPage < maxPage) currentPage++
    }

    // Pull-to-refresh handler
    suspend fun refreshTransactions() {
        AppLogger.debug("Refreshing transactions called")
        isRefreshing = true
        apiResponseAvailable = false
        try {
            currentPage = 1
            val skip = 0
            val transactionListFromAPI = transactionListV2(
                TransactionListV2Request(take = take, skip = skip, filter = filters)
            )
            if (transactionListFromAPI != null) {
                maxPage = ceil(transactionListFromAPI.data.total_count.toDouble() / take.toDouble()).toInt()
                totalTransactionCount = transactionListFromAPI.data.total_count
                transactions = transactionListFromAPI.data.records.filterNotNull()
               // receivalAmount = transactionListFromAPI.data.total_amount.toFloat()
            }
        } catch (e: Exception) {
            if (e.isUnauthorizedError()) {
                showSessionExpiredAndNavigateToFingerprint(context, navController)
            }
        } finally {
            isRefreshing = false
            apiResponseAvailable = true
            firstPageFetch = true
        }
    }

    LaunchedEffect(currentPage, take) {
        apiResponseAvailable = false
        try {
            val skip = (currentPage - 1) * take
            val transactionListFromAPI = transactionListV2(
                TransactionListV2Request(take = take, skip = skip, filter = filters)
            )

            if (transactionListFromAPI != null) {
                maxPage =
                    ceil(transactionListFromAPI.data.total_count.toDouble() / take.toDouble()).toInt()

                totalTransactionCount = transactionListFromAPI.data.total_count

                // For page 1, replace the list. For other pages, append
                if (currentPage == 1) {
                    transactions = transactionListFromAPI.data.records.filterNotNull()
                    AppLogger.debug("Replaced transactions list with "+transactions.size+" items")
                } else {
                    transactions = transactions.plus(transactionListFromAPI.data.records.filterNotNull())
                    AppLogger.debug("Appended to transactions list, now "+transactions.size+" items")
                }

                //set receival amount from API total_amount field (rounded to two decimal place)
              //  receivalAmount = transactionListFromAPI.data.total_amount.toFloat()
            }
        } catch (e: Exception) {


            if (e.isUnauthorizedError()) {
                showSessionExpiredAndNavigateToFingerprint(context, navController)
            }
        } finally {
            apiResponseAvailable = true
            firstPageFetch = true
        }
    }

    if (scrollingEndReached) LaunchedEffect(Unit) {
        nextPageHandler()
    }

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            // Launch refresh in a coroutine
            CoroutineScope(Dispatchers.IO).launch {
                refreshTransactions()
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                    fontWeight = FontWeight(990)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if(DPSharedPreferences.isAdmin(context)) {
                    FilledButton(
                        text = "View Insights",
                        onClick = {
                            navController.navigate("${Screens.TransactionHistory.route}?showBarChart=${true}")
                                  },
                        modifier = Modifier
                            .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                            .width(160.dp)
                            .height(35.dp)
                            .scale(0.8f),
                    )
                    Spacer(Modifier.height(20.dp))
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Today's Transactions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primary500,
                    )

                    SuggestionChip(border = borderThin, onClick = {
                        navController.navigate("${Screens.TransactionHistory.route}?showBarChart=${false}")

                    }, label = {
                        Text(
                            text = "View All",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    })
                }
            }

            if (!firstPageFetch && !apiResponseAvailable) {
                TransactionListShimmer(
                    modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
                    itemCount = 5
                )
            } else Column(modifier = Modifier.fillMaxWidth()) {
                Transactions(
                    navController,
                    transactions = transactions,
                    lazyColumnState = lazyColumnState,
                    updateReceivalAmount = {
                        receivalAmount = it
                    }
                )
            }
        }
    }
}