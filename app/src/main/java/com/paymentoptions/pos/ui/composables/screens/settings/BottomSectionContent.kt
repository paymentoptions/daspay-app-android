package com.paymentoptions.pos.ui.composables.screens.settings

import MyDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.SignOutResponse
import com.paymentoptions.pos.services.apiService.endpoints.signOut
import com.paymentoptions.pos.ui.composables._components.LinkWithIcon
import com.paymentoptions.pos.ui.composables._components.MySwitch
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.screentitle.ScreenTitleWithCloseButton
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.composables.screens.fingerprintscan.FingerprintScanScreen
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.purple50
import isBiometricAvailable
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

@Composable
fun BottomSectionContent(navController: NavController) {
    val context = LocalContext.current

    var signOutLoader by remember { mutableStateOf(false) }
    var showSignOutConfirmationDialog by remember { mutableStateOf(false) }
    var signOutResponse: SignOutResponse? = null
    val scope = rememberCoroutineScope()
    var showBiometricScreen by remember { mutableStateOf(false) }
    var biometricsEnabled by remember { mutableStateOf(SharedPreferences.getBiometricsStatus(context)) }
    val isBiometricsAvailable = isBiometricAvailable(context)

    var authDetails = SharedPreferences.getAuthDetails(context)
    val username = authDetails?.data?.name ?: ""
    val email = authDetails?.data?.email ?: ""
    rememberSystemUiController()
//    var immersiveMode by remember { mutableStateOf(systemUiController.isSystemBarsVisible) }

    val lastLoginString: String =
        SimpleDateFormat("DD MMMM YYYY | hh:mm a").format(authDetails?.data?.auth_time ?: "")

    MyDialog(
        showDialog = showSignOutConfirmationDialog,
        title = "Confirmation Required",
        text = "Do you want to log out?",
        acceptButtonText = "Log Out",
        onAcceptFn = {
            scope.launch {
                signOutLoader = true

                try {
                    signOutResponse = signOut(context)
                    println("signOutResponse: $signOutResponse")

                    if (signOutResponse == null) {
                        navController.navigate(Screens.SignIn.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }

                    signOutResponse?.let {
                        if (it.success) {
                            SharedPreferences.clearSharedPreferences(context)
                            navController.navigate(Screens.SignIn.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                } catch (e: Exception) {
//                            Toast.makeText(context, "Unable to sign out", Toast.LENGTH_LONG).show()

                    SharedPreferences.clearSharedPreferences(context)
                    navController.navigate(Screens.SignIn.route) {
                        popUpTo(0) { inclusive = true }
                    }

                    println("Error: ${e.toString()}")
                } finally {
                    signOutLoader = false
                }
            }

            showSignOutConfirmationDialog = false
        },
        onDismissFn = { showSignOutConfirmationDialog = false })

    if (showBiometricScreen) FingerprintScanScreen(
        navController = navController, onAuthSuccess = {
            SharedPreferences.saveBiometricsStatus(context, !biometricsEnabled)
            biometricsEnabled = !biometricsEnabled
            showBiometricScreen = false
        }, onAuthFailed = {
            //SharedPreferences.saveBiometricsStatus(context, false)
            showBiometricScreen = false
        }, bypassBiometric = false
    )
    else

        Column(
            modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScreenTitleWithCloseButton(title = "Settings", navController = navController)

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                Text(
                    text = "Welcome,",
                    fontWeight = FontWeight.Normal,
                    color = primary500,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = username,
                    fontWeight = FontWeight.Bold,
                    color = primary500,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Last login: $lastLoginString", color = purple50, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(30.dp))

                LinkWithIcon(
                    text = email, url = "mailto:$email", icon = Icons.Outlined.Mail
                )

                Spacer(modifier = Modifier.height(50.dp))

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(), color = Color.LightGray.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (isBiometricsAvailable) Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "Biometric",
                        fontWeight = FontWeight.Bold,
                        color = primary500,
                        fontSize = 15.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        Text(
                            if (biometricsEnabled) "Enabled" else "Disabled",
                            fontWeight = FontWeight.Medium,
                            color = primary500,
                            fontSize = 14.sp
                        )

                        MySwitch(isEnabled = biometricsEnabled, onClick = {
                            try {
                                showBiometricScreen = true
                            } catch (_: Exception) {
                                Toast.makeText(
                                    context, "Unable to set biometrics status", Toast.LENGTH_LONG
                                ).show()
                            }
                        })
                    }
                }

                //ImmersiveMode Toggle
//                Row(
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.fillMaxWidth(),
//                ) {
//                    Text(
//                        "Immersive Mode",
//                        fontWeight = FontWeight.Bold,
//                        color = primary500,
//                        fontSize = 15.sp
//                    )
//                    Switch(
//                        colors = SwitchDefaults.colors().copy(
//                            checkedTrackColor = primary100
//                        ), checked = immersiveMode, onCheckedChange = {
//                            try {
//                                systemUiController.isSystemBarsVisible =
//                                    !systemUiController.isSystemBarsVisible
//                                immersiveMode = !immersiveMode
//
//                                SharedPreferences.saveImmersiveModeStatus(context, !immersiveMode)
//
//                            } catch (_: Exception) {
//                                Toast.makeText(
//                                    context, "Unable to set immersive mode", Toast.LENGTH_LONG
//                                ).show()
//                            }
//                        }, modifier = Modifier.shadow(
//                            elevation = 8.dp,
//                            shape = RoundedCornerShape(16.dp),
//                            ambientColor = if (biometricsEnabled) Color.Gray.copy(0.25f) else Color.Transparent,
//                            spotColor = if (biometricsEnabled) Color.Gray.copy(0.25f) else Color.Transparent
//                        )
//                    )
//                }

                Spacer(modifier = Modifier.height(20.dp))

                FilledButton(
                    text = "Logout", onClick = {
                        showSignOutConfirmationDialog = true
                    }, isLoading = signOutLoader, modifier = Modifier
                        .fillMaxWidth()
                        .height(59.dp)
                )
            }
        }
}