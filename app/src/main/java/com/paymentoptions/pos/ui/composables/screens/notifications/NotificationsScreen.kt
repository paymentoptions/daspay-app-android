package com.paymentoptions.pos.ui.composables.screens.notifications

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout

@Composable
fun NotificationsScreen(navController: NavController) {
    val enableScrollingInsideBottomSectionContent = true

    SectionedLayout(
        navController = navController,
        bottomSectionMinHeightRatio = 0.85f,
        bottomSectionMaxHeightRatio = 0.85f,
        alwaysShowLogo = false,
        enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent
    ) {
        BottomSectionContent(
            navController, enableScrolling = enableScrollingInsideBottomSectionContent
        )
    }
}