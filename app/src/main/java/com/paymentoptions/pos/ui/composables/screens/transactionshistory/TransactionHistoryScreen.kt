package com.paymentoptions.pos.ui.composables.screens.transactionshistory


import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout


@Composable
fun TransactionHistoryScreen(navController: NavController) {
    val enableScrollingInsideBottomSectionContent = true

    SectionedLayout(
        navController = navController,
        bottomSectionMinHeightRatio = 0.9f,
        bottomSectionMaxHeightRatio = 0.9f,
        bottomSectionPaddingInDp = 0.dp,
        alwaysShowLogo = false,
        bottomBarContent = BottomBarContent.TOGGLE_BUTTON,
        enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent
    ) {
        BottomSectionContent(
            navController, enableScrolling = enableScrollingInsideBottomSectionContent
        )
    }
}