package com.paymentoptions.pos.ui.composables.screens.foodmenu

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout

@Composable
fun FoodMenuScreen(navController: NavController) {
    val enableScrollingInsideBottomSectionContent = true

    SectionedLayout(
        navController = navController,
        showBottomNavigationBar = false,
        showBackButton = true,
        defaultBottomSectionPaddingInDp = 0.dp,
        bottomSectionMinHeightRatio = 0.9f,
        enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent
    ) {
        BottomSectionContent(
            navController,
            enableScrolling = enableScrollingInsideBottomSectionContent
        )
    }
}