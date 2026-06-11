package com.paymentoptions.pos.ui.composables._components.images.apms

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.diners_club_apms

@Composable
fun DinersClubPayImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.diners_club_apms),
        contentDescription = "Diners Club",
        modifier = modifier
    )
}
