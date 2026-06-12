package com.paymentoptions.pos.ui.screens.transactionshistory

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
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.InsightsResponseDataRecord
import com.paymentoptions.pos.network.toTransactionListDataRecord
import com.paymentoptions.pos.ui.composables._components.NoData
import com.paymentoptions.pos.ui.screens.dashboard.TransactionSummary
import com.paymentoptions.pos.utils.getTransactionAmount
import com.paymentoptions.pos.ui.composables.BackHandler

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

            if ((selectedFilterKey == "ALL" || (selectedFilterKey == transaction.status?.uppercase() && transaction.TransactionType.uppercase() != "REFUND")
                        || selectedFilterKey == transaction.TransactionType.uppercase()))
                skip = false

            if (!skip) {
                val transactionListRecord = transaction.toTransactionListDataRecord()
                earningAmount += getTransactionAmount(transactionListRecord)

                TransactionSummary(
                    navController,
                    transactionListRecord
                )
            }
        }
        AppLogger.debug("total transactions : ${transactions.size} and earningAmount : $earningAmount")

        updateReceivalAmount(earningAmount)
    }
}
