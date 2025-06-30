package com.paymentoptions.pos.ui.composables.screens.token

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.paymentoptions.pos.ui.theme.primary100

@Composable
fun BottomSectionContent(navController: NavController) {
    remember { mutableStateOf("") }
    remember { mutableStateOf("") }
    remember { mutableStateOf("") }
    remember { mutableStateOf(false) }
    val otp = remember { mutableStateOf("") }
    val context = LocalContext.current
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
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "Enter Token",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0033CC),

            )

        Text(
            text = "Enter the Token sent by Payment Options",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OtpInputField(
            otp = otp,
            count = 6,
            textColor = Color.White,
            otpBoxModifier = Modifier
                .background(primary100)
                .border(40.pxToDp(), primary100, shape = RoundedCornerShape(50.pxToDp())),
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

        Spacer(modifier = Modifier.height(12.dp))

        FilledButton(
            text = "Confirm", onClick = { openFingerprintScan = true })
    }
}