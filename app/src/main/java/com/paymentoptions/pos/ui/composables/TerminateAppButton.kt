package com.paymentoptions.pos.ui.composables

import android.app.Activity
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun TerminateAppButton() {
    val context = LocalContext.current
    val activity = context as? Activity

    Button(onClick = {
        activity?.finish()
    }) {
        Text("Exit App")
    }
}
