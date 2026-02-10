package com.paymentoptions.pos.logger

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color

@Composable
fun SendLogsScreen(navController: NavHostController) {
    var logMessage by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(Color.White)
        ,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Send Log Message", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = logMessage,
            minLines = 5,
            onValueChange = { logMessage = it },
            label = { Text("Log Message") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                AppLogger.debug(logMessage)
                showConfirmation = true
                ExportLogs.sendLogsToSdkTeam(context)
                navController.popBackStack()
            },
            enabled = logMessage.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send Log")
        }
        if (showConfirmation) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Log sent successfully!", color = MaterialTheme.colorScheme.primary)
        }
    }
}