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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.services.apiService.TransactionListResponse
import com.paymentoptions.pos.services.apiService.endpoints.transactionsList
import com.paymentoptions.pos.ui.composables._components.CurrencyText
import com.paymentoptions.pos.ui.composables._components.MyCircularProgressIndicator
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.conditional
import kotlin.math.ceil

@Composable
fun BottomSectionContent(navController: NavController, enableScrolling: Boolean = false) {
    val context = LocalContext.current
    var receivalAmount: Float by remember { mutableFloatStateOf(0.0f) }
    var currency by remember { mutableStateOf("") }
    var apiResponseAvailable by remember { mutableStateOf(false) }
    var transactionList by remember { mutableStateOf<TransactionListResponse?>(null) }
    var take: Int by remember { mutableIntStateOf(10) }
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

        }
        apiResponseAvailable = true
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
                amount = receivalAmount.toString(),
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
                    text = "Recent Transactions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primary500,
                )

                SuggestionChip(border = borderThin, onClick = {
//                navController.navigate(Screens.TransactionHistory.route)
                }, label = {
                    Text(
                        text = "View All", fontSize = 14.sp, fontWeight = FontWeight.Bold
                    )
                })
            }

            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .conditional(enableScrolling) { verticalScroll(scrollState) }) {
                Transactions(
                    navController,
                    transactions = transactionList?.data?.records,
                    updateReceivalAmount = {
                        updateReceivalAmount(it)
                    })
            }
        }
    }

}