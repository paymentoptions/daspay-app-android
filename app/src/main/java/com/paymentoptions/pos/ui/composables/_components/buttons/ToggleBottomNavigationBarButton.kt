package com.paymentoptions.pos.ui.composables._components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.paymentoptions.pos.R
import com.paymentoptions.pos.ui.theme.containerBackgroundGradientBrush

@Composable
fun ToggleBottomNavigationBarButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick, modifier = modifier
            .width(40.dp)
            .height(28.dp)
            .background(
                brush = containerBackgroundGradientBrush, shape = RoundedCornerShape(
                    topStartPercent = 50,
                    topEndPercent = 50,
                    bottomStartPercent = 0,
                    bottomEndPercent = 0
                )
            )
    ) {
        Icon(
            painter = painterResource(R.drawable.down_arrow),
            contentDescription = "Back button",
            tint = Color(0xFFA2C9DF),
            modifier = Modifier
                .size(20.dp)
                .rotate(180f)
                .offset(y = (5).dp)
        )
    }
}