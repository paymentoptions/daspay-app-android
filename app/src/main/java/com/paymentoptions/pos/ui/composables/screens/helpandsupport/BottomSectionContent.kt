package com.paymentoptions.pos.ui.composables.screens.helpandsupport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables._components.LinkWithIcon
import com.paymentoptions.pos.ui.composables.screens.notifications.ScreenTitleWithCloseButton
import com.paymentoptions.pos.ui.theme.purple50


@Composable
fun BottomSectionContent(navController: NavController) {
    val text =
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent vehicula pellentesque odio, sit amet volutpat nisl porta vel. In quis tortor accumsan, venenatis urna eget, tempor massa. \n\n" + "Donec gravida eros quis arcu pellentesque, vitae elementum lectus lacinia. Cras id efficitur nibh. Vestibulum ante ipsum primis in faucibus"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {


        ScreenTitleWithCloseButton(title = "Help & Support", navController = navController)

        Spacer(modifier = Modifier.height(10.dp))

        Column {
            Text(text, textAlign = TextAlign.Justify, color = purple50, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(20.dp))
            LinkWithIcon(
                text = "support@DasPay.com",
                url = "mailto:support@DASPay.com",
                icon = Icons.Outlined.Mail
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}