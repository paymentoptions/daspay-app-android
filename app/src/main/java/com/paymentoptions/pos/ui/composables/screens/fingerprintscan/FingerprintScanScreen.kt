package com.paymentoptions.pos.ui.composables.screens.fingerprintscan

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.R
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
    var errorText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (bypassBiometric) onAuthSuccess()
        authenticateUser(
            context = context,
            onAuthSuccess = onAuthSuccess,
            onAuthFailed = { error ->
                errorText = error
                onAuthFailed()
            },
            onNoDeviceSecurity = {
                Toast.makeText(context, "Please enable device security using a PIN, fingerprint, or face lock.", Toast.LENGTH_SHORT).show()
            }
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
                textAlign = TextAlign.Center
            )
        }
    }
}

fun authenticateUser(
    context: Context,
    onAuthSuccess: () -> Unit,
    onAuthFailed: (String) -> Unit,
    onNoDeviceSecurity: (() -> Unit)
) {
    val activity = context as FragmentActivity
    val biometricManager = BiometricManager.from(context)
    val keyguardManager =
        context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

    val hasBiometric =
        biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)== BiometricManager.BIOMETRIC_SUCCESS

    val hasDeviceCredential = keyguardManager.isDeviceSecure

    println("hasBiometric: $hasBiometric, hasDeviceCredential: $hasDeviceCredential")

    if (hasBiometric || hasDeviceCredential) {

        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onAuthSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Optional: handle cancel / lockout
                    onAuthFailed("Auth failed : $errString")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onAuthFailed("Auth failed")
                }
            }
        )

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authenticate")
            .setSubtitle("Verify your identity to proceed")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            promptInfoBuilder.setAllowedAuthenticators(
                if (hasBiometric)
                    BiometricManager.Authenticators.BIOMETRIC_STRONG
                else
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
        } else {
            if (hasBiometric) {
                promptInfoBuilder.setNegativeButtonText("Cancel")
            } else {
                promptInfoBuilder.setDeviceCredentialAllowed(true)
            }
        }
        biometricPrompt.authenticate(promptInfoBuilder.build())
    } else {
        Toast.makeText(context, com.paymentoptions.pos.R.string.device_credential_missing, Toast.LENGTH_SHORT).show()
    }
}