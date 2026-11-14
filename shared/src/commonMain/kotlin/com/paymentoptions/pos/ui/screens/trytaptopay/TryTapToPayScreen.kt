package com.paymentoptions.pos.ui.screens.trytaptopay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.tapToPayDisplayName
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.images.TapToPayImage
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.composables.layout.simple.SimpleLayout
import com.paymentoptions.pos.ui.navigation.Screens
import com.paymentoptions.pos.ui.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.ttp_try_it_out_body
import paymentoptionspos.shared.generated.resources.ttp_try_it_out_cta
import paymentoptionspos.shared.generated.resources.ttp_try_it_out_title

/**
 * Post-setup "try it out" screen (3.9). Placeholder visuals only — not yet wired into any
 * trigger point (e.g. right after [com.paymentoptions.pos.ui.composables.tapToPay.EnableTapToPayFlow]'s
 * `onLinked` hook); a later step decides when this is shown. Reachable via
 * [com.paymentoptions.pos.ui.navigation.Screens.TryTapToPay] for now.
 */
@Composable
fun TryTapToPayScreen(navController: NavController) {
    SimpleLayout {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // TODO(design): placeholder illustration — swap for the final "try it out" artwork.
            TapToPayImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(200.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(Res.string.ttp_try_it_out_title),
                style = AppTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(Res.string.ttp_try_it_out_body, tapToPayDisplayName),
                style = AppTheme.typography.body,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            FilledButton(
                text = stringResource(Res.string.ttp_try_it_out_cta),
                onClick = {
                    navController.navigate(Screens.ReceiveMoneyFlow.route)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(59.dp),
            )
        }
    }
}
