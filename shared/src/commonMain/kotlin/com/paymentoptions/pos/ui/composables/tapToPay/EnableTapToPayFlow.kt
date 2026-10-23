package com.paymentoptions.pos.ui.composables.tapToPay

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.payment.reader.ReaderState
import com.paymentoptions.pos.payment.reader.getTapToPayReader
import com.paymentoptions.pos.tapToPayDisplayName
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.buttons.OutlinedButton
import com.paymentoptions.pos.ui.composables._components.dialogs.MyDialog
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import kotlinx.coroutines.launch

/**
 * Single reusable flow for Apple's Tap to Pay on iPhone account-linking (Terms & Conditions),
 * shared by every entry point (post-login prompt, Settings, checkout) so they all behave
 * identically. Placeholder HIG-style UI — final designs come later.
 *
 * Only [DPStorageManager.isAdmin] users can actually start linking; staff just see a message
 * pointing them at their admin (3.8/3.8.1).
 *
 * @param onLinked Hook called once, right after the reader reports [ReaderState.Ready] following
 * a successful link — reserved for merchant education content (step 4); currently a no-op.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnableTapToPayFlow(
    onDismiss: () -> Unit,
    onLinked: () -> Unit = {},
) {
    if (!DPStorageManager.isAdmin()) {
        MyDialog(
            showDialog = true,
            title = tapToPayDisplayName,
            text = "Ask your account admin to enable $tapToPayDisplayName.",
            acceptButtonText = "OK",
            showCancelButton = false,
            onAcceptFn = onDismiss,
            onDismissFn = onDismiss,
        )
        return
    }

    val reader = remember { getTapToPayReader() }
    val readerState by reader.state.collectAsState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var hasStartedLinking by remember { mutableStateOf(false) }
    var reportedLinked by remember { mutableStateOf(false) }

    LaunchedEffect(readerState, hasStartedLinking) {
        if (hasStartedLinking && readerState is ReaderState.Ready && !reportedLinked) {
            reportedLinked = true
            onLinked()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        contentColor = primary500,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when {
                hasStartedLinking && (readerState is ReaderState.Initializing || readerState is ReaderState.Configuring) -> {
                    val progress = (readerState as? ReaderState.Configuring)?.progress
                    Text(
                        text = "$tapToPayDisplayName is getting ready" +
                            (progress?.let { "… $it%" } ?: "…"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = primary900,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }

                hasStartedLinking && readerState is ReaderState.Ready -> {
                    Text(
                        text = "You're all set!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = primary900,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "$tapToPayDisplayName is ready to use.",
                        fontSize = 15.sp,
                        color = primary500,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FilledButton(
                        text = "Done",
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                    )
                }

                hasStartedLinking && readerState is ReaderState.Failed -> {
                    val message = (readerState as ReaderState.Failed).message
                    Text(
                        text = "Couldn't enable $tapToPayDisplayName",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = primary900,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = message, fontSize = 15.sp, color = primary500)
                    Spacer(modifier = Modifier.height(16.dp))
                    FilledButton(
                        text = "Try Again",
                        onClick = { hasStartedLinking = false },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        text = "Close",
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                    )
                }

                else -> {
                    Text(
                        text = tapToPayDisplayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = primary900,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "To start accepting contactless payments, you'll need to agree to Apple's terms for $tapToPayDisplayName.",
                        fontSize = 15.sp,
                        color = primary500,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FilledButton(
                        text = "Enable",
                        onClick = {
                            reportedLinked = false
                            hasStartedLinking = true
                            scope.launch { reader.linkAccount() }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        text = "Not Now",
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                    )
                }
            }
        }
    }
}
