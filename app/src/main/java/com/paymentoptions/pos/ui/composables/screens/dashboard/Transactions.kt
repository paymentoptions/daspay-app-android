package com.paymentoptions.pos.ui.composables.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.ui.composables._components.NoData

@Composable
fun Transactions(
    navController: NavController,
    transactions: Array<TransactionListDataRecord>?,
    updateReceivalAmount: (Float) -> Unit,
) {
    val scrollState = rememberScrollState()
    var receivalAmount = 0.0f
    var selectedFilterKey by remember { mutableStateOf("ALL") }
    var transactionsWithTrackId = mutableMapOf<String, Boolean>()

    if (transactions == null) NoData(text = "No transactions found")
    else Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        for (transaction in transactions) {
            receivalAmount = receivalAmount + transaction.amount.toFloat()

            var skip = true

            if (transaction.trackID !== "N/A") transactionsWithTrackId[transaction.trackID] = true

            if (!transactionsWithTrackId.contains(transaction.uuid)) {
                if ((selectedFilterKey === "ALL" || (selectedFilterKey == transaction.status.uppercase() && transaction.TransactionType.uppercase() != "REFUND") || selectedFilterKey == transaction.TransactionType.uppercase())) skip =
                    false
            }

            if (!skip) TransactionSummary(transaction)
        }
        updateReceivalAmount(receivalAmount)
    }
}