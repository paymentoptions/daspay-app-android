package com.paymentoptions.pos.ui.composables.screens._flow.refundFlow.refund

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.services.apiService.endpoints.transactionListV2
import com.paymentoptions.pos.ui.composables._components.MyCircularProgressIndicator
import com.paymentoptions.pos.ui.composables._components.ScreenTitleWithCloseButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.composables.screens.dashboard.Transactions
import com.paymentoptions.pos.utils.isScrolledToTheEnd
import kotlin.math.ceil

@Composable
fun BottomSectionContent(navController: NavController, enableScrolling: Boolean = false) {
    val context = LocalContext.current
    var firstPageFetch by remember { mutableStateOf(false) }
    var apiResponseAvailable by remember { mutableStateOf(false) }
    var transactions by remember { mutableStateOf<List<TransactionListDataRecord>>(listOf()) }
    var take by remember { mutableIntStateOf(20) }
    var currentPage by remember { mutableIntStateOf(1) }
    var maxPage by remember { mutableIntStateOf(0) }
    val lazyColumnState = rememberLazyListState()

    val scrollingEndReached by remember {
        derivedStateOf { lazyColumnState.isScrolledToTheEnd() }
    }

    var totalTransactionCount by remember { mutableIntStateOf(take) }

    fun nextPageHandler() {
        if (currentPage < maxPage) currentPage++
    }

    LaunchedEffect(currentPage, take) {
        apiResponseAvailable = false
        try {
            val skip = (currentPage - 1) * take
            val transactionListFromAPI = transactionListV2(context, take, skip)

            if (transactionListFromAPI != null) {
                maxPage =
                    ceil(transactionListFromAPI.data.total_count.toDouble() / take.toDouble()).toInt()

                totalTransactionCount = transactionListFromAPI.data.total_count

                //Logic to filter in transactions that can be refunded
                transactions = transactions.plus(transactionListFromAPI.data.records.filter {
                    it.status == "SUCCESSFUL" && it.TransactionType == "PURCHASE"
                })
            }
        } catch (e: Exception) {
            if (e.toString().contains("HTTP 401")) {
                Toast.makeText(
                    context,
                    "Your session has expired. Please log in again to continue.",
                    Toast.LENGTH_SHORT
                ).show()

                SharedPreferences.clearSharedPreferences(context)
                navController.navigate(Screens.AuthCheck.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        } finally {
            apiResponseAvailable = true
            firstPageFetch = true
        }
    }

    if (scrollingEndReached) LaunchedEffect(Unit) {
        nextPageHandler()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        ScreenTitleWithCloseButton(
            navController = navController,
            title = "Refund",
            modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (!firstPageFetch && !apiResponseAvailable) {
            MyCircularProgressIndicator()
        } else Column(modifier = Modifier.fillMaxWidth())
        {
            Transactions(
                navController,
                transactions = transactions,
                updateReceivalAmount = { },
                lazyColumnState = lazyColumnState
            )
        }
    }
}