package com.paymentoptions.pos.ui.composables.screens.token

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.composables.screens.fingerprintscan.FingerprintScanScreen
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.borderThickPrimary100
import com.paymentoptions.pos.ui.theme.borderThickPrimary500
import com.paymentoptions.pos.ui.theme.linkColor
import com.paymentoptions.pos.ui.theme.noBorder
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

    val buttonBorder = borderThickPrimary100

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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

                OutlinedButton(
                    enabled = false,
                    onClick = {},
                    shape = RoundedCornerShape(12.dp),
                    border = buttonBorder,
                    modifier = Modifier
                        .height(70.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(primary50)

                        .weight(1f)
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

            (1..emptyBoxCount).toList().forEachIndexed { index, item ->

                OutlinedButton(
                    enabled = false,
                    onClick = {},
                    shape = RoundedCornerShape(12.dp),
                    border = buttonBorder,
                    modifier = Modifier
                        .height(70.dp)
                        .weight(1f)
                ) {
                    Text(
                        "", color = primary500, fontSize = 30.sp, fontWeight = FontWeight.Normal
                    )
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

        //Keypad
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(
                    enabled = otp.value.length < 6,
                    onClick = {
                        otp.value = otp.value + "1"
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = borderThickPrimary500,
                    modifier = Modifier
                        .height(70.dp)
                        .weight(1f)
                ) {
                    Text(
                        "1", color = primary500, fontSize = 30.sp, fontWeight = FontWeight.Normal
                    )
                }

                OutlinedButton(
                    enabled = otp.value.length < 6,
                    onClick = {
                        otp.value = otp.value + "2"
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = buttonBorder,
                    modifier = Modifier
                        .height(70.dp)
                        .weight(1f)
                ) {
                    Text(
                        "2", color = primary500, fontSize = 30.sp, fontWeight = FontWeight.Normal
                    )
                }

                OutlinedButton(
                    enabled = otp.value.length < 6,
                    onClick = {
                        otp.value = otp.value + "3"
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = buttonBorder,
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
                    enabled = otp.value.length < 6,
                    onClick = {
                        otp.value = otp.value + "4"
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = buttonBorder,
                    modifier = Modifier
                        .height(70.dp)
                        .weight(1f)
                ) {
                    Text(
                        "4", color = primary500, fontSize = 30.sp, fontWeight = FontWeight.Normal
                    )
                }

                OutlinedButton(
                    enabled = otp.value.length < 6,
                    onClick = {
                        otp.value = otp.value + "5"
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = buttonBorder,
                    modifier = Modifier
                        .height(70.dp)
                        .weight(1f)
                ) {
                    Text(
                        "5", color = primary500, fontSize = 30.sp, fontWeight = FontWeight.Normal
                    )
                }

                OutlinedButton(
                    enabled = otp.value.length < 6,
                    onClick = {
                        otp.value = otp.value + "6"
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = buttonBorder,
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
                    enabled = otp.value.length < 6,
                    onClick = {
                        otp.value = otp.value + "7"
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = buttonBorder,
                    modifier = Modifier
                        .height(70.dp)
                        .weight(1f)
                ) {
                    Text(
                        "7", color = primary500, fontSize = 30.sp, fontWeight = FontWeight.Normal
                    )
                }

                OutlinedButton(
                    enabled = otp.value.length < 6,
                    onClick = {
                        otp.value = otp.value + "8"
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = buttonBorder,
                    modifier = Modifier
                        .height(70.dp)
                        .weight(1f)
                ) {
                    Text(
                        "8", color = primary500, fontSize = 30.sp, fontWeight = FontWeight.Normal
                    )
                }

                OutlinedButton(
                    enabled = otp.value.length < 6,
                    onClick = {
                        otp.value = otp.value + "9"
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = buttonBorder,
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
                    enabled = otp.value.length < 6,
                    onClick = {
                        otp.value = otp.value + "0"
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = buttonBorder,
                    modifier = Modifier
                        .height(70.dp)
                        .weight(1f)
                ) {
                    Text(
                        "0", color = primary500, fontSize = 30.sp, fontWeight = FontWeight.Normal
                    )
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
            text = "Confirm",
            onClick = { openFingerprintScan = true },
            disabled = otp.value.length < 6,
            modifier = Modifier
                .fillMaxWidth()
                .height(59.dp)
        )
    }
}