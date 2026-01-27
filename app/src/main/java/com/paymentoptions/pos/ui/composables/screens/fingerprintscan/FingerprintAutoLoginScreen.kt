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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.AuthEventManager
import com.paymentoptions.pos.services.apiService.TokenAutoRefresher
import com.paymentoptions.pos.services.apiService.endpoints.autoSignIn
import com.paymentoptions.pos.services.apiService.endpoints.completeDeviceRegistration
import com.paymentoptions.pos.services.apiService.endpoints.getExternalDeviceConfiguration
import com.paymentoptions.pos.ui.composables._components.images.BackgroundImage
import com.paymentoptions.pos.ui.composables._components.images.LogoImage
import com.paymentoptions.pos.ui.composables._components.images.TapToPayImage
import com.paymentoptions.pos.ui.composables.layout.sectioned.LOGO_HEIGHT_IN_DP
import com.paymentoptions.pos.ui.composables.layout.sectioned.LOGO_TOP_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.red500

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
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
                .padding(vertical = 40.dp, horizontal = 24.dp)
                .zIndex(3f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
        } else {
            if (hasBiometric) {
                promptInfoBuilder.setNegativeButtonText("Cancel")
            } else {
                promptInfoBuilder.setDeviceCredentialAllowed(true)
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
            val authCredentials = autoSignIn(context) // Remove redundant semicolon
            if (authCredentials != null) {
                val authDetails = SharedPreferences.getSavedCredentials(context)
                val otp = authDetails.third
                var errorMessage = ""

                Log.d(
                    "DEBUG_TOKEN",
                    "Step 1: Button clicked. Starting process. otp: $otp"
                )

                // --- First API Call ---
                if (otp == null) return@launch
                completeDeviceRegistration(context, otp).onSuccess { response ->
                    Log.d(
                        "DEBUG_TOKEN",
                        "Step 2: completeDeviceRegistration SUCCEEDED. Response: $response"
                    )
                    if (response.success || response.message.contains("Device already registered")) {

                        // --- Second API Call ---
                        Log.d(
                            "DEBUG_TOKEN",
                            "Step 3: Proceeding to get external device configuration."
                        )

                        SharedPreferences.saveTokenStatus(
                            context = context, tokenCode = otp, isVerified = true
                        )

                        getExternalDeviceConfiguration(
                            context, otp
                        ).onSuccess { configResponse ->
                            Log.d(
                                "DEBUG_TOKEN",
                                "Step 4: getExternalDeviceConfiguration SUCCEEDED. Response: $configResponse"
                            )
                            SharedPreferences.saveDeviceConfiguration(
                                context, configResponse
                            )
                        }.onFailure { exception ->
                            Log.e(
                                "DEBUG_TOKEN",
                                "Step 4 FAILED: getExternalDeviceConfiguration.",
                                exception
                            )
                            errorMessage =
                                exception.message ?: "Failed to fetch configuration"
                        }
                    } else {
                        Log.w(
                            "DEBUG_TOKEN",
                            "Step 2 WARNING: API reported not successful. Message: ${response.message}"
                        )
                        errorMessage = response.message
                    }
                }.onFailure { exception ->
                    //if (json)
                    try {
                        val jsonPart = exception.message?.substringAfter(":")?.trim()
                        val jsonObject =
                            if (jsonPart != null) JSONObject(jsonPart) else null // Fix type mismatch
                        val exceptionMessage = jsonObject?.getString("message") ?: ""

                        Log.e(
                            "Step 2 FAILED: completeDeviceRegistration.",
                            exceptionMessage
                        )
                        if (exceptionMessage == "Device already registered") {
                            getExternalDeviceConfiguration(
                                context, otp
                            ).onSuccess { configResponse ->

                                SharedPreferences.saveTokenStatus(
                                    context = context,
                                    tokenCode = otp,
                                    isVerified = true
                                )

                                Log.d(
                                    "DEBUG_TOKEN",
                                    "Step 4: getExternalDeviceConfiguration SUCCEEDED. Response: $configResponse"
                                )
                                SharedPreferences.saveDeviceConfiguration(
                                    context, configResponse
                                )
                            }.onFailure { exception ->
                                Log.e(
                                    "DEBUG_TOKEN",
                                    "Step 4 FAILED: getExternalDeviceConfiguration.",
                                    exception
                                )
                                errorMessage =
                                    exception.message ?: "Failed to fetch configuration"
                            }
                        } else if (exceptionMessage.lowercase()
                                .contains("unauthorized")
                        ) {
                            println("Auto login failing, fatal exception")
                            withContext(Dispatchers.Main) {
                                autoSignInFailed(context, navController)
                            }
                        } else {
                            errorMessage =
                                exceptionMessage ?: "An unknown error occurred"
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message.toString()
                    }
                }
                Log.d("DEBUG_TOKEN", "Step 5: Process finished.")

                Log.d("DEBUG_TOKEN", "Error message: $errorMessage")


                // go to home screen if auto sign-in was successful
                if (errorMessage.isEmpty()) {
                    // Start token auto refresh after successful auto sign-in
                    TokenAutoRefresher.getInstance(context).onUserSignedIn()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Auto sign-in successful", Toast.LENGTH_SHORT)
                            .show()
                        navController.navigate(Screens.Dashboard.route) {
                            popUpTo(Screens.AuthCheck.route) { inclusive = true }
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
            println("Auto login failing, fatal exception $e")
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
    Toast.makeText(context, "Auto sign-in failed", Toast.LENGTH_SHORT).show()
    AuthEventManager.onAutoSignInFailed()
    SharedPreferences.clearSharedPreferences(context)
    navController.navigate(Screens.AuthCheck.route) {
        popUpTo(0) { inclusive = true }
    }
}