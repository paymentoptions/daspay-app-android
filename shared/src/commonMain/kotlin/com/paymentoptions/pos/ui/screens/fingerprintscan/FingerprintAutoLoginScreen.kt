package com.paymentoptions.pos.ui.screens.fingerprintscan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.paymentoptions.pos.getBiometricAuthenticator
import com.paymentoptions.pos.showToast
import com.paymentoptions.pos.auth.AuthEventManager
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.endpoints.autoSignIn
import com.paymentoptions.pos.network.endpoints.completeDeviceRegistration
import com.paymentoptions.pos.network.endpoints.getExternalDeviceConfiguration
import com.paymentoptions.pos.storage.AppStorage
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.images.BackgroundImage
import com.paymentoptions.pos.ui.composables._components.images.LogoImage
import com.paymentoptions.pos.ui.composables._components.images.TapToPayImage
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.layout.sectioned.LOGO_HEIGHT_IN_DP
import com.paymentoptions.pos.ui.composables.layout.sectioned.LOGO_TOP_PADDING_IN_DP
import com.paymentoptions.pos.ui.navigation.Screens
import com.paymentoptions.pos.ui.theme.red500
import com.paymentoptions.pos.utils.getDeviceIdentifier
import com.paymentoptions.pos.utils.parseApiErrorMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FingerprintAutoLoginScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val biometricAuthenticator = remember { getBiometricAuthenticator() }

    var errorText by remember { mutableStateOf<String?>(null) }
    var isSigningIn by remember { mutableStateOf(false) }

    fun startAuthFlow() {
        errorText = null

        if (!biometricAuthenticator.isAvailable()) {
            showToast("Please enable device security using a PIN, fingerprint, or face lock.")
            navController.navigate(Screens.Token.route) {
                popUpTo(Screens.AuthCheck.route) { inclusive = true }
            }
            return
        }

        biometricAuthenticator.authenticate(
            title = "Authenticate",
            subtitle = "Verify your identity to proceed",
            onSuccess = {
                scope.launch {
                    isSigningIn = true
                    val outcome = withContext(Dispatchers.Default) { performAutoSignIn() }
                    when (outcome) {
                        AutoSignInOutcome.Success -> {
                            showToast("Auto sign-in is successful")
                            navController.navigate(Screens.Dashboard.route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }

                        AutoSignInOutcome.RequireManualSignIn -> {
                            handleAutoSignInFailure(navController)
                        }

                        is AutoSignInOutcome.Error -> {
                            errorText = "failed: ${outcome.message}"
                            handleAutoSignInFailure(navController)
                        }
                    }
                    isSigningIn = false
                }
            },
            onError = { message ->
                errorText = "failed: $message"
                showToast("Auth failed: $message")
            },
        )
    }

    LaunchedEffect(Unit) {
        startAuthFlow()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundImage(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = LOGO_TOP_PADDING_IN_DP)
                .align(alignment = Alignment.TopCenter)
                .zIndex(2f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LogoImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LOGO_HEIGHT_IN_DP),
            )

            Spacer(modifier = Modifier.height(20.dp))

            TapToPayImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment = Alignment.BottomCenter)
                .navigationBarsPadding()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
                .padding(vertical = 40.dp, horizontal = 24.dp)
                .zIndex(3f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            FilledButton(
                text = if (isSigningIn) "Signing in..." else "Authenticate",
                onClick = {
                    if (!isSigningIn) startAuthFlow()
                },
                modifier = Modifier
                    .padding(horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP)
                    .width(160.dp)
                    .height(35.dp)
                    .scale(0.8f),
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Authentication Required",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(20.dp))

            errorText?.let {
                Text(
                    text = "Biometric $it",
                    color = red500,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 20.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (isSigningIn) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
                    .zIndex(4f),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

private suspend fun performAutoSignIn(): AutoSignInOutcome {
    return try {
        val authCredentials = autoSignIn(getDeviceIdentifier()) ?: return AutoSignInOutcome.RequireManualSignIn
        if (authCredentials.success != true) return AutoSignInOutcome.RequireManualSignIn

        val otp = DPStorageManager.getSavedCredentials().third ?: return AutoSignInOutcome.RequireManualSignIn
        var errorMessage = ""
        var requireManualSignIn = false

        completeDeviceRegistration(otp, getDeviceIdentifier())
            .onSuccess { response ->
                if (response.success || response.message.contains("Device already registered")) {
                    DPStorageManager.saveTokenStatus(tokenCode = otp, isVerified = true)

                    getExternalDeviceConfiguration(otp, getDeviceIdentifier())
                        .onSuccess { configResponse ->
                            DPStorageManager.saveDeviceConfiguration(configResponse)
                        }
                        .onFailure { exception ->
                            errorMessage = parseApiErrorMessage(exception, "Failed to fetch configuration")
                        }
                } else {
                    errorMessage = response.message
                }
            }
            .onFailure { exception ->
                val exceptionMessage = parseApiErrorMessage(exception, "Registration failed")
                AppLogger.error("AUTO_SIGN_IN", "completeDeviceRegistration failed", exception)

                if (exceptionMessage == "Device already registered") {
                    getExternalDeviceConfiguration(otp, getDeviceIdentifier())
                        .onSuccess { configResponse ->
                            DPStorageManager.saveTokenStatus(tokenCode = otp, isVerified = true)
                            DPStorageManager.saveDeviceConfiguration(configResponse)
                        }
                        .onFailure { innerException ->
                            errorMessage = parseApiErrorMessage(innerException, "Failed to fetch configuration")
                        }
                } else if (exceptionMessage.lowercase().contains("unauthorized")) {
                    requireManualSignIn = true
                } else {
                    errorMessage = exceptionMessage
                }
            }

        when {
            requireManualSignIn -> AutoSignInOutcome.RequireManualSignIn
            errorMessage.isNotEmpty() -> AutoSignInOutcome.Error(errorMessage)
            else -> AutoSignInOutcome.Success
        }
    } catch (e: Exception) {
        AppLogger.error("AUTO_SIGN_IN", "Auto sign-in fatal exception", e)
        AutoSignInOutcome.RequireManualSignIn
    }
}

private fun handleAutoSignInFailure(navController: NavController) {
    showToast("Auto sign-in failed, please enter credentials again")
    AuthEventManager.requireManualSignIn()
    AppStorage.clearAll()
    navController.navigate(Screens.AuthCheck.route) {
        popUpTo(0) { inclusive = true }
    }
}

private sealed interface AutoSignInOutcome {
    data object Success : AutoSignInOutcome
    data object RequireManualSignIn : AutoSignInOutcome
    data class Error(val message: String) : AutoSignInOutcome
}



