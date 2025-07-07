package com.paymentoptions.pos.ui.composables.screens.signIn

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.messaging.FirebaseMessaging
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.device.SharedPreferences.Companion.saveFcmToken
import com.paymentoptions.pos.services.apiService.SignInResponse
import com.paymentoptions.pos.services.apiService.endpoints.signIn
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.inputs.BasicTextInput
import com.paymentoptions.pos.ui.composables._components.inputs.PasswordInput
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.utils.validation.validateEmail
import com.paymentoptions.pos.utils.validation.validateOtp
import com.paymentoptions.pos.utils.validation.validatePassword
import kotlinx.coroutines.launch


data class Credentials(
    val email: String,
    val password: String,
)

@Composable
fun BottomSectionContent(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val allCredentials = listOf(
        Credentials(email = "ankitkambale097@myyahoo.com", password = "Test12345678@#"),
        Credentials(email = "vijacip629@daupload.com", password = "Test123456789@#"),
        Credentials(email = "kavitest15@ghunowa.com", password = "Kavios@12345678")
    )

    val credentials = allCredentials[1]

    var email by remember { mutableStateOf(credentials.email) }
    var password by remember { mutableStateOf(credentials.password) }

    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    var otp by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf(false) }

    var showDrawer by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Sign in",
            style = AppTheme.typography.screenTitle,
        )

        Spacer(modifier = Modifier.height(16.dp))

        BasicTextInput(
            value = email,
            label = "Registered Email",
            placeholder = "Enter your registered Email",
            onChange = {
                email = it
                emailError = !validateEmail(email)
            },
            error = emailError,
            errorText = "Invalid email",
            modifier = Modifier.fillMaxWidth()
        )

        PasswordInput(
            value = password,
            label = "Password",
            placeholder = "Enter your password",
            onChange = {
                password = it
                passwordError = !validatePassword(password)
            },
            error = passwordError,
            modifier = Modifier.fillMaxWidth(),
            visible = passwordVisible,
            onClickTrailingIcon = {
                passwordVisible = !passwordVisible
            })

        BasicTextInput(
            value = otp, label = "OTP", placeholder = "6-digit OTP", {
                otp = it
                otpError = !validateOtp(otp)
            }, modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Didn't receive the code?")

            TextButton(onClick = { /* TODO: resend OTP */ }) {
                Text("Resend Code", textDecoration = TextDecoration.Underline)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        FilledButton(
            text = "Proceed",
            onClick = {
                scope.launch {
                    isLoading = true
                    var signInResponse: SignInResponse? = null

                    try {
                        signInResponse = signIn(email, password)
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
                                showDrawer = true
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
            isLoading = isLoading,
            disabled = emailError || passwordError,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
