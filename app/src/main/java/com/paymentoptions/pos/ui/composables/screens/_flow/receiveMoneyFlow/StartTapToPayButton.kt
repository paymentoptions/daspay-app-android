package com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.R

// Sampled directly from the reference PNG (darkest pixel in each of text/icon/
// chevron converged to ~#0228D8) — slightly different from the XML assets'
// baked-in #1554E8/#1557E8. Using the sampled value here for the closest visual
// match; update the XML strokeColor too if you want the drawables to match exactly.
private val AccentBlue = Color(0xFF0228D8)

@Composable
fun StartTapToPayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pillShape = RoundedCornerShape(32.dp)
    val fillBrush = Brush.verticalGradient(
        colors = listOf(Color.White, Color(0xFFA4D4FC)),
    )
    val borderBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF1082FB), Color(0xFF0257FB)),
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 24.dp)
            .shadow(
                elevation = 5.dp,
                shape = pillShape,
                ambientColor = Color(0xFF0F82FB),
                spotColor = Color(0xFF0F82FB),
            )
            .background(fillBrush, pillShape),
        shape = pillShape,
        color = Color.Transparent,
        border = BorderStroke(width = 2.5.dp, brush = borderBrush),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_daspay_nfc_wave),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = "Start Tap to Pay",
                color = AccentBlue,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Image(
                painter = painterResource(id = R.drawable.ic_right_chevron),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}