import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextField(label: String, value: String, onValueChange: (String) -> Unit) {
    val textColor = Color(0xFFEFEFEF)
    val background = Color(0xFF121017)

    OutlinedTextField(
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        label = { Text(label, color = Color.Gray) },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        textStyle = TextStyle(
            color = textColor,
            fontSize = 12.sp,
            fontStyle = FontStyle.Normal,
            textDecoration = TextDecoration.None
        ),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = background,
            focusedContainerColor = background,
            focusedIndicatorColor = Color(0xFFFF9800),
            focusedLabelColor = Color(0xFFFF9800),
            unfocusedLabelColor = Color.DarkGray,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            errorContainerColor = Color.White,
        )
    )

}

@Composable
fun TextInputDialog(
    showDialog: Boolean,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    title: String,
    acceptButtonText: String = "OK",
    cancelButtonText: String = "Cancel",
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
): Unit {

    if (showDialog) {
        AlertDialog(
            title = { Text(title) },
            text = { TextField(label, value, onValueChange) },
            confirmButton = {
                TextButton(onClick = onAccept, enabled = value !== "") {
                    Text(acceptButtonText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {

                    Text(cancelButtonText)
                }
            },
            onDismissRequest = onDismiss,
        )
    }

}


 