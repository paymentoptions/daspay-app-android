import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.buttons.OutlinedButton
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.primary500

@Composable
fun CustomDialog(
    showDialog: Boolean,
    title: String,
    text: String,
    acceptButtonText: String = "OK",
    cancelButtonText: String = "Cancel",
    showAcceptButton: Boolean = true,
    showCancelButton: Boolean = true,
    onAcceptFn: () -> Unit,
    onDismissFn: () -> Unit,
) {

    if (showDialog) {
        AlertDialog(
            title = {
                Text(
                    title,
                    style = AppTheme.typography.titleNormal.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text,
                    color = primary500,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(top = 10.dp, bottom = 20.dp)
                )
            },
            confirmButton = {
                if (showAcceptButton) FilledButton(
                    text = acceptButtonText,
                    onClick = onAcceptFn,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                if (showCancelButton) OutlinedButton(
                    text = cancelButtonText,
                    onClick = onDismissFn,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            onDismissRequest = onDismissFn,
        )
    }

}


