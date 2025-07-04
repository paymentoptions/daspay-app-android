package com.paymentoptions.pos.ui.composables.screens.authcheckscreen

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.composables.screens.fingerprintscan.FingerprintScanScreen


@Composable
fun AuthCheckScreen(navController: NavController) {
    val context = LocalContext.current
    val authDetails = SharedPreferences.getAuthDetails(context)
    val biometricStatus = SharedPreferences.getBiometricsStatus(context)

    if (authDetails?.success == true) FingerprintScanScreen(
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
        SharedPreferences.clearSharedPreferences(context)
        navController.navigate(Screens.SignIn.route) {
            popUpTo(0) { inclusive = true }
        }
    }
}
