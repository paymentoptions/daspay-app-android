package com.paymentoptions.pos.ui.composables.screens.fingerprintscan

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.theme.red500

@Composable
fun FingerprintScanScreen(
    navController: NavController,
    onAuthSuccess: () -> Unit,
    onAuthFailed: () -> Unit,
    bypassBiometric: Boolean = false,
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var hasPrompted by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {

        if (!hasPrompted) {
            hasPrompted = true

            if (bypassBiometric) onAuthSuccess()
            else authenticateUser(
                context = context,
                lifecycleOwner = lifecycleOwner,
                onAuthSuccess = onAuthSuccess,
                onAuthFailed = { error ->
                    errorText = error
                    onAuthFailed()
                })
        }
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
                textAlign = TextAlign.Center
            )
        }
    }
}

fun authenticateUser(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    onAuthSuccess: () -> Unit,
    onAuthFailed: (String) -> Unit,
) {
    val biometricManager = BiometricManager.from(context)
    val activity = context as FragmentActivity

    if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
        onAuthFailed("Biometric not available")
        return
    }

    val executor = ContextCompat.getMainExecutor(context)

    val biometricPrompt = BiometricPrompt(
        activity, executor, object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onAuthSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onAuthFailed("Auth failed")
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onAuthFailed("Error: $errString")
            }

        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle("Authenticate")
        .setSubtitle("Verify your identity to proceed").setNegativeButtonText("Cancel").build()

    biometricPrompt.authenticate(promptInfo)
}