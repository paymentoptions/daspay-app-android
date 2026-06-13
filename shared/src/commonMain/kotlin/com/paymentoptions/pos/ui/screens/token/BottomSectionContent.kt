package com.paymentoptions.pos.ui.screens.token

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.getBiometricAuthenticator
import com.paymentoptions.pos.network.endpoints.completeDeviceRegistration
import com.paymentoptions.pos.network.endpoints.getExternalDeviceConfiguration
import com.paymentoptions.pos.showToast
import com.paymentoptions.pos.ui.composables._components.MyElevatedCard
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.navigation.Screens
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.innerShadow
import com.paymentoptions.pos.ui.theme.noBorder
import com.paymentoptions.pos.ui.theme.primary300
import com.paymentoptions.pos.ui.theme.primary50
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.utils.getDeviceIdentifier
import com.paymentoptions.pos.utils.modifiers.innerShadow
import com.paymentoptions.pos.utils.parseApiErrorMessage
import kotlinx.coroutines.launch

@Composable
fun TokenBottomSectionContent(navController: NavController, enableScrolling: Boolean = false) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val deviceNumber = remember { getDeviceIdentifier() }

    var otp by remember { mutableStateOf("") }
    var lastClicked by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(6.dp))
        errorMessage?.let { message ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(color = Color(0xFFEB5757).copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp))
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Error,
                    contentDescription = "Error",
                    tint = Color(0xFFEB5757),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message,
                    color = Color(0xFFEB5757),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Text(text = "Register Device", style = AppTheme.typography.screenTitle)

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Enter the token provided by admin to register this device",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = purple50,
        )

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(6) { idx ->
                val ch = otp.getOrNull(idx)?.toString() ?: ""
                MyElevatedCard(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        enabled = false,
                        onClick = {},
                        border = noBorder,
                        modifier = Modifier
                            .height(70.dp)
                            .fillMaxWidth()
                            .innerShadow(
                                color = primary300,
                                blur = 15.dp,
                                spread = 5.dp,
                                cornersRadius = 5.dp,
                                offsetX = 0.dp,
                                offsetY = 0.dp,
                            )
                            .background(primary50),
                    ) {
                        Text(ch, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Normal, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState, enabled = enableScrolling),
        ) {
            val rows = listOf(
                listOf(1, 2, 3),
                listOf(4, 5, 6),
                listOf(7, 8, 9),
            )

            rows.forEach { rowDigits ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowDigits.forEach { digit ->
                        DigitButton(
                            digit = digit,
                            isSelected = lastClicked == digit,
                            onClick = {
                                if (otp.length < 6) {
                                    otp += digit.toString()
                                    lastClicked = digit
                                }
                            },
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.height(70.dp).weight(1f))

                DigitButton(
                    digit = 0,
                    isSelected = lastClicked == 0,
                    onClick = {
                        if (otp.length < 6) {
                            otp += "0"
                            lastClicked = 0
                        }
                    },
                )

                OutlinedButton(
                    enabled = otp.isNotEmpty(),
                    onClick = { otp = otp.dropLast(1) },
                    shape = RoundedCornerShape(8.dp),
                    border = noBorder,
                    modifier = Modifier.height(70.dp).weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Backspace,
                        contentDescription = "delete",
                        tint = primary500,
                        modifier = Modifier.scale(1.2f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            FilledButton(
                modifier = Modifier.fillMaxWidth().height(59.dp),
                text = "Register Device",
                disabled = otp.length < 6 || isLoading,
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null

                        completeDeviceRegistration(otp, deviceNumber)
                            .onSuccess { response ->
                                if (response.success || response.message.contains("Device already registered")) {
                                    DPStorageManager.saveTokenStatus(tokenCode = otp, isVerified = true)
                                    getExternalDeviceConfiguration(otp, deviceNumber)
                                        .onSuccess { configResponse ->
                                            DPStorageManager.saveDeviceConfiguration(configResponse)
                                            openBiometricAndNavigate(navController)
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
                                if (exceptionMessage == "Device already registered") {
                                    getExternalDeviceConfiguration(otp, deviceNumber)
                                        .onSuccess { configResponse ->
                                            DPStorageManager.saveTokenStatus(tokenCode = otp, isVerified = true)
                                            DPStorageManager.saveDeviceConfiguration(configResponse)
                                            openBiometricAndNavigate(navController)
                                        }
                                        .onFailure { innerException ->
                                            errorMessage = parseApiErrorMessage(innerException, "Failed to fetch configuration")
                                        }
                                } else if (exceptionMessage.lowercase().contains("unauthorized")) {
                                    showToast("Token expired. Please sign in again.")
                                    DPStorageManager.clearSharedPreferences()
                                    navController.navigate(Screens.AuthCheck.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                } else {
                                    errorMessage = exceptionMessage
                                }
                            }

                        isLoading = false
                    }
                },
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RowScope.DigitButton(
    digit: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    MyElevatedCard(modifier = Modifier.weight(1f), isSelected = isSelected) {
        OutlinedButton(
            enabled = true,
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            border = noBorder,
            modifier = Modifier
                .height(70.dp)
                .innerShadow(
                    color = innerShadow,
                    blur = if (isSelected) 50.dp else 1.dp,
                    spread = if (isSelected) 10.dp else 1.dp,
                    cornersRadius = 8.dp,
                    offsetX = 0.dp,
                    offsetY = 0.dp,
                )
                .fillMaxWidth(),
        ) {
            Text(
                text = digit.toString(),
                color = primary500,
                fontSize = 30.sp,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}

private fun openBiometricAndNavigate(navController: NavController) {
    val biometricAuthenticator = getBiometricAuthenticator()
    if (!biometricAuthenticator.isAvailable()) {
        showToast("Device security is not enabled. Please set up PIN, fingerprint, or face lock.")
        return
    }

    biometricAuthenticator.authenticate(
        title = "Authenticate",
        subtitle = "Verify your identity to proceed",
        onSuccess = {
            navController.navigate(Screens.Dashboard.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        },
        onError = {
            showToast("Authentication failed")
        },
    )
}

