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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.services.apiService.TransactionListResponse
import com.paymentoptions.pos.services.apiService.endpoints.transactionsList
import com.paymentoptions.pos.ui.composables._components.CustomCircularProgressIndicator
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.bannerBgColor
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import kotlin.math.ceil

data class Filter(
    val text: String,
    val matchText: String,
    val badgeCount: Int = 0,
)

@Composable
fun FilterButton(filter: Filter, selected: Filter, onClick: () -> Unit) {

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
        if (filter.badgeCount > 0) Box(
            modifier = Modifier
                .size(16.dp)
                .background(Color.White)
                .clip(RoundedCornerShape(50))
                .background(primary100.copy(alpha = 0.2f)), contentAlignment = Alignment.Center
        ) {
            Text(filter.badgeCount.toString(), color = bannerBgColor, fontSize = 12.sp)
        }
    }
}

@Composable
fun BottomSectionContent(navController: NavController) {

    val filters = arrayOf<Filter>(
        Filter("All", "ALL", 5),
        Filter("Received", "SUCCESSFUL", 1),
        Filter("Refunded", "REFUND"),
        Filter("Failed", "NOTSUCCESSFUL")
    )
    var selected by remember { mutableStateOf<Filter>(filters[0]) }

    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var apiResponseAvailable by remember { mutableStateOf(false) }
    var transactionList by remember { mutableStateOf<TransactionListResponse?>(null) }
    var take: Int by remember { mutableIntStateOf(100) }
    var currentPage: Int by remember { mutableIntStateOf(1) }
    var maxPage: Int by remember { mutableIntStateOf(1) }
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

        ScreenTitleWithCloseButton(title = "Notifications", navController = navController)

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            filters.forEachIndexed { index, filter ->
                FilterButton(filter = filter, selected, onClick = {
                    selected = filter
                })
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (apiResponseAvailable) {
            if (transactionList == null) {
                Toast.makeText(
                    context, "Token expired. Please log in again.", Toast.LENGTH_LONG
                ).show()
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
                        println("transaction: ${transaction.status}")

                        var skip = true
                        if (transaction.trackID !== "N/A") transactionsWithTrackId[transaction.trackID] =
                            true

                        if (!transactionsWithTrackId.contains(transaction.uuid)) {
                            if ((selected.text.uppercase() == "ALL" || (selected.matchText == transaction.status.uppercase() && transaction.TransactionType.uppercase() != "REFUND") || selected.text.uppercase() == transaction.TransactionType.uppercase())) skip =
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
                CustomCircularProgressIndicator()
            }
        }
    }
}
