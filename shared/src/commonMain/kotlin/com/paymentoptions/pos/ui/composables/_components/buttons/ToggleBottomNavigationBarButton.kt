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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.paymentoptions.pos.ui.theme.borderColor
import com.paymentoptions.pos.ui.theme.containerToggleBottomButtonGradientBrush
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.down_arrow

@Composable
fun ToggleBottomNavigationBarButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick, modifier = modifier
            .width(65.dp)
            .height(38.dp)
            .shadow(spotColor = borderColor, ambientColor = borderColor, elevation = 6.dp)
            .background(
                brush = containerToggleBottomButtonGradientBrush, shape = RoundedCornerShape(
                    topStartPercent = 60,
                    topEndPercent = 60,
                    bottomStartPercent = 30,
                    bottomEndPercent = 30
                ),
            )
    ) {
        Icon(
            painter = painterResource(Res.drawable.down_arrow),
            contentDescription = "Toggle navigation",
            tint = Color(0xFFA2C9DF),
            modifier = Modifier
                .size(25.dp)
                .rotate(180f)
                .offset(y = (-1).dp)
        )
    }
}
