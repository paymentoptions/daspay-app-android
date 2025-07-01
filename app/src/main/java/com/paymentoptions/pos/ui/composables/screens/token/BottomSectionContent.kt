package com.paymentoptions.pos.ui.composables.screens.token

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.inputs.OtpInputField
import com.paymentoptions.pos.ui.composables._components.inputs.pxToDp
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.composables.screens.fingerprintscan.FingerprintScanScreen
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.primary50
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.purple50


@Composable
fun BottomSectionContent(navController: NavController) {
    val context = LocalContext.current
    val otp = remember { mutableStateOf("") }
    var openFingerprintScan by remember { mutableStateOf(false) }

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
        Text(
            text = "Enter Token", style = AppTheme.typography.screenTitle
        )

        Text(
            text = "Enter the Token sent by Payment Options",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = purple50
        )

        Spacer(modifier = Modifier.height(30.dp))

        OtpInputField(
            otp = otp,
            count = 6,
            textColor = Color.White,
            otpBoxModifier = Modifier
                .height(70.dp)
                .background(Color.Transparent)
                .clip(RoundedCornerShape(8.dp))
                .background(primary50)
                .border(1.pxToDp(), Color(0xFF0C659D), shape = RoundedCornerShape(50.pxToDp())),
            otpTextType = KeyboardType.Number,
        )

        Row(
            modifier = Modifier.align(alignment = Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Didn't receive the code?")
            TextButton(onClick = { /* TODO: resend OTP */ }) {
                Text("Resend Code", textDecoration = TextDecoration.Underline)
            }
        }


        //Keypad
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, primary500.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .height(70.dp)
                        .weight(1f)
                ) {
                    Text(
                        "1", color = primary500, fontSize = 30.sp, fontWeight = FontWeight.Normal
                    )
                }

                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, primary500.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .height(70.dp)
                        .weight(1f)
                ) {
                    Text(
                        "2", color = primary500, fontSize = 30.sp, fontWeight = FontWeight.Normal
                    )
                }

                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, primary500.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .height(70.dp)
                        .weight(1f)
                ) {
                    Text(
                        "3", color = primary500, fontSize = 30.sp, fontWeight = FontWeight.Normal
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, primary500.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .height(70.dp)
                        .weight(1f)
                ) {
                    Text(
                        "4", color = primary500, fontSize = 30.sp, fontWeight = FontWeight.Normal
                    )
                }

                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, primary500.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .height(70.dp)
                        .weight(1f)
                ) {
                    Text(
                        "5", color = primary500, fontSize = 30.sp, fontWeight = FontWeight.Normal
                    )
                }

                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, primary500.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .height(70.dp)
                        .weight(1f)
                ) {
                    Text(
                        "6", color = primary500, fontSize = 30.sp, fontWeight = FontWeight.Normal
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, primary500.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .height(70.dp)
                        .weight(1f)
                ) {
                    Text(
                        "7", color = primary500, fontSize = 30.sp, fontWeight = FontWeight.Normal
                    )
                }

                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, primary500.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .height(70.dp)
                        .weight(1f)
                ) {
                    Text(
                        "8", color = primary500, fontSize = 30.sp, fontWeight = FontWeight.Normal
                    )
                }

                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, primary500.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .height(70.dp)
                        .weight(1f)
                ) {
                    Text(
                        "9", color = primary500, fontSize = 30.sp, fontWeight = FontWeight.Normal
                    )
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
                ) {

                }

                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, primary500.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .height(70.dp)
                        .weight(1f)
                ) {
                    Text(
                        "0", color = primary500, fontSize = 30.sp, fontWeight = FontWeight.Normal
                    )
                }

                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, primary500.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .height(70.dp)
                        .weight(1f)
                ) {

                    Icon(imageVector = Icons.Default.Backspace, contentDescription = "delete")

                }
            }
        }

        FilledButton(
            text = "Confirm",
            onClick = { openFingerprintScan = true },
            disabled = otp.value.length < 6
        )
    }
}