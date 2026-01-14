package com.paymentoptions.pos.ui.composables.screens.splash

import android.os.Handler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import co.yml.charts.common.extensions.isNotNull
import coil3.compose.AsyncImage
import com.paymentoptions.pos.R
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.ui.composables.layout.simple.SimpleLayout
import com.paymentoptions.pos.ui.composables.navigation.Screens

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val signInResponse = SharedPreferences.getAuthDetails(context = context)

    if (signInResponse.isNotNull())
        navController.navigate(Screens.Dashboard.route){
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
        }
    else {
        Handler().postDelayed({
            navController.navigate(Screens.AuthCheck.route)
        }, 4000)

        SimpleLayout {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = R.drawable.daspay_loader_transparent,
                    contentDescription = "Loading Animation",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}