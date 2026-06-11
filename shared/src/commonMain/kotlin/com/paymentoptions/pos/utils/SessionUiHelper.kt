package com.paymentoptions.pos.utils

import androidx.navigation.NavController
import com.paymentoptions.pos.showToast
import com.paymentoptions.pos.ui.navigation.Screens

fun showSessionExpiredAndNavigateToFingerprint(
    navController: NavController,
) {
    showToast("Session Expired")
    navController.navigate(Screens.FingerprintScan.route) {
        // Clear back stack to prevent going back to authenticated screens
        popUpTo(0) { inclusive = true }
    }
}
