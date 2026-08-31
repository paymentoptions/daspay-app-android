package com.paymentoptions.pos.ui.screens.logger

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.sendLogsToSdkTeam
import com.paymentoptions.pos.ui.composables._components.ScreenTitleWithCloseButton
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.inputs.BasicTextInput
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.ui.theme.purple50
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun SendLogsScreen(navController: NavHostController) {
    val logMessageState = rememberTextFieldState("")
    var showConfirmation by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    SectionedLayout(
        navController = navController,
        bottomBarContent = BottomBarContent.NOTHING,
        bottomSectionMaxHeightRatio = 0.95f,
        enableScrollingOfBottomSectionContent = true,
        imePaddingEnabled = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenTitleWithCloseButton(
                navController = navController,
                title = "Send Logs",
                fontSize = 24.sp,
                onClose = { navController.popBackStack() }
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Describe the issue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primary900
                )

                Text(
                    text = "Include as much detail as possible to help us diagnose the problem.",
                    fontSize = 14.sp,
                    color = purple50
                )

                BasicTextInput(
                    state = logMessageState,
                    placeholder = "Enter your log message here...",
                    textFieldHeight = 200.dp,
                    maxLength = 1000,
                    lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 5, maxHeightInLines = 10),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            FilledButton(
                text = "Send Log",
                onClick = {
                    focusManager.clearFocus()
                    val message = logMessageState.text.toString()
                    AppLogger.debug("User Log Message: $message")
                    AppLogger.info("Log message captured. Initiating export.")

                    showConfirmation = true
                    sendLogsToSdkTeam()
                    // navController.popBackStack()
                },
                disabled = logMessageState.text.isBlank() || showConfirmation,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )

            if (showConfirmation) {
                Text(
                    text = "Log sent successfully!",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                
                LaunchedEffect(Unit) {
                    delay(2.seconds)
                    navController.popBackStack()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
