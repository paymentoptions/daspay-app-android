package com.paymentoptions.pos.ui.screens.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.daspay_loader_transparent
import coil3.compose.AsyncImage
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.storage.AppStorage
import com.paymentoptions.pos.ui.composables.layout.simple.SimpleLayout
import com.paymentoptions.pos.isLocationPermissionGranted
import com.paymentoptions.pos.rememberLocationPermissionLauncher
import com.paymentoptions.pos.ui.navigation.Screens
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun SplashScreen(navController: NavController, environment: String) {
    val signInResponse = DPStorageManager.getAuthDetails()

    var locationPermissionGranted by remember {
        mutableStateOf(isLocationPermissionGranted())
    }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLocationPermissionLauncher { granted ->
        locationPermissionGranted = granted
        permissionDenied = !locationPermissionGranted
    }

    // Request location permission on first launch
    LaunchedEffect(Unit) {
        if (!locationPermissionGranted) {
            permissionLauncher.launch()
        }
    }

    // Proceed with app logic only after permission is granted
    LaunchedEffect(locationPermissionGranted) {
        if (!locationPermissionGranted) return@LaunchedEffect

        try {
            val configInitialized = ConfigurationManager.initializeConfig(environment)
            if (configInitialized) {
                AppLogger.info("Config initialized successfully on splash")
            } else {
                AppLogger.warn("Config initialization failed on splash, using default base URL")
            }
        } catch (e: Exception) {
            AppLogger.error("Error initializing config on splash: ${e.message}")
        }

        if (signInResponse != null) {
            delay(1.seconds)
            // check if token is saved
            val token = AppStorage.tokenCode
            if(token?.isBlank() == true) {
                AppLogger.warn("Token is blank, navigating to AuthCheck")
                navController.navigate(Screens.AuthCheck.route) {
                    popUpTo(Screens.AuthCheck.route) { inclusive = true }
                }
            } else {
                navController.navigate(Screens.Dashboard.route) {
                    popUpTo(Screens.Dashboard.route) { inclusive = true }
                }
            }
        } else {
            delay(3.seconds)
            navController.navigate(Screens.AuthCheck.route)
        }
    }

    if (permissionDenied && !locationPermissionGranted) {
        // Show permission required screen
        SimpleLayout {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize().padding(24.dp)
            ) {
                Text(
                    text = "Location Permission Required",
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "This app requires location permission to verify your device region. The app cannot be used without it.",
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {
                    permissionLauncher.launch()
                }) {
                    Text("Grant Permission")
                }
            }
        }
    } else {
        // Show splash animation
        SimpleLayout {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = Res.drawable.daspay_loader_transparent,
                    contentDescription = "Loading Animation",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
