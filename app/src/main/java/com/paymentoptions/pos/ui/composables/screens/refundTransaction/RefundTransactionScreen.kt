package com.paymentoptions.pos.ui.composables.screens.refundTransaction

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout

@Composable
fun RefundTransactionScreen(navController: NavController) {
    val enableScrollingInsideBottomSectionContent = false

    SectionedLayout(
        navController = navController,
        showBottomNavigationBar = false,
        showBackButton = true,
        defaultBottomSectionPaddingInDp = 0.dp,
        imageBelowLogo = { },
        enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent
    ) {
        BottomSectionContent(
            navController,
            enableScrolling = enableScrollingInsideBottomSectionContent
        )
    }
}