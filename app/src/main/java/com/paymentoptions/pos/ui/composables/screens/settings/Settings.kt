package com.paymentoptions.pos.ui.composables.screens.settings


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout


@Composable
fun SettingsScreen(navController: NavController) {
    SectionedLayout(
        navController = navController, bottomSectionMaxHeightRatio = 0.7f, bottomSectionContent = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                BottomSectionContent(navController)
            }
        })
}