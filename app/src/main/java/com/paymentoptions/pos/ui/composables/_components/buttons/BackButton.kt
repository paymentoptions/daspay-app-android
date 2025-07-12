package com.paymentoptions.pos.ui.composables._components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500

@Composable
fun BackButton(
    onClickShowMenuBarButton: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClickShowMenuBarButton,
        modifier = modifier
            .width(30.dp)
            .height(40.dp)
            .background(
                primary100.copy(alpha = 0.2f), shape = RoundedCornerShape(
                    topStartPercent = 50,
                    topEndPercent = 50,
                    bottomStartPercent = 0,
                    bottomEndPercent = 0
                )
            )
    ) {
        Icon(
            imageVector = Icons.Default.ArrowUpward,
            contentDescription = "Back button",
            tint = primary500.copy(alpha = 0.2f),
            modifier = Modifier.size(30.dp)
        )
    }
}