package com.paymentoptions.pos.ui.composables.screens.transactionshistory

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.services.apiService.InsightsResponseDataRecord
import com.paymentoptions.pos.services.apiService.toTransactionListDataRecord
import com.paymentoptions.pos.ui.composables._components.NoData
import com.paymentoptions.pos.ui.composables.screens.dashboard.TransactionSummary

@Composable
fun Transactions(
    navController: NavController,
    transactions: List<InsightsResponseDataRecord>,
    updateReceivalAmount: (Float) -> Unit
) {
    var selectedFilterKey by remember { mutableStateOf("ALL") }
    var longClickedTransactionId by remember { mutableStateOf("") }
    var backPressHandled by remember { mutableStateOf(false) }

    BackHandler(enabled = !backPressHandled) {
        longClickedTransactionId = ""
        backPressHandled = true
    }

    if (transactions.isEmpty()) NoData(text = "No transactions found")
    else Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.End,
        modifier = Modifier.fillMaxSize()
    ) {

        var earningAmount = 0.0f

        transactions.forEachIndexed { index, transaction ->

            var skip = true

            if ((selectedFilterKey == "ALL" || (selectedFilterKey == transaction.status.uppercase() && transaction.TransactionType.uppercase() != "REFUND") || selectedFilterKey == transaction.TransactionType.uppercase())) skip =
                false

            if (!skip) {
                if (transaction.TransactionType == "PURCHASE" && transaction.status == "SUCCESSFUL") earningAmount += transaction.amount.toFloat()

                TransactionSummary(
                    navController,
                    transaction.toTransactionListDataRecord(),
//                    longClickedTransactionId,
//                    onLongClick = {
//
//                        if (transaction.TransactionType == "PURCHASE" && transaction.status == "SUCCESSFUL") {
//                            longClickedTransactionId = if (longClickedTransactionId.isEmpty()) it
//                            else if (longClickedTransactionId == it) "" else it
//                        } else {
////                            Toast.makeText(
////                                context,
////                                "Txn details: ${transaction.status} | ${transaction.TransactionType}: refund not enabled",
////                                Toast.LENGTH_SHORT
////                            ).show()
//                        }
//                    },
//                    onSwipeLeft = {
//                        if (transaction.TransactionType == "PURCHASE" && transaction.status == "SUCCESSFUL") {
//                            longClickedTransactionId = it
//                        } else {
////                            Toast.makeText(
////                                context,
////                                "Txn details: ${transaction.status} | ${transaction.TransactionType}: refund not enabled",
////                                Toast.LENGTH_SHORT
////                            ).show()
//                        }
//                    },
//                    onSwipeRight = {
//                        longClickedTransactionId = ""
//                    },
//                    triggerListRefresh = { triggerListRefresh() }
                )
            }
        }
        updateReceivalAmount(earningAmount)
    }
}