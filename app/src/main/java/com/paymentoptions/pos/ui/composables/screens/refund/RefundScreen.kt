package com.paymentoptions.pos.ui.composables.screens.refund

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout

@Composable
fun RefundScreen(navController: NavController) {
    val enableScrollingInsideBottomSectionContent = true

    SectionedLayout(
        navController = navController,
        bottomSectionPaddingInDp = 0.dp,
        bottomSectionMinHeightRatio = 0.8f,
        bottomBarContent = BottomBarContent.TOGGLE_BUTTON,
        enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent
    ) {
        BottomSectionContent(
            navController,
            enableScrolling = enableScrollingInsideBottomSectionContent
        )
    }
}