import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.ui.theme.Orange10

@Composable
fun CustomDropdown(
    filters: Map<String, String>,
    selectedFilterValue: String,
    onFilterChange: (String, String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    if (filters.isNotEmpty())
        Box {
            Button(
                onClick = { expanded = true },
                colors = ButtonDefaults.buttonColors(containerColor = Orange10)
            ) {
                Text(selectedFilterValue, color = Color.White, fontSize = 12.sp)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filters.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.value) },
                        onClick = {
                            onFilterChange(option.key, option.value)
                            expanded = false
                        }
                    )
                }
            }
        }
}
