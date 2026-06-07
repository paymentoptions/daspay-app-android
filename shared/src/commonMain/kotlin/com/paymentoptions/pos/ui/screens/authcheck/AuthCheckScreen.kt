package com.paymentoptions.pos.ui.screens.authcheck

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.storage.AppStorage
import com.paymentoptions.pos.ui.navigation.Screens

@Composable
fun AuthCheckScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        val isLoggedIn = AppStorage.isLoggedIn()
        val isTokenVerified = AppStorage.tokenVerified

        if (isLoggedIn) {
            if (isTokenVerified) {
                // If logged in and token verified, go to fingerprint/dashboard
                navController.navigate(Screens.FingerprintScan.route) {
                    popUpTo(Screens.AuthCheck.route) { inclusive = true }
                }
            } else {
                // If logged in but token not verified, go to token verification
                navController.navigate(Screens.Token.route) {
                    popUpTo(Screens.AuthCheck.route) { inclusive = true }
                }
            }
        } else {
            // Not logged in, go to sign in
            AppStorage.clearAll()
            navController.navigate(Screens.SignIn.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Checking authentication...",
            textAlign = TextAlign.Center
        )
    }
}
