package com.paymentoptions.pos.ui.composables.screens.dashboard

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.paymentoptions.pos.ui.composables._components.CustomCircularProgressIndicator
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.Orange10
import com.paymentoptions.pos.ui.theme.primary500
import kotlin.math.ceil

@Composable
fun RecentTransactions(navController: NavController) {
    val scrollState = rememberScrollState()
    var viewAll by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var apiResponseAvailable by remember { mutableStateOf(false) }
    var transactionList by remember { mutableStateOf<TransactionListResponse?>(null) }
    var take: Int by remember { mutableIntStateOf(30) }
    var currentPage: Int by remember { mutableIntStateOf(1) }
    var maxPage: Int by remember { mutableIntStateOf(1) }
    mapOf<String, String>(
        "ALL" to "ALL",
        "SUCCESSFUL" to "SUCCESS",
        "REFUND" to "REFUNDED",
        "NOTSUCCESSFUL" to "FAILED"
    )
    var selectedFilterKey by remember { mutableStateOf("ALL") }
    var transactionsWithTrackId = mutableMapOf<String, Boolean>()


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

    Column {

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Recent Transactions",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = primary500,
            )

            SuggestionChip(onClick = {
                viewAll = !viewAll
            }, label = { Text(text = "View All", fontSize = 14.sp, fontWeight = FontWeight.Bold) })
        }

        Spacer(Modifier.height(10.dp))

        if (apiResponseAvailable) {
            if (transactionList == null) {
                Toast.makeText(context, "Token expired. Please log in again.", Toast.LENGTH_LONG)
                    .show()
                navController.navigate(Screens.SignIn.route) {
                    popUpTo(0) { inclusive = true }
                }
            }

            transactionList?.let {

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {

                    for (transaction in it.data.records) {
                        println("transaction: $transaction")
                        var skip = true

                        if (transaction.trackID !== "N/A") transactionsWithTrackId[transaction.trackID] =
                            true

                        if (!transactionsWithTrackId.contains(transaction.uuid)) {
                            if ((selectedFilterKey === "ALL" || (selectedFilterKey == transaction.status.uppercase() && transaction.TransactionType.uppercase() != "REFUND") || selectedFilterKey == transaction.TransactionType.uppercase())) skip =
                                false
                        }

                        if (!skip) TransactionSummary(transaction)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CustomCircularProgressIndicator(null, Orange10)
            }
        }
    }
}