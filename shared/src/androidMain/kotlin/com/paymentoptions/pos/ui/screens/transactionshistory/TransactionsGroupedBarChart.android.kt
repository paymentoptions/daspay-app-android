package com.paymentoptions.pos.ui.screens.transactionshistory

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.paymentoptions.pos.network.InsightsResponseDataRecord

@Composable
actual fun TransactionsGroupedBarChart(
    navController: NavController,
    transactions: List<InsightsResponseDataRecord>,
    currency: String,
    updateReceivalAmount: (Float) -> Unit,
    startDateMillis: Long,
    endDateMillis: Long,
) {
    Transactions(
        navController = navController,
        transactions = transactions,
        updateReceivalAmount = updateReceivalAmount,
    )
}
