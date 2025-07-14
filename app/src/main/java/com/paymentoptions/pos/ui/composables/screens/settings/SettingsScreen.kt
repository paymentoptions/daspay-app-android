package com.paymentoptions.pos.ui.composables.screens.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout

@Composable
fun SettingsScreen(navController: NavController) {
    SectionedLayout(navController = navController, showBottomNavigationBar = false) {
        BottomSectionContent(navController)
    }
}