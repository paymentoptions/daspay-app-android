package com.paymentoptions.pos.ui.composables.screens.dashboard

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord
import com.paymentoptions.pos.ui.composables._components.NoData

@Composable
fun Transactions(
    navController: NavController,
    transactions: List<TransactionListDataRecord>,
    updateReceivalAmount: (Float) -> Unit,
) {
    val context = LocalContext.current
    var selectedFilterKey by remember { mutableStateOf("ALL") }
    var transactionsWithTrackId = mutableMapOf<String, Boolean>()
    var longClickedTransactionId by remember { mutableStateOf("") }
    var backPressHandled by remember { mutableStateOf(false) }

    BackHandler(enabled = !backPressHandled) {
        longClickedTransactionId = ""
        backPressHandled = true
    }

    if (transactions.isEmpty()) NoData(text = "No transactions found")
    else Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.End,
        modifier = Modifier.fillMaxSize()
//            .verticalScroll(scrollState)
    ) {

        var earningAmount = 0.0f

        for (transaction in transactions) {

            if (transaction.trackID !== "N/A") transactionsWithTrackId[transaction.trackID] = true
            var skip = true

            if (!transactionsWithTrackId.contains(transaction.uuid)) {
                if ((selectedFilterKey == "ALL" || (selectedFilterKey == transaction.status.uppercase() && transaction.TransactionType.uppercase() != "REFUND") || selectedFilterKey == transaction.TransactionType.uppercase())) skip =
                    false
            }

            if (!skip) {
                if (transaction.TransactionType == "PURCHASE" && transaction.status == "SUCCESSFUL") earningAmount += transaction.amount.toFloat()

                TransactionSummary(
                    navController, transaction, longClickedTransactionId, onLongClick = {

                        if (transaction.TransactionType == "PURCHASE" && transaction.status == "SUCCESSFUL") {
                            longClickedTransactionId = if (longClickedTransactionId.isEmpty()) it
                            else if (longClickedTransactionId == it) "" else it
                        } else Toast.makeText(
                            context,
                            "Txn details: ${transaction.status} | ${transaction.TransactionType}: refund not enabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
            }
        }
        updateReceivalAmount(earningAmount)
    }
}