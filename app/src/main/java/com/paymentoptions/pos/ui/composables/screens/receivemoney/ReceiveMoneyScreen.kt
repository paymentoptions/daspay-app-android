package com.paymentoptions.pos.ui.composables.screens.receivemoney

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout

@Composable
fun ReceiveMoneyScreen(navController: NavController) {
    SectionedLayout(
        navController = navController,
        bottomSectionContent = {
            BottomSectionContent(navController)
        },
        bottomSectionMaxHeightRatio = 0.95f,
        showBottomNavigationBar = false,
        showBackButton = true,
        defaultBottomSectionPaddingInDp = 0.dp,
    )
}