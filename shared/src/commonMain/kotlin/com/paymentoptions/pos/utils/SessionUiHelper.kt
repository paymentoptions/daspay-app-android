package com.paymentoptions.pos.utils

import androidx.navigation.NavController
import com.paymentoptions.pos.platformLogError
import com.paymentoptions.pos.showToast
import com.paymentoptions.pos.ui.navigation.Screens

fun showSessionExpiredAndNavigateToFingerprint(
    navController: NavController,
) {
    platformLogError("SESSION_ERROR","Session expired. " +
            "Navigating to fingerprint scan screen.")
    showToast("Your session has expired. Please log in again to continue." )
    navController.navigate(Screens.FingerprintScan.route) {
        // Clear back stack to prevent going back to authenticated screens
        popUpTo(0) { inclusive = true }
    }
}
