package com.paymentoptions.pos.device

import Navigator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Device not connected to the Internet",
                color = Color.Red,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    color = Color.DarkGray,
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .size(20.dp)
                )
                Text(
                    text = "Checking internet connectivity status in the background",
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 10.sp)

                )
            }
        }
        onNetworkNotAvailable()
    }
}

