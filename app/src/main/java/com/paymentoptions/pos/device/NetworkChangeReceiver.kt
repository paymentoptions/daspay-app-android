package com.paymentoptions.pos.device

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.paymentoptions.pos.ui.composables.navigation.Navigator
import com.paymentoptions.pos.ui.composables.screens.nonetwork.NoNetworkScreen

class NetworkChangeReceiver(private val onConnectionChanged: (Boolean) -> Unit) :
    BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        val isConnected = activeNetwork?.isConnectedOrConnecting == true
        onConnectionChanged(isConnected)
    }
}

@Composable
fun NetworkStatusComposable(
    onNetworkAvailable: () -> Unit = {},
    onNetworkNotAvailable: () -> Unit = {},
) {
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf(true) }


    DisposableEffect(Unit) {
        val receiver = NetworkChangeReceiver { status ->
            isConnected = status
        }
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(receiver, filter)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    if (isConnected) {
        onNetworkAvailable()
        Navigator()
    } else {
        NoNetworkScreen()
        onNetworkNotAvailable()
    }
}

