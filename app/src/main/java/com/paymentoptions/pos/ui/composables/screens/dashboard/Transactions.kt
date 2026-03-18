package com.paymentoptions.pos.ui.composables.screens.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import com.paymentoptions.pos.utils.getTransactionAmount
import java.time.OffsetDateTime

@Composable
fun Transactions(
    navController: NavController,
    transactions: List<TransactionListDataRecord>,
    updateReceivalAmount: (Float) -> Unit = {},
    lazyColumnState: LazyListState,
) {
    var selectedFilterKey by remember { mutableStateOf("ALL") }
    var longClickedTransactionId by remember { mutableStateOf("") }
    var backPressHandled by remember { mutableStateOf(false) }

    BackHandler(enabled = !backPressHandled) {
        longClickedTransactionId = ""
        backPressHandled = true
    }

    if (transactions.isEmpty()) NoData(text = "No transactions found")
    else LazyColumn(
        state = lazyColumnState,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.End,
        modifier = Modifier.fillMaxSize()
    ) {
        var earningAmountTodayOnly = 0.0f
        transactions.forEachIndexed { index, transaction ->
                earningAmountTodayOnly += getTransactionAmount(transaction)

                item {
                    TransactionSummary(
                        navController, transaction,
                    )
                }
        }

/*//        transactions.forEachIndexed { index, transaction ->
//
//            var skip = true
//
////            if ((selectedFilterKey == "ALL" || (selectedFilterKey == transaction.status.uppercase() && transaction.TransactionType.uppercase() != "REFUND")
////                        || selectedFilterKey == transaction.TransactionType.uppercase()))
////                skip = false
//
//            if (!skip) {
//                 earningAmountTodayOnly += getTransactionAmount(transaction)
//                AppLogger.debug("earningAmountTodayOnly = $earningAmountTodayOnly and transaction amount : ${getTransactionAmount(transaction)}")
//
////                if (transaction.TransactionType == "PURCHASE" && transaction.status == "SUCCESSFUL") {
////
////                    val transactionDate = OffsetDateTime.parse(transaction.Date)
////                    val today = OffsetDateTime.now()
////
////                    if (transactionDate.dayOfMonth == today.dayOfMonth && transactionDate.year == today.year)
////                        earningAmountTodayOnly += transaction.amount.toFloat()
////                }
//
//                item {
//                    TransactionSummary(
//                        navController, transaction,
////                        longClickedTransactionId, onLongClick = {
////
////                            if (transaction.TransactionType == "PURCHASE" && transaction.status == "SUCCESSFUL") {
////                                longClickedTransactionId =
////                                    if (longClickedTransactionId.isEmpty()) it
////                                    else if (longClickedTransactionId == it) "" else it
////                            } else {
////                                Toast.makeText(
////                                    context,
////                                "Txn details: ${transaction.status} | ${transaction.TransactionType}:",
////                                Toast.LENGTH_SHORT
////                            ).show()
////                            }
////                        }, onSwipeLeft = {
////                            if (transaction.TransactionType == "PURCHASE" && transaction.status == "SUCCESSFUL") {
////                                longClickedTransactionId = it
////                            } else {
////                            Toast.makeText(
////                                context,
////                                "Txn details: ${transaction.status} | ${transaction.TransactionType}:",
////                                Toast.LENGTH_SHORT
////                            ).show()
////                            }
////                        }, onSwipeRight = {
////                            longClickedTransactionId = ""
////                        },
////                        triggerListRefresh = { triggerListRefresh() }
//                    )
//                }
//            }
//        }*/
        updateReceivalAmount(earningAmountTodayOnly)
    }
}