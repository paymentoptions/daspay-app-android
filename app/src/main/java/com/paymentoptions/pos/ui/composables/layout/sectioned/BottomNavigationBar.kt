package com.paymentoptions.pos.ui.composables.layout.sectioned

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Money
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.primary100

data class BottomNavigationBarItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String,
)

val home = BottomNavigationBarItem(
    title = "Home",
    selectedIcon = Icons.Default.Dashboard,
    unselectedIcon = Icons.Outlined.Dashboard,
    route = Screens.Dashboard.route
)

val foodMenu = BottomNavigationBarItem(
    title = "Food Menu",
    selectedIcon = Icons.Default.Fastfood,
    unselectedIcon = Icons.Outlined.Fastfood,
    route = Screens.FoodMenu.route
)

val receiveMoney = BottomNavigationBarItem(
    title = "Receive Money",
    selectedIcon = Icons.Default.Money,
    unselectedIcon = Icons.Outlined.Money,
    route = Screens.ReceiveMoney.route,
)

val notifications = BottomNavigationBarItem(
    title = "Notifications",
    selectedIcon = Icons.Default.Notifications,
    unselectedIcon = Icons.Outlined.Notifications,
    route = Screens.Notifications.route
)

val more = BottomNavigationBarItem(
    title = "More",
    selectedIcon = Icons.Default.MoreHoriz,
    unselectedIcon = Icons.Outlined.MoreHoriz,
    route = "More"
)

val itemsInMore = listOf<BottomNavigationBarItem>(
    BottomNavigationBarItem(
        title = "Transaction History",
        selectedIcon = Icons.Default.CreditCard,
        unselectedIcon = Icons.Outlined.CreditCard,
        route = Screens.TransactionHistory.route
    ),
    BottomNavigationBarItem(
        title = "Refund",
        selectedIcon = Icons.Default.MoneyOff,
        unselectedIcon = Icons.Outlined.MoneyOff,
        route = Screens.Refund.route
    ),
    BottomNavigationBarItem(
        title = "Settings",
        selectedIcon = Icons.Default.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        route = Screens.Settings.route
    ),
    BottomNavigationBarItem(
        title = "Help & Support",
        selectedIcon = Icons.Default.Info,
        unselectedIcon = Icons.Outlined.Info,
        route = Screens.HelpAndSupport.route
    ),
    BottomNavigationBarItem(
        title = "Log Out",
        selectedIcon = Icons.Default.Logout,
        unselectedIcon = Icons.Outlined.Logout,
        route = Screens.SignOut.route
    ),
)

@Composable
fun Item(
    item: BottomNavigationBarItem,
    selected: BottomNavigationBarItem,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val icon = if (selected == item) item.selectedIcon else item.unselectedIcon

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        IconButton(
            onClick = onSelected, colors = IconButtonDefaults.iconButtonColors().copy(
                contentColor = primary100
            )
        ) {
            Icon(
                imageVector = icon, contentDescription = item.title, modifier = Modifier.size(24.dp)
            )
        }
        Text(
            item.title,
            textAlign = TextAlign.Center,
            minLines = 2,
            maxLines = 2,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = primary100
        )
    }
}

@Composable
fun MyBottomNavigationBar(
    navController: NavController,
    showMoreItems: Boolean = false,
    onClickShowMoreItems: () -> Unit,
) {
    var selected by remember { mutableStateOf<BottomNavigationBarItem>(home) }

    Column {
        if (showMoreItems) Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                items(itemsInMore.size) {

                    OutlinedCard(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        border = BorderStroke(2.dp, primary100.copy(alpha = 0.2f)),
                    ) {

                        Item(
                            itemsInMore[it],
                            more,
                            onSelected = {
                                navController.navigate(itemsInMore[it].route)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Top,
        ) {

            Item(home, selected, onSelected = {
                selected = home
                navController.navigate(selected.route)
            })

            Item(foodMenu, selected, onSelected = {
                selected = foodMenu
                navController.navigate(selected.route)
            })

            Item(receiveMoney, selected, onSelected = {
                selected = receiveMoney
                navController.navigate(selected.route)
            })

            Item(notifications, selected, onSelected = {
                selected = notifications
                navController.navigate(selected.route)
            })

            Item(more, selected, onSelected = {
                selected = more
                onClickShowMoreItems()
//            navController.navigate(selected.route)
            })
        }
    }
}