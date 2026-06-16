package com.paymentoptions.pos.ui.screens.fingerprintscan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.getBiometricAuthenticator
import com.paymentoptions.pos.showToast
import com.paymentoptions.pos.ui.theme.red500

@Composable
fun FingerprintScanScreen(
    navController: NavController,
    onAuthSuccess: () -> Unit,
    onAuthFailed: () -> Unit,
    bypassBiometric: Boolean = false,
) {
    val biometricAuthenticator = remember { getBiometricAuthenticator() }
    var errorText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (bypassBiometric) {
            onAuthSuccess()
            return@LaunchedEffect
        }

        if (!biometricAuthenticator.isAvailable()) {
            showToast("Please enable device security using a PIN, fingerprint, or face lock.")
            onAuthFailed()
            return@LaunchedEffect
        }

        biometricAuthenticator.authenticate(
            title = "Authenticate",
            subtitle = "Verify your identity to proceed",
            onSuccess = onAuthSuccess,
            onError = { error ->
                errorText = error
                onAuthFailed()
            },
        )
    }

    errorText?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                "Biometric $it",
                color = red500,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 20.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}
