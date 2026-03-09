package com.paymentoptions.pos.ui.composables.screens.splash

import android.os.Handler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import co.yml.charts.common.extensions.isNotNull
import coil3.compose.AsyncImage
import com.paymentoptions.pos.R
import com.paymentoptions.pos.device.DPSharedPreferences
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.ConfigurationManager
import com.paymentoptions.pos.ui.composables.layout.simple.SimpleLayout
import com.paymentoptions.pos.ui.composables.navigation.Screens
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val signInResponse = DPSharedPreferences.getAuthDetails(context = context)

    // Initialize config for authenticated users
    LaunchedEffect(Unit) {
        try {
            val configInitialized = ConfigurationManager.initializeConfig(context)
            if (configInitialized) {
                AppLogger.info("Config initialized successfully on splash")
            } else {
                AppLogger.warn("Config initialization failed on splash, using default base URL")
            }
        } catch (e: Exception) {
            AppLogger.error("Error initializing config on splash: ${e.message}")
        }
        if (signInResponse.isNotNull()) {
            delay(1000) // Brief delay to show splash
            navController.navigate(Screens.Dashboard.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    if (signInResponse.isNotNull()) {
        // Show loading while config is being initialized
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
    } else {
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