/**package com.paymentoptions.pos.ui.composables.screens.splash

import android.os.Handler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables._components.images.LogoImage
import com.paymentoptions.pos.ui.composables.layout.sectioned.LOGO_HEIGHT_IN_DP
import com.paymentoptions.pos.ui.composables.layout.simple.SimpleLayout
import com.paymentoptions.pos.ui.composables.navigation.Screens

@Composable
fun SplashScreen(navController: NavController) {
    Handler().postDelayed({
        navController.navigate(Screens.AuthCheck.route)
    }, 1000)

    SimpleLayout {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            LogoImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LOGO_HEIGHT_IN_DP)
            )
        }
    }
}**/

package com.paymentoptions.pos.ui.composables.screens.splash

import android.os.Handler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.paymentoptions.pos.R
import com.paymentoptions.pos.ui.composables.layout.sectioned.LOGO_HEIGHT_IN_DP
import com.paymentoptions.pos.ui.composables.layout.simple.SimpleLayout
import com.paymentoptions.pos.ui.composables.navigation.Screens

@Composable
fun SplashScreen(navController: NavController) {
    Handler().postDelayed({
        navController.navigate(Screens.AuthCheck.route)
    }, 4000)

    SimpleLayout {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = R.drawable.daspay_loader_gif_dark,
                contentDescription = "Loading Animation",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(150.dp) // Square size of 150dp x 150dp
            )
        }
    }
}