package com.paymentoptions.pos.ui.composables.screens.signIn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout

@Composable
fun SignInScreen(navController: NavController) {
    SectionedLayout(
        navController = navController, bottomSectionMaxHeightRatio = 0.65f, bottomSectionContent = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxSize()
            ) {
                BottomSectionContent(navController)
            }
        }, enableBottomNavigationBar = false
    )
}