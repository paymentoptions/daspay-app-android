package com.paymentoptions.pos.ui.composables.screens.signIn

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.messaging.FirebaseMessaging
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.device.SharedPreferences.Companion.saveFcmToken
import com.paymentoptions.pos.services.apiService.SignInResponse
import com.paymentoptions.pos.services.apiService.endpoints.signIn
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.inputs.BasicTextInput
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.utils.validation.validateEmail
import com.paymentoptions.pos.utils.validation.validateOtp
import com.paymentoptions.pos.utils.validation.validatePassword
import kotlinx.coroutines.launch

sealed class CredentialModel(
    val email: String,
    val password: String,
) {
    object Ankit :
        CredentialModel(email = "ankitkambale097@myyahoo.com", password = "Test12345678@#")

    object Vijay : CredentialModel(email = "vijacip629@daupload.com", password = "Test123456789@#")
    object Kavita : CredentialModel(email = "kavitest15@ghunowa.com", password = "Kavios@12345678")
    object Robowah : CredentialModel(email = "rabowah650@fursee.com", password = "Test12345678@#")
    object Empty : CredentialModel(email = "", password = "")
}

@Composable
fun BottomSectionContent(navController: NavController, enableScrolling: Boolean = false) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

//    val credentialModel = CredentialModel.Empty
    val credentialModel = CredentialModel.Robowah

    val emailState = rememberTextFieldState(initialText = credentialModel.email)
    var emailError by remember { mutableStateOf(false) }

    val passwordState = rememberTextFieldState(initialText = credentialModel.password)
    var passwordError by remember { mutableStateOf(false) }

    val otpState = rememberTextFieldState()
    var otpError by remember { mutableStateOf(false) }

    LaunchedEffect(emailState.text) {
        emailError = !validateEmail(emailState.text.toString())
    }

    LaunchedEffect(passwordState.text) {
        passwordError = !validatePassword(passwordState.text.toString())
    }

    LaunchedEffect(otpState.text) {
        otpError = !validateOtp(otpState.text.toString())
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

        Spacer(modifier = Modifier.height(8.dp))

        BasicTextInput(
            state = otpState,
            label = "Enter the 6-digit code sent on your registered email",
            placeholder = "Enter OTP",
            modifier = Modifier.fillMaxWidth(),
            isSecure = true,
            maxLength = 6
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Didn't receive the code?",
                color = purple50,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Resend Code", textDecoration = TextDecoration.Underline, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        FilledButton(
            text = "Proceed",
            disabled = emailError || passwordError || otpError,
            isLoading = isLoading,
            onClick = {
                scope.launch {
                    isLoading = true
                    var signInResponse: SignInResponse? = null

                    try {
                        signInResponse =
                            signIn(emailState.text.toString(), passwordState.text.toString())
                        println("signInResponse: $signInResponse")

                        if (signInResponse == null) {
                            Toast.makeText(
                                context, "Invalid Credentials", Toast.LENGTH_LONG
                            ).show()
                        }

                        signInResponse?.let {
                            if (signInResponse.success) {

                                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val token = task.result
                                        saveFcmToken(context, token)
                                        println("mainActivity token --> $token")
                                    } else {
                                        println("mainActivity token fetching failed ${task.exception}")
                                    }
                                }

                                SharedPreferences.saveAuthDetails(context, signInResponse)
                                navController.navigate(Screens.Token.route)
                            }
                        }
                    } catch (_: Exception) {
                        Toast.makeText(context, "Invalid Credentials", Toast.LENGTH_LONG).show()
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(59.dp)
        )
    }
}
