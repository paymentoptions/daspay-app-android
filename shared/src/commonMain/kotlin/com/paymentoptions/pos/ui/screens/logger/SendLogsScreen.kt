package com.paymentoptions.pos.ui.screens.logger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.sendLogsToSdkTeam
import com.paymentoptions.pos.ui.theme.AppTheme

@Composable
fun SendLogsScreen(navController: NavHostController) {
    var logMessage by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Send Log Message", style = AppTheme.typography.titleLarge)
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

                // Include build metadata to help correlate exported logs with app/runtime config.
                // Note: Build metadata like VERSION_NAME, etc. are currently platform-specific in this project.
                // We'll just log the message for now, and platform-specific info can be added to the platform's sendLogsToSdkTeam if needed.
                AppLogger.info("Log message captured. Initiating export.")

                showConfirmation = true
                sendLogsToSdkTeam()
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
