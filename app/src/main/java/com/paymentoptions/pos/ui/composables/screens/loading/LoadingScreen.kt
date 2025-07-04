package com.paymentoptions.pos.ui.composables.screens.loading

import android.os.Handler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables._components.images.LogoImage
import com.paymentoptions.pos.ui.composables.layout.simple.SimpleLayout
import com.paymentoptions.pos.ui.composables.navigation.Screens

@Composable
fun LoadingScreen(navController: NavController) {
    Handler().postDelayed({
        navController.navigate(Screens.AuthCheck.route)
    }, 1000)
    SimpleLayout {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            LogoImage()
        }
    }
}