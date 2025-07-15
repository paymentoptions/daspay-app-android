import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.R
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.borderThick

@Composable
fun MyDropdown(
    filters: Map<String, String>,
    selectedFilter: Map.Entry<String, String>,
    onFilterChange: (Map.Entry<String, String>) -> Unit,
    icon: ImageVector? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    if (filters.isNotEmpty()) Box {

        Row(
            modifier = Modifier
                .fillMaxHeight()
                .border(borderThick)
                .padding(vertical = 4.dp, horizontal = 12.dp)
                .clickable(onClick = { expanded = true }),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (icon != null) Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "Calender",
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = selectedFilter.key,
                style = AppTheme.typography.titleNormal.copy(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.width(70.dp))

            Icon(
                painter = painterResource(R.drawable.down_arrow),
                contentDescription = "Calender",
            )
        }

//            OutlinedButton(
//                text = selectedFilterValue,
//                onClick = { expanded = true },
//            )

        DropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false }) {
            filters.forEach { option ->
                DropdownMenuItem(text = { Text(option.value) }, onClick = {
                    onFilterChange(option)
                    expanded = false
                })
            }
        }
    }
}
