package com.paymentoptions.pos.ui.composables._components.buttons

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.primary900

@Composable

fun ShareButton(text: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, "Share")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)

    Column(
        modifier = modifier.clickable {
            startActivity(context, shareIntent, null)
        }, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.Share, contentDescription = "Share", modifier = Modifier
                .background(
                    iconBackgroundColor, shape = RoundedCornerShape(50)
                )
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            color = primary900,
        )
    }
}