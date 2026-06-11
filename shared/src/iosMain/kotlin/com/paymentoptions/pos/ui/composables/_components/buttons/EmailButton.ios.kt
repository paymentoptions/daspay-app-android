package com.paymentoptions.pos.ui.composables._components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.primary900
import platform.MessageUI.MFMailComposeViewController
import platform.MessageUI.MFMailComposeViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.darwin.NSObject

@Composable
actual fun EmailButton(text: String, email: Email, modifier: Modifier) {
    Column(
        modifier = modifier.clickable {
            if (MFMailComposeViewController.canSendMail()) {
                val mailComposeViewController = MFMailComposeViewController()
                mailComposeViewController.setSubject(email.subject)
                mailComposeViewController.setMessageBody(email.text, false)
                mailComposeViewController.setToRecipients(listOf(email.id))
                
                // We need a delegate to dismiss the controller
                val delegate = object : NSObject(), MFMailComposeViewControllerDelegateProtocol {
                    override fun mailComposeController(
                        controller: MFMailComposeViewController,
                        didFinishWithResult: platform.MessageUI.MFMailComposeResult,
                        error: platform.Foundation.NSError?
                    ) {
                        controller.dismissViewControllerAnimated(true, null)
                    }
                }
                // Note: delegate must be kept alive, but for this simple example:
                mailComposeViewController.mailComposeDelegate = delegate

                val window = UIApplication.sharedApplication.windows.first() as? platform.UIKit.UIWindow
                window?.rootViewController?.presentViewController(
                    mailComposeViewController,
                    animated = true,
                    completion = null
                )
            }
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Outlined.Email,
            contentDescription = "Share via Email",
            modifier = Modifier
                .background(
                    iconBackgroundColor, shape = RoundedCornerShape(50)
                )
                .padding(16.dp)
        )

        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            color = primary900,
        )
    }
}
