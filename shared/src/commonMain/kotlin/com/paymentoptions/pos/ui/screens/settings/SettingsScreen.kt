package com.paymentoptions.pos.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout

@Composable
fun SettingsScreen(navController: NavController) {
    SectionedLayout(navController = navController, bottomBarContent = BottomBarContent.NAVIGATION_BAR,//NOTHING
    ) {
        BottomSectionContent(navController)
    }
}