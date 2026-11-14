@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.paymentoptions.pos.ui.composables._components.images

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.paymentoptions.pos.ui.theme.AppTheme
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UIImageRenderingMode
import platform.UIKit.UIImageView
import platform.UIKit.UIViewContentMode

/**
 * As specified: the real Apple SF Symbol "wave.3.right.circle.fill", via native UIKit interop
 * (not a Compose bitmap) — matches the iOS platform convention the icon is meant to evoke.
 */
@Composable
actual fun TapToPayButtonIcon(modifier: Modifier) {
    val tint = AppTheme.colorScheme.primary
    UIKitView(
        factory = {
            val imageView = UIImageView()
            val symbol = UIImage.systemImageNamed("wave.3.right.circle.fill")
                ?.imageWithRenderingMode(UIImageRenderingMode.UIImageRenderingModeAlwaysTemplate)
            imageView.image = symbol
            imageView.contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
            // NOTE: UIAccessibility's isAccessibilityElement/accessibilityLabel aren't exposed
            // through this project's Kotlin/Native UIKit cinterop bindings (unresolved
            // reference) — accessibility label for this native view is a known follow-up, not
            // part of this placeholder step.
            imageView.tintColor = UIColor(
                red = tint.red.toDouble(),
                green = tint.green.toDouble(),
                blue = tint.blue.toDouble(),
                alpha = tint.alpha.toDouble(),
            )
            imageView
        },
        modifier = modifier,
    )
}
