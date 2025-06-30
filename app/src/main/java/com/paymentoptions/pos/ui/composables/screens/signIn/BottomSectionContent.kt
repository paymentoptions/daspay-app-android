package com.paymentoptions.pos.ui.composables.screens.signIn

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
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
import com.paymentoptions.pos.ui.composables._components.inputs.PasswordInput
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.utils.validation.validateEmail
import com.paymentoptions.pos.utils.validation.validateOtp
import com.paymentoptions.pos.utils.validation.validatePassword
import kotlinx.coroutines.launch

@Composable
fun BottomSectionContent(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("ankitkambale097@myyahoo.com") }
    var password by remember { mutableStateOf("Test12345678@#") }

//    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }

//    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    var otp by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf(false) }

    var showDrawer by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Sign in",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0033CC),
            modifier = Modifier.padding(bottom = 16.dp)
        )

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
            text = "Proceed", onClick = {
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
                    } catch (e: Exception) {
                        Toast.makeText(context, "Invalid Credentials", Toast.LENGTH_LONG).show()
                    } finally {
                        isLoading = false
                    }
                }


            },
            isLoading = isLoading,
            disabled = emailError || passwordError
        )
    }
}
