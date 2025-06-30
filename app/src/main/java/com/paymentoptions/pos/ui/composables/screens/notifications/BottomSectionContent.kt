package com.paymentoptions.pos.ui.composables.screens.notifications

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.services.apiService.TransactionListResponse
import com.paymentoptions.pos.services.apiService.endpoints.transactionsList
import com.paymentoptions.pos.ui.composables._components.CustomCircularProgressIndicator
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.Orange10
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary900
import kotlin.math.ceil

data class Tab(
    val title: String,
    val badgeCount: Int = 0,
)

@Composable
fun BottomSectionContent(navController: NavController) {

    var tabs = arrayOf<Tab>(
        Tab("All", 5),
        Tab("Received", 1),
        Tab("Refunded"),
        Tab("Failed")
    )
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }


    val scrollState = rememberScrollState()
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


    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Notifications",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0033CC),
            )

            IconButton(onClick = {
                navController.popBackStack()
            }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, destination ->

                Tab(
                    selected = selectedTabIndex == index,
                    modifier = Modifier.background(Color.White),
                    selectedContentColor = primary900,
                    unselectedContentColor = primary100,
                    onClick = {
                        selectedTabIndex = index
                    },
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(15.dp),
                        ) {
                            Text(
                                text = destination.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )

                            if (destination.badgeCount > 0) BadgedBox(
                                badge = {
                                    Badge(
                                        contentColor = primary900,
                                        containerColor = primary100.copy(alpha = 0.4f),
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Text(destination.badgeCount.toString())
                                    }
                                }) {}

                        }

                    })
            }
        }


        Column(modifier = Modifier.padding(16.dp)) {

            if (apiResponseAvailable) {
                if (transactionList == null) {
                    Toast.makeText(
                        context,
                        "Token expired. Please log in again.",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    navController.navigate(Screens.SignIn.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }

                transactionList?.let {

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
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

                            if (!skip) NotificationSummary(transaction)
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
}
