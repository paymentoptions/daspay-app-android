package com.paymentoptions.pos.ui.composables.screens.notifications

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout

@Composable
fun NotificationsScreen(navController: NavController) {
    SectionedLayout(
        navController = navController,
        bottomSectionMinHeightRatio = 0.3f,
        bottomSectionMaxHeightRatio = 0.9f,
        bottomSectionContent = {
            BottomSectionContent(navController)
        },
        enableBottomNavigationBar = false,
    )
}