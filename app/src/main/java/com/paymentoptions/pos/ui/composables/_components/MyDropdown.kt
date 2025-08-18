import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.R
import com.paymentoptions.pos.ui.composables._components.screentitle.ScreenTitleWithCloseButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.primary500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDropdown(
    navController: NavController,
    filters: Map<String, String>,
    selectedFilter: Map.Entry<String, String>,
    onFilterChange: (Map.Entry<String, String>) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (filters.isNotEmpty()) {
        Row(
            modifier = modifier
                .border(borderThin, shape = RoundedCornerShape(4.dp))
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

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                painter = painterResource(R.drawable.down_arrow),
                contentDescription = "Calender",
            )
        }
    }

    if (expanded) ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = { expanded = false },
        sheetState = sheetState,
        containerColor = Color.White,
        contentColor = primary500,
        dragHandle = {}) {

        ScreenTitleWithCloseButton(
            navController = navController,
            title = "Transaction Period",
            onClose = { expanded = false },
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP,
                    vertical = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
                )
                .background(Color.White)
        )

        filters.entries.forEachIndexed { index, option ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP,
                            end = DEFAULT_BOTTOM_SECTION_PADDING_IN_DP.plus(10.dp)
                        )
                        .clickable {
                            onFilterChange(option)
//                            expanded = false
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = option.value,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500
                    )

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.White, shape = RoundedCornerShape(50))
                            .border(borderThin, shape = RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedFilter == option) Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(primary500, shape = RoundedCornerShape(50))
                        )
                    }
                }

                if (index != filters.entries.size - 1) HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 2.dp,
                    color = Color.LightGray.copy(alpha = 0.2f)
                )
            }
        }
    }
}
