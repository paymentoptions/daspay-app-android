package com.paymentoptions.pos.ui.composables.screens.signIn

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.messaging.FirebaseMessaging
import com.paymentoptions.pos.device.DPSharedPreferences
import com.paymentoptions.pos.device.DPSharedPreferences.saveFcmToken
import com.paymentoptions.pos.device.GeoRestrictionManager
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.SignInResponse
import com.paymentoptions.pos.services.apiService.TokenAutoRefresher
import com.paymentoptions.pos.services.apiService.endpoints.signIn
import com.paymentoptions.pos.services.analytics.AppAnalytics
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.inputs.BasicTextInput
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.utils.inProduction
import com.paymentoptions.pos.utils.parseApiErrorMessage
import com.paymentoptions.pos.utils.validation.validateEmail
import com.paymentoptions.pos.utils.validation.validatePassword
import kotlinx.coroutines.launch

sealed class CredentialModel(
    val email: String = "",
    val password: String = "",
    val otp: String = "123456",
) {
    object Ankit :
        CredentialModel(email = "ankitkambale097@myyahoo.com", password = "Test12345678@#")

    object Vijay : CredentialModel(email = "vijacip629@daupload.com", password = "Test123456789@#")
    object Kavita : CredentialModel(email = "kavitest15@ghunowa.com", password = "Kavios@12345678")
    object Robowah : CredentialModel(email = "rabowah650@fursee.com", password = "Test12345678@#")
    object Empty : CredentialModel()
}

@Composable
fun BottomSectionContent(navController: NavController, enableScrolling: Boolean = false) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val credentialModel = if (inProduction) CredentialModel.Empty else CredentialModel.Robowah

    val (savedEmail, savedPassword, otp) = remember { DPSharedPreferences.getSavedCredentials(context) }

    val emailState = rememberTextFieldState(initialText = savedEmail ?: credentialModel.email)
    var emailError by remember { mutableStateOf(false) }

    val passwordState =
        rememberTextFieldState(initialText = savedPassword ?: credentialModel.password)
    var passwordError by remember { mutableStateOf(false) }

//    val otpState =
//        rememberTextFieldState(initialText = if (inProduction) "" else credentialModel.otp)
   // var otpError by remember { mutableStateOf(false) }

    LaunchedEffect(emailState.text) {
        emailError = !validateEmail(emailState.text.toString())
    }

    LaunchedEffect(passwordState.text) {
        passwordError = !validatePassword(passwordState.text.toString())
    }

//    LaunchedEffect(otpState.text) {
//        otpError = !validateOtp(otpState.text.toString())
//    }

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
            maxLength = 40
        )

        Spacer(modifier = Modifier.height(8.dp))

        BasicTextInput(
            state = passwordState,
            label = "Enter your password",
            placeholder = "Enter Password",
            modifier = Modifier.fillMaxWidth(),
            isSecure = true,
            maxLength = 32
        )

        Spacer(modifier = Modifier.height(22.dp))

//        BasicTextInput(
//            state = otpState,
//            label = "Enter the 6-digit code sent on your registered email",
//            placeholder = "Enter OTP",
//            modifier = Modifier.fillMaxWidth(),
//            isSecure = true,
//            maxLength = 6
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))

//        Row(
//            horizontalArrangement = Arrangement.Center,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                "Didn't receive the code?",
//                color = purple50,
//                fontSize = 12.sp,
//                fontWeight = FontWeight.Medium
//            )
//            Spacer(modifier = Modifier.width(4.dp))
//            Text("Resend Code", textDecoration = TextDecoration.Underline, fontSize = 12.sp)
//        }
//
//        Spacer(modifier = Modifier.height(12.dp))

        FilledButton(
            text = "Proceed",
            disabled = emailError || passwordError,
            isLoading = isLoading,
            onClick = {
                AppAnalytics.criticalButtonClick(buttonName = "proceed_login", screen = "sign_in")
                scope.launch {
                    isLoading = true
                    var signInResponse: SignInResponse? = null

                    try {
                        signInResponse =
                            signIn(context, emailState.text.toString(), passwordState.text.toString())
                        AppLogger.debug("signInResponse: $signInResponse")

                        if (signInResponse == null) {
                            AppAnalytics.login(result = "failed", email = emailState.text.toString())
                            Toast.makeText(
                                context, "Invalid Credentials", Toast.LENGTH_LONG
                            ).show()
                        }

                        signInResponse?.let {
                            if (signInResponse.success) {
                                AppAnalytics.login(result = "success", email = emailState.text.toString())
                                DPSharedPreferences.saveCredentials(
                                    context,
                                    emailState.text.toString(),
                                    passwordState.text.toString()
                                )

                                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val token = task.result
                                        saveFcmToken(context, token)
                                        AppLogger.debug("mainActivity token --> $token")
                                    } else {
                                        AppLogger.error("mainActivity token fetching failed ${task.exception}")
                                    }
                                }

                                DPSharedPreferences.saveAuthDetails(context, signInResponse)


                                // Save merchant country for geo-restriction
                                GeoRestrictionManager.saveMerchantCountry(context, signInResponse.data.subsidiaries)

                                // Start token auto refresh after successful sign-in
                                TokenAutoRefresher.getInstance(context).onUserSignedIn()

                                navController.navigate(Screens.Token.route)
                            } else {
                                AppAnalytics.login(result = "failed", email = emailState.text.toString())
                            }
                        }
                    } catch (e: retrofit2.HttpException) {
                        val errorMessage = parseApiErrorMessage(e, "Invalid Credentials")
                        AppLogger.error("sign in  HTTP error $errorMessage")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                    catch (e: Exception){
                        AppLogger.error("signIn error: $e")
                        AppAnalytics.login(result = "failed", email = emailState.text.toString())
                        Toast.makeText(context, "Invalid Credentials", Toast.LENGTH_LONG).show()
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(59.dp)
                //.padding(bottom = 35.dp, top = 15.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}
