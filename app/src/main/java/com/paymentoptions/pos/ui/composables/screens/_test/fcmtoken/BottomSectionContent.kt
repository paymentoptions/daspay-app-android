package com.paymentoptions.pos.ui.composables.screens._test.fcmtoken

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.ui.composables._components.MyCircularProgressIndicator
import com.paymentoptions.pos.ui.composables.screens.notifications.ScreenTitleWithCloseButton
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500


@Composable
fun BottomSectionContent(navController: NavController) {

    val context = LocalContext.current
    var fcmToken by remember { mutableStateOf<String?>("") }
    var loaderState by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loaderState = true
        fcmToken = SharedPreferences.getFcmToken(context)
        loaderState = false
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        ScreenTitleWithCloseButton(title = "FCM Token", navController = navController)

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, color = primary100.copy(alpha = 0.2f)),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            SelectionContainer {
                if (loaderState) MyCircularProgressIndicator("Loading...", primary500)
                else Text(
                    text = fcmToken.toString(),
                    modifier = Modifier.padding(20.dp),
                    color = primary500
                )
            }
        }
    }
}