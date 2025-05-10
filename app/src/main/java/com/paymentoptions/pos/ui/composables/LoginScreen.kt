package com.paymentoptions.pos.ui.composables

import BiometricAuthScreen
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.R
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.SignInResponse
import com.paymentoptions.pos.services.apiService.endpoints.signIn
import com.paymentoptions.pos.ui.composables._components.CustomCircularProgressIndicator
import com.paymentoptions.pos.ui.theme.Orange10
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var invalidCredentials by remember { mutableStateOf<String?>(null) }
    var showDrawer by remember { mutableStateOf<Boolean>(false) }
    var signInLoader by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    println("signInLoader: $signInLoader")

    val context = LocalContext.current

    if (showDrawer) {
        BiometricAuthScreen(
            {
                navController.navigate("drawerScreen") {
                    popUpTo(0) { inclusive = true }
                }
            },
            {
                Toast.makeText(context, "Cancelled", Toast.LENGTH_LONG).show()
            },
            navController,
            true
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween

        ) {


            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                Image(
                    painter = painterResource(id = R.drawable.logo), // Replace with your logo
                    contentDescription = "Logo",
                    modifier = Modifier.size(180.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Sign in to ")
                        }
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = Orange10
                            )
                        ) {
                            append("Payment Options")
                        }
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = if (isValidEmail(it)) null else "Invalid email"
                    },
                    label = { Text("Email Address") },
                    isError = emailError != null,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Orange10
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    shape = RoundedCornerShape(30.dp),
                    singleLine = true,
                    textStyle = TextStyle(
//                fontSize = 20.sp,
//                fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Normal,
                        textDecoration = TextDecoration.None
                    ),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        focusedIndicatorColor = Orange10,
                        focusedLabelColor = Orange10,
                        unfocusedLabelColor = Color.DarkGray,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorContainerColor = Color.White
                    )
                )

                if (emailError != null) {
                    Text(
                        emailError!!,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(5.dp)
                            .fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError =
                            if (isValidPassword(it)) null else "Password must be at least 6 characters"
                    },
                    label = { Text("Password") },
                    isError = passwordError != null,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Orange10
                        )
                    },
                    trailingIcon = {
                        val icon =
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Orange10
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    shape = RoundedCornerShape(30.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        focusedIndicatorColor = Orange10,
                        focusedLabelColor = Orange10,
                        unfocusedLabelColor = Color.DarkGray,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorContainerColor = Color.White
                    )
                )

                if (passwordError != null) {
                    Text(
                        passwordError!!,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(5.dp)
                            .fillMaxWidth()
                    )
                }

                if (invalidCredentials != null) {
                    Text(
                        invalidCredentials!!,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(5.dp)
                            .fillMaxWidth()
                    )
                }

                // Forgot Password
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Button(
                onClick = {
                    emailError = if (isValidEmail(email)) null else "Invalid email"
                    passwordError =
                        if (isValidPassword(password)) null else "Password must be at least 8 characters"

                    if (emailError == null && passwordError == null && invalidCredentials == null) {

                        scope.launch {
                            signInLoader = true
                            var signInResponse: SignInResponse? = null

                            try {

                                signInResponse = signIn(email, password)
                                println("signInResponse: $signInResponse")

                                if (signInResponse == null) {
                                    Toast.makeText(
                                        context,
                                        "Invalid Credentials",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                }

                                signInResponse?.let {
                                    if (signInResponse.success) {
                                        SharedPreferences.saveAuthDetails(context, signInResponse)
                                        showDrawer = true
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Invalid Credentials", Toast.LENGTH_LONG)
                                    .show()
                            } finally {
                                signInLoader = false
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Orange10),
                shape = RoundedCornerShape(50),
                enabled = !signInLoader,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {

                if (signInLoader)
                    CustomCircularProgressIndicator("Signing In", Orange10)
                else
                    Text("Sign In", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun isValidPassword(password: String): Boolean {
    return password.length >= 8
}