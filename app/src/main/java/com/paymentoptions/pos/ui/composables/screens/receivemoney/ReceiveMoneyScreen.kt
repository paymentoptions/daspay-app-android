package com.paymentoptions.pos.ui.composables.screens.receivemoney

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout

@Composable
fun ReceiveMoneyScreen(navController: NavController) {
    val enableScrollingInsideBottomSectionContent = true

    SectionedLayout(
        navController = navController,
        bottomSectionMaxHeightRatio = 0.95f,
        bottomBarContent = BottomBarContent.TOGGLE_BUTTON,
        bottomSectionPaddingInDp = 0.dp,
        enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent
    ) {
        BottomSectionContent(
            navController,
            enableScrolling = enableScrollingInsideBottomSectionContent
        )
    }
}