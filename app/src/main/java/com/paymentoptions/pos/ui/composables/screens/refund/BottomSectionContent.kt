package com.paymentoptions.pos.ui.composables.screens.refund

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.services.apiService.TransactionListResponse
import com.paymentoptions.pos.services.apiService.endpoints.transactionsList
import com.paymentoptions.pos.ui.composables._components.MyCircularProgressIndicator
import com.paymentoptions.pos.ui.composables._components.screentitle.ScreenTitleWithCloseButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.screens.dashboard.Transactions
import com.paymentoptions.pos.utils.conditional
import kotlin.math.ceil

@Composable
fun BottomSectionContent(navController: NavController, enableScrolling: Boolean = false) {
    val context = LocalContext.current
    var receivalAmount: Float by remember { mutableFloatStateOf(0.0f) }
    var currency by remember { mutableStateOf("") }
    var apiResponseAvailable by remember { mutableStateOf(false) }
    var transactionList by remember { mutableStateOf<TransactionListResponse?>(null) }
    var take: Int by remember { mutableIntStateOf(100) }
    var currentPage: Int by remember { mutableIntStateOf(1) }
    var maxPage: Int by remember { mutableIntStateOf(1) }
    val scrollState = rememberScrollState()

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
                ceil(transactionList!!.data.total_count.toDouble() / take.toDouble()).toInt()

            apiResponseAvailable = true
        } catch (e: Exception) {

        } finally {
            apiResponseAvailable = true
        }
    }

    if (!apiResponseAvailable) Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MyCircularProgressIndicator()
    } else {
        currency = transactionList?.data?.records?.first()?.CurrencyCode ?: ""

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(modifier = Modifier.height(10.dp))

            ScreenTitleWithCloseButton(
                navController = navController,
                title = "Refund",
                modifier = Modifier.padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
            )

            Spacer(Modifier.height(10.dp))

            var transactions = transactionList?.data?.records?.filter {
                it.status == "SUCCESSFUL" && it.TransactionType == "PURCHASE"
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .conditional(enableScrolling) { verticalScroll(scrollState) }) {
                Transactions(
                    navController,
                    transactions = transactions?.toTypedArray(),
                    updateReceivalAmount = { updateReceivalAmount(it) })
            }
        }
    }

}