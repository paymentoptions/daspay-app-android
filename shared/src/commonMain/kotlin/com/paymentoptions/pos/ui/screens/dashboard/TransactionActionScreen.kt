package com.paymentoptions.pos.ui.screens.dashboard

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.paymentoptions.pos.network.TransactionListDataRecord
import com.paymentoptions.pos.utils.TransactionAction

@Composable
expect fun TransactionActionScreen(
    navController: NavController,
    transaction: TransactionListDataRecord,
    targetAction: TransactionAction,
)
