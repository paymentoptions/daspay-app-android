package com.paymentoptions.pos.ui.composables.screens.splash

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import co.yml.charts.common.extensions.isNotNull
import coil3.compose.AsyncImage
import com.paymentoptions.pos.BuildConfig
import com.paymentoptions.pos.R
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.storage.AppStorage
import com.paymentoptions.pos.ui.composables.layout.simple.SimpleLayout
import com.paymentoptions.pos.ui.composables.navigation.Screens
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val signInResponse = DPStorageManager.getAuthDetails()

    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationPermissionGranted = fineGranted || coarseGranted
        permissionDenied = !locationPermissionGranted
    }

    // Request location permission on first launch
    LaunchedEffect(Unit) {
        if (!locationPermissionGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Proceed with app logic only after permission is granted
    LaunchedEffect(locationPermissionGranted) {
        if (!locationPermissionGranted) return@LaunchedEffect

        try {
            val configInitialized = ConfigurationManager.initializeConfig(BuildConfig.ENVIRONMENT)
            if (configInitialized) {
                AppLogger.info("Config initialized successfully on splash")
            } else {
                AppLogger.warn("Config initialization failed on splash, using default base URL")
            }
        } catch (e: Exception) {
            AppLogger.error("Error initializing config on splash: ${e.message}")
        }

        if (signInResponse.isNotNull()) {
            delay(1000)
            // check if token is saved
            val token = AppStorage.tokenCode
            if(token?.isBlank() == true) {
                AppLogger.warn("Token is blank, navigating to AuthCheck")
                navController.navigate(Screens.AuthCheck.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            } else {
                navController.navigate(Screens.Dashboard.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        } else {
            delay(4000)
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
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
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
                    model = R.drawable.daspay_loader_transparent,
                    contentDescription = "Loading Animation",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}