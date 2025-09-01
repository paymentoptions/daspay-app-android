package com.paymentoptions.pos.ui.composables._components.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.green500
import com.paymentoptions.pos.ui.theme.innerShadow
import com.paymentoptions.pos.ui.theme.red500
import com.paymentoptions.pos.utils.modifiers.innerShadow

enum class AlertDialogType {
    ERROR, SUCCESS, WARNING
}

@Composable
fun MyAlertDialog(
    showDialog: Boolean,
    title: String? = null,
    text: String,
    actionButtonText: String = "OK",
    showActionButton: Boolean = true,
    type: AlertDialogType = AlertDialogType.SUCCESS,
    onActionFn: () -> Unit,
) {
    val color = when (type) {
        AlertDialogType.ERROR -> red500
        AlertDialogType.SUCCESS -> green500
        AlertDialogType.WARNING -> Color.Magenta
    }

    val icon = when (type) {
        AlertDialogType.ERROR -> Icons.Default.ErrorOutline
        AlertDialogType.SUCCESS -> Icons.Default.Check
        AlertDialogType.WARNING -> Icons.Default.WarningAmber
    }

    if (showDialog) {
        AlertDialog(
            modifier = Modifier.innerShadow(
                color = innerShadow,
                blur = 10.dp,
                spread = 10.dp,
                cornersRadius = 25.dp,
                offsetX = 0.dp,
                offsetY = 0.dp
            ),
            containerColor = Color.White,
            title = {
                if (title != null) Text(
                    title,
                    style = AppTheme.typography.titleNormal.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)

                            .border(width = 1.dp, color = color, shape = RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = type.toString(),
                            tint = color,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Text(
                        text,
                        color = color,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 10.dp, bottom = 20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                if (showActionButton) Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FilledButton(
                        text = actionButtonText,
                        onClick = onActionFn,
                    )
                }
            },
            dismissButton = {},
            onDismissRequest = onActionFn,
        )
    }

}


