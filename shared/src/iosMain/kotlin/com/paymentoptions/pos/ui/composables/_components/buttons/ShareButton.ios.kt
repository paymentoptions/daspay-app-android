package com.paymentoptions.pos.ui.composables._components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
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
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

@Composable
actual fun ShareButton(text: String, shareContent: String, modifier: Modifier) {
    Column(
        modifier = modifier.clickable {
            val activityViewController = UIActivityViewController(
                activityItems = listOf(shareContent),
                applicationActivities = null
            )
            val window = UIApplication.sharedApplication.windows.first() as? platform.UIKit.UIWindow
            window?.rootViewController?.presentViewController(
                activityViewController,
                animated = true,
                completion = null
            )
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Outlined.Share, contentDescription = "Share", modifier = Modifier
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
