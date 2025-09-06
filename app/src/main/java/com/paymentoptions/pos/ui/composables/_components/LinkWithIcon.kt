package com.paymentoptions.pos.ui.composables._components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.common.extensions.isNotNull
import com.paymentoptions.pos.ui.theme.linkColor


@Composable
fun LinkWithIcon(text: String, url: String, icon: ImageVector? = null) {
    val annotatedLinkString: AnnotatedString = remember {
        buildAnnotatedString {

            val linkStyle = SpanStyle(
                color = linkColor,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                textDecoration = TextDecoration.Underline,
            )

            withLink(LinkAnnotation.Url(url = url)) {
                withStyle(
                    style = linkStyle
                ) {
                    append(text)
                }
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (icon.isNotNull()) Arrangement.spacedBy(4.dp) else Arrangement.Center
    ) {

        if (icon != null) Icon(
            imageVector = icon, contentDescription = "Email", modifier = Modifier.size(18.dp)
        )

        Text(annotatedLinkString, textAlign = TextAlign.Center)
    }
}