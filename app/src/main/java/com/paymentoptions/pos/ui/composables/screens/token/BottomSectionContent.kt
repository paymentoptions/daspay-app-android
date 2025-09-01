package com.paymentoptions.pos.ui.composables.screens.token

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.endpoints.completeDeviceRegistration
import com.paymentoptions.pos.services.apiService.endpoints.getExternalDeviceConfiguration
import com.paymentoptions.pos.ui.composables._components.MyElevatedCard
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.dialogs.AlertDialogType
import com.paymentoptions.pos.ui.composables._components.dialogs.MyAlertDialog
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.composables.screens.fingerprintscan.FingerprintScanScreen
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.innerShadow
import com.paymentoptions.pos.ui.theme.linkColor
import com.paymentoptions.pos.ui.theme.noBorder
import com.paymentoptions.pos.ui.theme.primary300
import com.paymentoptions.pos.ui.theme.primary50
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.utils.modifiers.innerShadow
import kotlinx.coroutines.launch
import org.json.JSONObject


@Composable
fun BottomSectionContent(navController: NavController, enableScrolling: Boolean = false) {
    val context = LocalContext.current
    val otp = remember { mutableStateOf("") }
    val scrollstate = rememberScrollState()
    var openFingerprintScan by remember { mutableStateOf(false) }
    var lastClicked by remember { mutableStateOf<Int?>(null) }
    var scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (openFingerprintScan) FingerprintScanScreen(navController = navController, onAuthSuccess = {
        navController.navigate(Screens.Dashboard.route) {
            popUpTo(Screens.SignIn.route) { inclusive = true }
        }
        openFingerprintScan = false

    }, onAuthFailed = {
        Toast.makeText(context, "Cancelled", Toast.LENGTH_LONG).show()
        openFingerprintScan = false
    })

    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        MyAlertDialog(
            showDialog = errorMessage != null,
            text = errorMessage.toString(),
            actionButtonText = "Try Again",
            type = AlertDialogType.ERROR,
            onActionFn = { errorMessage = null })

        Text(
            text = "Enter Token", style = AppTheme.typography.screenTitle
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Enter the Token sent by Payment Options",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = purple50
        )

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            val emptyBoxCount = 6 - otp.value.length

            otp.value.toList().forEachIndexed { index, digit ->
                MyElevatedCard(modifier = Modifier.weight(1.0f)) {
                    OutlinedButton(
                        enabled = false,
                        onClick = {},
                        border = noBorder,
                        modifier = Modifier
                            .height(70.dp)
                            .innerShadow(
                                color = primary300,
                                blur = 15.dp,
                                spread = 5.dp,
                                cornersRadius = 5.dp,
                                offsetX = 0.dp,
                                offsetY = 0.dp
                            )
                            .background(primary50)

                    ) {
                        Text(
                            digit.toString(),
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            (1..emptyBoxCount).toList().forEachIndexed { index, item ->

                MyElevatedCard(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        enabled = false,
                        border = noBorder,
                        onClick = {},
                        modifier = Modifier.height(70.dp)
                    ) {
                        Text(
                            "", color = primary500, fontSize = 30.sp, fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.align(alignment = Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Didn't receive the code?",
                color = purple50,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "Resend Code",
                fontSize = 12.sp,
                textDecoration = TextDecoration.Underline,
                color = linkColor
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollstate)
        ) {
            //Keypad
            Column(
                modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    MyElevatedCard(modifier = Modifier.weight(1f), isSelected = lastClicked == 1) {
                        OutlinedButton(
                            enabled = otp.value.length < 6, onClick = {
                                otp.value = otp.value + "1"
                                lastClicked = 1
                            }, border = noBorder, modifier = Modifier
                                .fillMaxWidth()
                                .innerShadow(
                                    color = innerShadow,
                                    blur = if (lastClicked == 1) 50.dp else 1.dp,
                                    spread = if (lastClicked == 1) 10.dp else 1.dp,
                                    cornersRadius = 8.dp,
                                    offsetX = 0.dp,
                                    offsetY = 0.dp
                                )
                                .height(70.dp)

                        ) {
                            Text(
                                "1",
                                color = primary500,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    MyElevatedCard(modifier = Modifier.weight(1f), isSelected = lastClicked == 2) {
                        OutlinedButton(
                            enabled = otp.value.length < 6,
                            onClick = {
                                otp.value = otp.value + "2"
                                lastClicked = 2
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = noBorder,
                            modifier = Modifier
                                .height(70.dp)
                                .innerShadow(
                                    color = innerShadow,
                                    blur = if (lastClicked == 2) 50.dp else 1.dp,
                                    spread = if (lastClicked == 2) 10.dp else 1.dp,
                                    cornersRadius = 8.dp,
                                    offsetX = 0.dp,
                                    offsetY = 0.dp
                                )
                                .fillMaxWidth()
                        ) {
                            Text(
                                "2",
                                color = primary500,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    MyElevatedCard(modifier = Modifier.weight(1f), isSelected = lastClicked == 3) {
                        OutlinedButton(
                            enabled = otp.value.length < 6,
                            onClick = {
                                otp.value = otp.value + "3"
                                lastClicked = 3
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = noBorder,
                            modifier = Modifier
                                .height(70.dp)
                                .innerShadow(
                                    color = innerShadow,
                                    blur = if (lastClicked == 3) 50.dp else 1.dp,
                                    spread = if (lastClicked == 3) 10.dp else 1.dp,
                                    cornersRadius = 8.dp,
                                    offsetX = 0.dp,
                                    offsetY = 0.dp
                                )
                                .fillMaxWidth()
                        ) {
                            Text(
                                "3",
                                color = primary500,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    MyElevatedCard(modifier = Modifier.weight(1f), isSelected = lastClicked == 4) {
                        OutlinedButton(
                            enabled = otp.value.length < 6,
                            onClick = {
                                otp.value = otp.value + "4"
                                lastClicked = 4
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = noBorder,
                            modifier = Modifier
                                .height(70.dp)
                                .innerShadow(
                                    color = innerShadow,
                                    blur = if (lastClicked == 4) 50.dp else 1.dp,
                                    spread = if (lastClicked == 4) 10.dp else 1.dp,
                                    cornersRadius = 8.dp,
                                    offsetX = 0.dp,
                                    offsetY = 0.dp
                                )
                                .fillMaxWidth()
                        ) {
                            Text(
                                "4",
                                color = primary500,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    MyElevatedCard(modifier = Modifier.weight(1f), isSelected = lastClicked == 5) {
                        OutlinedButton(
                            enabled = otp.value.length < 6,
                            onClick = {
                                otp.value = otp.value + "5"
                                lastClicked = 5
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = noBorder,
                            modifier = Modifier
                                .height(70.dp)
                                .innerShadow(
                                    color = innerShadow,
                                    blur = if (lastClicked == 5) 50.dp else 1.dp,
                                    spread = if (lastClicked == 5) 10.dp else 1.dp,
                                    cornersRadius = 8.dp,
                                    offsetX = 0.dp,
                                    offsetY = 0.dp
                                )
                                .fillMaxWidth()
                        ) {
                            Text(
                                "5",
                                color = primary500,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    MyElevatedCard(modifier = Modifier.weight(1f), isSelected = lastClicked == 6) {
                        OutlinedButton(
                            enabled = otp.value.length < 6,
                            onClick = {
                                otp.value = otp.value + "6"
                                lastClicked = 6
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = noBorder,
                            modifier = Modifier
                                .height(70.dp)
                                .innerShadow(
                                    color = innerShadow,
                                    blur = if (lastClicked == 6) 50.dp else 1.dp,
                                    spread = if (lastClicked == 6) 10.dp else 1.dp,
                                    cornersRadius = 8.dp,
                                    offsetX = 0.dp,
                                    offsetY = 0.dp
                                )
                                .fillMaxWidth()
                        ) {
                            Text(
                                "6",
                                color = primary500,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {


                    MyElevatedCard(modifier = Modifier.weight(1f), isSelected = lastClicked == 7) {
                        OutlinedButton(
                            enabled = otp.value.length < 6,
                            onClick = {
                                otp.value = otp.value + "7"
                                lastClicked = 7
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = noBorder,
                            modifier = Modifier
                                .height(70.dp)
                                .innerShadow(
                                    color = innerShadow,
                                    blur = if (lastClicked == 7) 50.dp else 1.dp,
                                    spread = if (lastClicked == 7) 10.dp else 1.dp,
                                    cornersRadius = 8.dp,
                                    offsetX = 0.dp,
                                    offsetY = 0.dp
                                )
                                .fillMaxWidth()
                        ) {
                            Text(
                                "7",
                                color = primary500,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }


                    MyElevatedCard(modifier = Modifier.weight(1f), isSelected = lastClicked == 8) {
                        OutlinedButton(
                            enabled = otp.value.length < 6,
                            onClick = {
                                otp.value = otp.value + "8"
                                lastClicked = 8
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = noBorder,
                            modifier = Modifier
                                .height(70.dp)
                                .innerShadow(
                                    color = innerShadow,
                                    blur = if (lastClicked == 8) 50.dp else 1.dp,
                                    spread = if (lastClicked == 8) 10.dp else 1.dp,
                                    cornersRadius = 8.dp,
                                    offsetX = 0.dp,
                                    offsetY = 0.dp
                                )
                                .fillMaxWidth()
                        ) {
                            Text(
                                "8",
                                color = primary500,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    MyElevatedCard(modifier = Modifier.weight(1f), isSelected = lastClicked == 9) {
                        OutlinedButton(
                            enabled = otp.value.length < 6,
                            onClick = {
                                otp.value = otp.value + "9"
                                lastClicked = 9
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = noBorder,
                            modifier = Modifier
                                .height(70.dp)
                                .innerShadow(
                                    color = innerShadow,
                                    blur = if (lastClicked == 9) 50.dp else 1.dp,
                                    spread = if (lastClicked == 9) 10.dp else 1.dp,
                                    cornersRadius = 8.dp,
                                    offsetX = 0.dp,
                                    offsetY = 0.dp
                                )
                                .fillMaxWidth()
                        ) {
                            Text(
                                "9",
                                color = primary500,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .height(70.dp)
                            .weight(1f)
                    ) {}

                    MyElevatedCard(modifier = Modifier.weight(1f), isSelected = lastClicked == 0) {
                        OutlinedButton(
                            enabled = otp.value.length < 6,
                            onClick = {
                                otp.value = otp.value + "0"
                                lastClicked = 0
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = noBorder,
                            modifier = Modifier
                                .height(70.dp)
                                .innerShadow(
                                    color = innerShadow,
                                    blur = if (lastClicked == 0) 50.dp else 1.dp,
                                    spread = if (lastClicked == 0) 10.dp else 1.dp,
                                    cornersRadius = 8.dp,
                                    offsetX = 0.dp,
                                    offsetY = 0.dp
                                )
                                .fillMaxWidth()
                        ) {
                            Text(
                                "0",
                                color = primary500,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    OutlinedButton(
                        enabled = otp.value.isNotEmpty(),
                        onClick = {
                            otp.value = if (otp.value.length == 1) "" else otp.value.substring(
                                0, otp.value.length - 1
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = noBorder,
                        modifier = Modifier
                            .height(70.dp)
                            .weight(1f)
                    ) {

                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Backspace,
                            contentDescription = "delete",
                            tint = primary500,
                            modifier = Modifier.scale(1.2f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            FilledButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(59.dp),
                text = "Confirm",
                disabled = otp.value.length < 6 || isLoading,
                onClick = {
                    // special version for debugging
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        Log.d("DEBUG_TOKEN", "Step 1: Button clicked. Starting process.")

                        // --- First API Call ---
                        completeDeviceRegistration(context, otp.value).onSuccess { response ->
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
                                    context = context, isVerified = true
                                )

                                getExternalDeviceConfiguration(
                                    context, otp.value
                                ).onSuccess { configResponse ->
                                    Log.d(
                                        "DEBUG_TOKEN",
                                        "Step 4: getExternalDeviceConfiguration SUCCEEDED. Response: $configResponse"
                                    )
                                    SharedPreferences.saveDeviceConfiguration(
                                        context, configResponse
                                    )
                                    openFingerprintScan = true
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
                                val jsonObject = JSONObject(jsonPart)
                                val exceptionMessage = jsonObject.getString("message")

                                Log.e(
                                    "Step 2 FAILED: completeDeviceRegistration.", exceptionMessage
                                )
                                if (exceptionMessage == "Device already registered") {
                                    getExternalDeviceConfiguration(
                                        context, otp.value
                                    ).onSuccess { configResponse ->

                                        SharedPreferences.saveTokenStatus(
                                            context = context, isVerified = true
                                        )

                                        Log.d(
                                            "DEBUG_TOKEN",
                                            "Step 4: getExternalDeviceConfiguration SUCCEEDED. Response: $configResponse"
                                        )
                                        SharedPreferences.saveDeviceConfiguration(
                                            context, configResponse
                                        )
                                        openFingerprintScan = true
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
                                    Toast.makeText(
                                        context,
                                        "Token expired. Please sign in again.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    navController.navigate(Screens.SignIn.route) {
                                        popUpTo(Screens.SignIn.route) { inclusive = true }
                                    }
                                } else {
                                    errorMessage = exceptionMessage ?: "An unknown error occurred"
                                }
                            } catch (e: Exception) {
                                errorMessage = e.message.toString()
                            }
                        }
                        isLoading = false
                        Log.d("DEBUG_TOKEN", "Step 5: Process finished.")
                    }
                })
        }
    }
}