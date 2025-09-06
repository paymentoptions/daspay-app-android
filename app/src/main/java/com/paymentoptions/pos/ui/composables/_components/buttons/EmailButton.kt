package com.paymentoptions.pos.ui.composables._components.buttons

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.paymentoptions.pos.ui.composables._components.inputs.BasicTextInput
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.utils.validation.validateEmail

data class Email(
    val id: String = "",
    val subject: String = "Shared via DASPay",
    val text: String = "",
)

@Composable
fun EmailButton(text: String, email: Email, modifier: Modifier = Modifier) {

    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    val emailState = rememberTextFieldState(initialText = email.id)
    var emailError by remember { mutableStateOf(false) }

    LaunchedEffect(emailState.text) {
        emailError = !validateEmail(emailState.text.toString())
    }

    fun emailAction() {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_EMAIL, arrayOf<String>(emailState.text.toString()))
            putExtra(Intent.EXTRA_SUBJECT, email.subject)
            putExtra(Intent.EXTRA_TEXT, email.text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(context, shareIntent, null)
    }

    if (showDialog) AlertDialog(
        text = {
            BasicTextInput(
                state = emailState,
                label = "Recipient's email id",
                placeholder = "Enter Email",
                isError = emailError,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .focusable(),
                maxLength = 40,
            )
        }, confirmButton = {
            FilledButton(
                text = "Send", disabled = emailError, onClick = {
                    emailAction()
                    showDialog = false
                }, modifier = Modifier.fillMaxWidth()
            )
        }, dismissButton = {
            OutlinedButton(
                text = "Cancel",
                onClick = { showDialog = false },
                modifier = Modifier.fillMaxWidth()
            )
        }, onDismissRequest = { showDialog = false }, containerColor = Color.White
    )

    Column(
        modifier = modifier.clickable {
            showDialog = true
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Outlined.Email,
            contentDescription = "Share via Email",
            modifier = Modifier
                .background(
                    iconBackgroundColor, shape = RoundedCornerShape(50)
                )
                .padding(16.dp)
        )



        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            color = primary900,
        )
    }
}