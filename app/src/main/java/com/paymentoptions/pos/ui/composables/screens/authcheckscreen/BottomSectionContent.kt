package com.paymentoptions.pos.ui.composables.screens.authcheckscreen

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.SignInResponse
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.buttons.OutlinedButton
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.composables.screens.fingerprintscan.FingerprintScanScreen

@Composable
fun BottomSectionContent(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    var isLoading by remember { mutableStateOf(true) }
    var authDetails by remember { mutableStateOf<SignInResponse?>(null) }
    var isAuthenticated by remember { mutableStateOf(false) }
    val biometricStatus = SharedPreferences.getBiometricsStatus(context)

    LaunchedEffect(Unit) {
        isLoading = true
        authDetails = SharedPreferences.getAuthDetails(context)
        isAuthenticated = authDetails?.success == true
        isLoading = false
    }

    if (isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Checking auth status. Please wait...", textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))

            FilledButton(text = "Cancel and Quit", onClick = { activity?.finish() })
        }
    } else if (isAuthenticated) FingerprintScanScreen(
        navController = navController,
        onAuthSuccess = {
            navController.navigate(Screens.Dashboard.route) {
                popUpTo(Screens.SignIn.route) { inclusive = true }
            }
        },
        onAuthFailed = {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_LONG).show()
        },
        bypassBiometric = !biometricStatus
    )
    else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = "You are not signed in.", textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(20.dp))

            FilledButton(text = "Sign in", onClick = {
                SharedPreferences.clearSharedPreferences(context)
                navController.navigate(Screens.SignIn.route)
            }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(text = "Quit", onClick = {
                activity?.finish()
            }, modifier = Modifier.fillMaxWidth())
        }

    }
}
