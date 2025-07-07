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

//fun DrawScope.createOvalBottomPath(): Path {
//    val path = Path()
//    path.moveTo(0f, 0f) // Top-left corner
//    path.lineTo(size.width, 0f) // Top-right corner
//    path.lineTo(size.width, size.height - size.width / 2) // Bottom-right corner (before oval)
//    path.quadraticBezierTo(size.width / 2, size.height, 0f, size.height - size.width / 2) // Oval bottom-left
//    path.close()
//    return path
//}

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