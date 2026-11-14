package com.paymentoptions.pos.ui.screens.taptopayawareness

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.tapToPayDisplayName
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.layout.simple.SimpleLayout
import com.paymentoptions.pos.ui.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.ttp_awareness_body
import paymentoptionspos.shared.generated.resources.ttp_awareness_cta
import paymentoptionspos.shared.generated.resources.ttp_awareness_hero_label
import paymentoptionspos.shared.generated.resources.ttp_awareness_title

/**
 * One-time Tap to Pay on iPhone awareness splash (3.1/3.2/3.3). Placeholder visuals only — not
 * yet wired into any trigger point (login/dashboard/etc.); a later step decides when this is
 * shown and to whom. Reachable via [com.paymentoptions.pos.ui.navigation.Screens.TapToPayAwareness]
 * for now.
 */
@Composable
fun TapToPayAwarenessScreen(navController: NavController) {
    SimpleLayout {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // TODO(design): replace this placeholder box with Apple's official Tap to Pay on
            // iPhone hero image/asset once it's available.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.ttp_awareness_hero_label),
                    color = AppTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(Res.string.ttp_awareness_title, tapToPayDisplayName),
                style = AppTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(Res.string.ttp_awareness_body),
                style = AppTheme.typography.body,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            FilledButton(
                text = stringResource(Res.string.ttp_awareness_cta),
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(59.dp),
            )
        }
    }
}
