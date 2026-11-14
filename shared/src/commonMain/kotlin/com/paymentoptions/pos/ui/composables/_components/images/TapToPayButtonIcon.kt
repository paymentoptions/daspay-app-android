package com.paymentoptions.pos.ui.composables._components.images

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The actionable Tap to Pay button's icon — platform-specific per 5.5's suggestion: iOS renders
 * Apple's native SF Symbol via interop (see the iosMain actual), other platforms render the
 * shared placeholder artwork ([ttp_placeholder]/[TapToPayImage]).
 */
@Composable
expect fun TapToPayButtonIcon(modifier: Modifier = Modifier)
