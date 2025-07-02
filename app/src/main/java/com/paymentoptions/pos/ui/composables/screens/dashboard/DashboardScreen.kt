package com.paymentoptions.pos.ui.composables.screens.dashboard

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout

@Composable
fun DashboardScreen(navController: NavController) {
    SectionedLayout(
        navController = navController, bottomSectionContent = {
            BottomSectionContent(navController)
        })
}