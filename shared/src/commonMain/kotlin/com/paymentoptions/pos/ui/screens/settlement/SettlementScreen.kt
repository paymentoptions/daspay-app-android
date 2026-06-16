package com.paymentoptions.pos.ui.screens.settlement

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout


@Composable
fun SettlementScreen(navController: NavController) {

    val enableScrollingInsideBottomSectionContent = true

    SectionedLayout(
        navController = navController,
        bottomSectionMinHeightRatio = 0.9f,
        bottomSectionMaxHeightRatio = 0.9f,
        bottomBarContent = BottomBarContent.NAVIGATION_BAR,
        bottomSectionPaddingInDp = 0.dp,
        enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent,
    ) {
        BottomSectionContent(
            navController,
            enableScrolling = enableScrollingInsideBottomSectionContent
        )
    }
}
