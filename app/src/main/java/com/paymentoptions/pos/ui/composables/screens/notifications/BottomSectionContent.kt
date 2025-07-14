package com.paymentoptions.pos.ui.composables.screens.notifications

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.paymentoptions.pos.ui.composables._components.MyCircularProgressIndicator
import com.paymentoptions.pos.ui.composables._components.screentitle.ScreenTitleWithCloseButton
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.bannerBgColor
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.utils.conditional
import kotlin.math.ceil

data class Tab(
    val text: String,
    val matchText: String,
    val newCount: Int = 0,
)

@Composable
fun FilterButton(filter: Tab, selected: Tab, onClick: () -> Unit) {

    Row(
        modifier = Modifier.clickable {
            onClick()
        },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = filter.text,
            color = primary500,
            fontSize = 14.sp,
            fontWeight = if (selected == filter) FontWeight.Bold else FontWeight.Normal
        )
        if (filter.newCount > 0) Box(
            modifier = Modifier
                .size(20.dp)
                .background(Color.White)
                .clip(RoundedCornerShape(50))
                .background(primary100.copy(alpha = 0.2f)), contentAlignment = Alignment.Center
        ) {
            Text(filter.newCount.toString(), color = bannerBgColor, fontSize = 12.sp)
        }
    }
}

@Composable
fun BottomSectionContent(navController: NavController, enableScrolling: Boolean = false) {

    var tabs = remember {
        mutableStateListOf<Tab>(
            Tab("All", "ALL"),
            Tab("Received", "SUCCESSFUL"),
            Tab("Refunded", "REFUND"),
            Tab("Failed", "NOTSUCCESSFUL")
        )
    }

    var selectedTab by remember { mutableStateOf<Tab>(tabs[0]) }

    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var apiResponseAvailable by remember { mutableStateOf(false) }
    var transactionList by remember { mutableStateOf<TransactionListResponse?>(null) }
    var take: Int by remember { mutableIntStateOf(100) }
    var currentPage: Int by remember { mutableIntStateOf(1) }
    var maxPage: Int by remember { mutableIntStateOf(1) }
    var transactionsWithTrackId = mutableMapOf<String, Boolean>()
    var transactions by remember { mutableStateOf<List<TransactionListDataRecord>?>(listOf()) }

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

    LaunchedEffect(transactionList) {
        transactions = transactionList?.data?.records?.filter { transaction ->

            tabs[0] = Tab(tabs[0].text, tabs[0].matchText, tabs[0].newCount + 1)

            if (transaction.status == "NOTSUCCESSFUL") tabs[3] = Tab(
                tabs[3].text, tabs[3].matchText, tabs[3].newCount + 1
            )
            else if (transaction.status == "SUCCESSFUL") {
                if (transaction.TransactionType == "PURCHASE") tabs[1] =
                    Tab(tabs[1].text, tabs[1].matchText, tabs[1].newCount + 1)
                else if (transaction.TransactionType == "REFUND") tabs[2] =
                    Tab(tabs[2].text, tabs[2].matchText, tabs[2].newCount + 1)
            }

            selectedTab.matchText == transaction.status
        }
    }

    fun firstPageHandler() {
        currentPage = 1
    }

    fun backPageHandler() {
        currentPage--
    }

    fun nextPageHandler() {
        currentPage++
    }

    fun lastPageHandler() {
        currentPage = maxPage
    }

    fun exitToLoginScreen() {
        navController.navigate("loginScreen") {
            popUpTo(0) { inclusive = true }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        ScreenTitleWithCloseButton(title = "Notifications", navController = navController)

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            tabs.forEachIndexed { index, filter ->
                FilterButton(filter = filter, selectedTab, onClick = { selectedTab = filter })
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (!apiResponseAvailable) Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MyCircularProgressIndicator()
        } else {
            if (transactionList == null) {
                Toast.makeText(
                    context, "Token expired. Please log in again.", Toast.LENGTH_LONG
                ).show()
                navController.navigate(Screens.SignIn.route) {
                    popUpTo(0) { inclusive = true }
                }
            }

            transactions = transactionList?.data?.records?.filter {

                var filterIn = when (selectedTab.text) {
                    "All" -> allFilterFn(it)
                    "Received" -> receivedFilterFn(it)
                    "Refunded" -> refundedFilterFn(it)
                    "Failed" -> failedFilterFn(it)
                    else -> false
                }

                filterIn
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .fillMaxSize()
                    .conditional(enableScrolling) { verticalScroll(scrollState) }) {

                for (transaction in transactions!!.toTypedArray()) {

                    var skip = true
                    if (transaction.trackID !== "N/A") transactionsWithTrackId[transaction.trackID] =
                        true

                    if (!transactionsWithTrackId.contains(transaction.uuid)) {
                        if ((selectedTab.text.uppercase() == "ALL" || (selectedTab.matchText == transaction.status.uppercase() && transaction.TransactionType.uppercase() != "REFUND") || selectedTab.text.uppercase() == transaction.TransactionType.uppercase())) skip =
                            false
                    }

                    if (!skip) NotificationSummary(transaction)
                }
            }
        }
    }
}

fun allFilterFn(transaction: TransactionListDataRecord): Boolean {
    return true
}

fun receivedFilterFn(transaction: TransactionListDataRecord): Boolean {
    return transaction.TransactionType == "PURCHASE" && transaction.status == "SUCCESSFUL"
}

fun refundedFilterFn(transaction: TransactionListDataRecord): Boolean {
    return transaction.TransactionType == "REFUND" && transaction.status == "SUCCESSFUL"
}

fun failedFilterFn(transaction: TransactionListDataRecord): Boolean {
    return transaction.status == "NOTSUCCESSFUL"
}
