package com.paymentoptions.pos.ui.composables.screens.helpandsupport

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
import com.paymentoptions.pos.ui.composables._components.screentitle.ScreenTitleWithCloseButton
import com.paymentoptions.pos.ui.theme.purple50


@Composable
fun BottomSectionContent(navController: NavController) {
    val text =
        "Need assistance? We’re here to help you get the most out of DASPay.\n" + "\n" + "If you have any questions about using the app, payments, or features, please reach out to us. Our support team will get back to you as soon as possible to assist with your queries.\n" + "\n"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        ScreenTitleWithCloseButton(title = "Help & Support", navController = navController)

        Spacer(modifier = Modifier.height(30.dp))

        Column {
            Text(text, textAlign = TextAlign.Justify, color = purple50, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "You can contact us anytime at:",
                textAlign = TextAlign.Justify,
                color = purple50,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            LinkWithIcon(
                text = "support@paymentoptions.com",
                url = "mailto:support@paymentoptions.com",
                icon = Icons.Outlined.Mail
            )
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}