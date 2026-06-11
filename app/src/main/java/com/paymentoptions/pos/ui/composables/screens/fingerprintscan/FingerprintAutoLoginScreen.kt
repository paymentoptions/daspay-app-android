package com.paymentoptions.pos.ui.composables.screens.fingerprintscan

import android.app.KeyguardManager
import android.app.ProgressDialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.paymentoptions.pos.auth.AuthEventManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.endpoints.autoSignIn
import com.paymentoptions.pos.network.endpoints.completeDeviceRegistration
import com.paymentoptions.pos.network.endpoints.getExternalDeviceConfiguration
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.images.BackgroundImage
import com.paymentoptions.pos.ui.composables._components.images.LogoImage
import com.paymentoptions.pos.ui.composables._components.images.TapToPayImage
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.layout.sectioned.LOGO_HEIGHT_IN_DP
import com.paymentoptions.pos.ui.composables.layout.sectioned.LOGO_TOP_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.red500
import com.paymentoptions.pos.utils.getDeviceIdentifier
import com.paymentoptions.pos.utils.parseApiErrorMessage
import androidx.compose.foundation.layout.navigationBarsPadding

@Composable
fun FingerprintAutoLoginScreen(
    navController: NavController
) {
    val context = LocalContext.current
    var errorText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        authenticateUser(
            scope = scope,
            navController = navController,
            context = context
        )
    }

    // Use the branded background layout
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image (gradient blue)
        BackgroundImage(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
        )

        // Top Section with Logo and Tap to Pay image
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = LOGO_TOP_PADDING_IN_DP)
                .align(alignment = Alignment.TopCenter)
                .zIndex(2f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LOGO_HEIGHT_IN_DP)
            )

            Spacer(modifier = Modifier.height(20.dp))

            TapToPayImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )
        }

        // Bottom Section - Authentication Required card
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
            verticalArrangement = Arrangement.Center
        ) {
            // Added this button if pop is dismissed by system or user
            // it will be hidden behind pop up if pop up is showing
            FilledButton(
                text = "Authenticate",
                onClick = { authenticateUser(
                    scope = scope,
                    navController = navController,
                    context = context
                ) },
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
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Error text if any
            errorText?.let {
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
}

fun authenticateUser(
    scope: CoroutineScope,
    navController: NavController,
    context: Context
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
                    onAuthSuccess(scope, context, navController)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Optional: handle cancel / lockout
                    onAuthFailed(context,"Auth failed : $errString")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onAuthFailed(context, "Auth failed")
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
            // Only set negative button text if device credential is NOT allowed
            if (hasBiometric) {
                promptInfoBuilder.setNegativeButtonText("Cancel")
            }
        } else {
            if (hasBiometric) {
                promptInfoBuilder.setNegativeButtonText("Cancel")
            } else {
                promptInfoBuilder.setDeviceCredentialAllowed(true)
                // Do NOT set negative button text if device credential is allowed
            }
        }

        biometricPrompt.authenticate(promptInfoBuilder.build())

    } else {
        // 🚨 No security configured
        Toast.makeText(context, com.paymentoptions.pos.R.string.device_credential_missing, Toast.LENGTH_SHORT).show()
        navController.navigate(Screens.Token.route) {
            popUpTo(Screens.AuthCheck.route) { inclusive = true }
        }
    }
}

private fun onAuthFailed(context: Context, string: String) {
    Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
}

private fun onAuthSuccess(
    scope: CoroutineScope,
    context: FragmentActivity,
    navController: NavController
) {
    scope.launch(Dispatchers.IO) {
        // Use a modern progress indicator in production; suppress deprecated warning for now
        val progressDialog = withContext(Dispatchers.Main) {
            @Suppress("DEPRECATION")
            ProgressDialog(context).apply {
                setMessage("Signing in...")
                setCancelable(false)
                show()
            }
        }
        try {
            val authCredentials = autoSignIn(getDeviceIdentifier())
            if (authCredentials != null) {
                val authDetails = DPStorageManager.getSavedCredentials()
                val otp = authDetails.third
                var errorMessage = ""

                Log.d(
                    "DEBUG_TOKEN",
                    "Step 1: Button clicked. Starting process. otp: $otp"
                )

                // --- First API Call ---
                if (otp == null) return@launch
                completeDeviceRegistration(otp, getDeviceIdentifier()).onSuccess { response ->
                    Log.d(
                        "DEBUG_TOKEN",
                        "Step 2: completeDeviceRegistration SUCCEEDED. Response: $response"
                    )
                    if (response.success || response.message.contains("Device already registered")) {

                        // --- Second API Call ---
                        AppLogger.debug(
                            "DEBUG_TOKEN",
                            "Step 3: Proceeding to get external device configuration."
                        )

                        DPStorageManager.saveTokenStatus(
                            tokenCode = otp, isVerified = true
                        )

                        getExternalDeviceConfiguration(
                            otp, getDeviceIdentifier()
                        ).onSuccess { configResponse ->
                            AppLogger.debug(
                                "DEBUG_TOKEN",
                                "Step 4: getExternalDeviceConfiguration SUCCEEDED. Response: $configResponse"
                            )
                            DPStorageManager.saveDeviceConfiguration(
                                configResponse
                            )
                        }.onFailure { exception ->
                            AppLogger.error(
                                "DEBUG_TOKEN",
                                "Step 4 FAILED: getExternalDeviceConfiguration.",
                                exception
                            )
                            errorMessage = parseApiErrorMessage(exception, "Failed to fetch configuration")
                        }
                    } else {
                        AppLogger.warn(
                            "DEBUG_TOKEN",
                            "Step 2 WARNING: API reported not successful. Message: ${response.message}"
                        )
                        errorMessage = response.message
                    }
                }.onFailure { exception ->
                    val exceptionMessage = parseApiErrorMessage(exception, "Registration failed")
                    AppLogger.e("DEBUG_TOKEN", "Step 2 FAILED: completeDeviceRegistration. Msg: $exceptionMessage", exception.toString())

                    if (exceptionMessage == "Device already registered") {
                        getExternalDeviceConfiguration(otp, getDeviceIdentifier()).onSuccess { configResponse ->
                            DPStorageManager.saveTokenStatus(
                                tokenCode = otp,
                                isVerified = true
                            )

                            AppLogger.debug(
                                "DEBUG_TOKEN",
                                "Step 4: getExternalDeviceConfiguration SUCCEEDED. Response: $configResponse"
                            )
                            DPStorageManager.saveDeviceConfiguration(
                                configResponse
                            )
                        }.onFailure { innerException ->
                            AppLogger.error(
                                "DEBUG_TOKEN",
                                "Step 4 FAILED: getExternalDeviceConfiguration.",
                                innerException
                            )
                            errorMessage = parseApiErrorMessage(innerException, "Failed to fetch configuration")
                        }
                    } else if (exceptionMessage.lowercase().contains("unauthorized")) {
                        AppLogger.debug("Auto login failing, fatal exception")
                        withContext(Dispatchers.Main) {
                            autoSignInFailed(context, navController)
                        }
                    } else {
                        errorMessage = exceptionMessage
                    }
                }
                AppLogger.debug("DEBUG_TOKEN", "Step 5: Process finished.")

                AppLogger.debug("DEBUG_TOKEN", "Error message: $errorMessage")


                // go to home screen if auto sign-in was successful
                if (errorMessage.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Auto sign-in is successful", Toast.LENGTH_SHORT)
                            .show()
                        navController.navigate(Screens.Dashboard.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        autoSignInFailed(context, navController)
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    autoSignInFailed(context, navController)
                }
            }
        } catch (e: Exception) {
            AppLogger.error("Auto login failing, fatal exception $e")
            withContext(Dispatchers.Main) {
                autoSignInFailed(context, navController)
            }
        } finally {
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
            }
        }
    }
}

private fun autoSignInFailed(
    context: FragmentActivity,
    navController: NavController
) {
    // Auto sign-in failed - notify the system
    Toast.makeText(context, "Auto sign-in has failed, Please enter credentials again", Toast.LENGTH_SHORT).show()
     AuthEventManager.requireManualSignIn()
    //TODO
    DPStorageManager.clearSharedPreferences()
    navController.navigate(Screens.AuthCheck.route) {
        popUpTo(0) { inclusive = true }
    }
}