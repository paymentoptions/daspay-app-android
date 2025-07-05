package com.paymentoptions.pos.ui.composables.screens.token

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout

@Composable
fun TokenScreen(navController: NavController) {
    SectionedLayout(
        navController = navController, bottomSectionContent = {
            BottomSectionContent(navController)
        }, enableBottomNavigationBar = false
    )
}