package com.paymentoptions.pos.ui.composables.screens.transactionshistory


import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout


@Composable
fun TransactionHistoryScreen(navController: NavController) {
    SectionedLayout(
        navController = navController, bottomSectionContent = {
                BottomSectionContent(navController)
        })
}