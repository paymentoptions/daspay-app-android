package com.paymentoptions.pos.ui.composables._components.images

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import com.paymentoptions.pos.tapToPayDisplayName
import com.paymentoptions.pos.ui.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.ttp_placeholder

// TODO(design): placeholder art (see ttp_placeholder.xml) — swap the drawable for Apple's/the
// final Tap to Pay artwork when ready; this call site won't need to change.
@Composable
fun TapToPayImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.ttp_placeholder),
        contentDescription = tapToPayDisplayName,
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(AppTheme.colorScheme.primary),
        modifier = modifier
    )
}
