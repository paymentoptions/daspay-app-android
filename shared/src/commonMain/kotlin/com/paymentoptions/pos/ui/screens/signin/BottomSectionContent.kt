package com.paymentoptions.pos.ui.screens.signin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.analytics.AnalyticsHelper
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.getPushToken
import com.paymentoptions.pos.showToast
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.ApiHttpException
import com.paymentoptions.pos.network.SignInResponse
import com.paymentoptions.pos.network.endpoints.signIn
import com.paymentoptions.pos.storage.AppStorage
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.inputs.BasicTextInput
import com.paymentoptions.pos.ui.navigation.Screens
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.utils.getDeviceIdentifier
import com.paymentoptions.pos.utils.inProduction
import com.paymentoptions.pos.utils.isAndroid
import com.paymentoptions.pos.utils.parseApiErrorMessage
import com.paymentoptions.pos.utils.validation.validateEmail
import com.paymentoptions.pos.utils.validation.validatePassword
import kotlinx.coroutines.launch

private sealed class CredentialModel(
    val email: String = "",
    val password: String = "",
) {
    data object Robowah : CredentialModel(email = "rabowah650@fursee.com", password = "Test12345678@#")
    data object Empty : CredentialModel()
}

@Composable
fun SignInBottomSectionContent(navController: NavController, enableScrolling: Boolean = false) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var isLoading by remember { mutableStateOf(false) }

    val credentialModel = if (inProduction) CredentialModel.Empty else CredentialModel.Robowah
    val (savedEmail, savedPassword, _) = remember { DPStorageManager.getSavedCredentials() }

    val emailState = rememberTextFieldState(initialText = savedEmail ?: credentialModel.email)
    val passwordState = rememberTextFieldState(initialText = savedPassword ?: credentialModel.password)

    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }


    LaunchedEffect(emailState.text) {
        emailError = !validateEmail(emailState.text.toString())
    }

    LaunchedEffect(passwordState.text) {
        passwordError = !validatePassword(passwordState.text.toString())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState, enabled = enableScrolling),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Sign in",
            style = AppTheme.typography.screenTitle,
        )

        Spacer(modifier = Modifier.height(16.dp))

        BasicTextInput(
            state = emailState,
            label = "Enter your registered email",
            placeholder = "Enter Email",
            isError = emailError,
            modifier = Modifier.fillMaxWidth(),
            maxLength = 40,
        )

        Spacer(modifier = Modifier.height(8.dp))

        BasicTextInput(
            state = passwordState,
            label = "Enter your password",
            placeholder = "Enter Password",
            modifier = Modifier.fillMaxWidth(),
            isSecure = true,
            maxLength = 32,
        )

        Spacer(modifier = Modifier.height(22.dp))

        FilledButton(
            text = "Proceed",
            disabled = emailError || passwordError,
            isLoading = isLoading,
            onClick = {
                AnalyticsHelper.trackCriticalButtonClick(buttonName = "Proceed", screenName = "SignIn")

                scope.launch {
                    isLoading = true
                    try {
                        val signInResponse = signIn(
                            username = emailState.text.toString(),
                            password = passwordState.text.toString(),
                            deviceNumber = getDeviceIdentifier(),
                            isAndroid = isAndroid()
                        )

                        AppLogger.debug("signInResponse: $signInResponse")

                        if (signInResponse == null || signInResponse.success != true) {
                            showToast("Invalid Credentials")
                            return@launch
                        }

                        onSignInSuccess(
                            navController = navController,
                            signInResponse = signInResponse,
                            email = emailState.text.toString(),
                            password = passwordState.text.toString(),
                        )
                    } catch (e: Exception) {
                        val statusCode = (e as? ApiHttpException)?.statusCode
                        AnalyticsHelper.trackApiError(
                            endpoint = "signIn",
                            statusCode = statusCode,
                            message = e.message ?: "Sign in failed",
                            throwable = e,
                        )
                        AppLogger.error("signIn error", e)
                        showToast(parseApiErrorMessage(e, "Invalid Credentials"))
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(59.dp),
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

private suspend fun onSignInSuccess(
    navController: NavController,
    signInResponse: SignInResponse,
    email: String,
    password: String,
) {
    AnalyticsHelper.trackLogin(
        userId = signInResponse.data?.uid,
        merchantId = signInResponse.data?.subsidiaries?.firstOrNull(),
    )

    signInResponse.data?.subsidiaries?.firstOrNull()?.let { merchantId ->
        AnalyticsHelper.trackMerchantSelection(merchantId = merchantId)
    }

    DPStorageManager.saveCredentials(email, password)

    getPushToken()?.let { token ->
        DPStorageManager.saveFcmToken(token)
        AppLogger.debug("push token -> $token")
    }

    DPStorageManager.saveAuthDetails(signInResponse)
    AppStorage.merchantCountriesJson = signInResponse.data?.subsidiaries?.joinToString(",") ?: ""

    navController.navigate(Screens.Token.route)
}


