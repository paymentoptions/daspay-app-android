package com.paymentoptions.pos.ui.screens.helpandsupport

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.payment.reader.isTapToPayEducationAvailable
import com.paymentoptions.pos.payment.reader.showTapToPayEducation
import com.paymentoptions.pos.tapToPayDisplayName
import com.paymentoptions.pos.ui.composables._components.LinkWithIcon
import com.paymentoptions.pos.ui.composables._components.ScreenTitleWithCloseButton
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.utils.modifiers.noRippleClickable
import kotlinx.coroutines.launch

@Composable
fun BottomSectionContent(navController: NavController) {
    val text =
        "Need assistance? We're here to help you get the most out of DASPay.\n" + "\n" + "If you have any questions about using the app, payments, or features, please reach out to us. Our support team will get back to you as soon as possible to assist with your queries.\n" + "\n"

    val scope = rememberCoroutineScope()

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

            if (isTapToPayEducationAvailable()) {
                Text(
                    text = "How to use $tapToPayDisplayName",
                    textAlign = TextAlign.Center,
                    color = primary500,
                    fontSize = 15.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .noRippleClickable { scope.launch { showTapToPayEducation() } },
                )
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
