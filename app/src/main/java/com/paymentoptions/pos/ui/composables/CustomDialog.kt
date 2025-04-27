import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun CustomDialog(
    showDialog: Boolean,
    title: String,
    text: String,
    acceptButtonText: String = "OK",
    cancelButtonText: String = "Cancel",
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
): Unit {

    if (showDialog) {
        AlertDialog(
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = onAccept) {
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


 