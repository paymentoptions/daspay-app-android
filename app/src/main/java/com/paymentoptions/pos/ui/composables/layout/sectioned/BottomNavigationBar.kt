package com.paymentoptions.pos.ui.composables.layout.sectioned

import CustomDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Money
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.SignOutResponse
import com.paymentoptions.pos.services.apiService.endpoints.signOut
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import kotlinx.coroutines.launch

data class BottomNavigationBarItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
)

val home = BottomNavigationBarItem(
    title = "Home", icon = Icons.Outlined.Dashboard, route = Screens.Dashboard.route
)

val foodMenu = BottomNavigationBarItem(
    title = "Food Menu", icon = Icons.Outlined.Fastfood, route = Screens.FoodMenu.route
)

val receiveMoney = BottomNavigationBarItem(
    title = "Receive Money",
    icon = Icons.Outlined.Money,
    route = Screens.ReceiveMoney.route,
)

val notifications = BottomNavigationBarItem(
    title = "Notifications",
    icon = Icons.Outlined.Notifications,
    route = Screens.Notifications.route
)

val more = BottomNavigationBarItem(
    title = "More", icon = Icons.Outlined.MoreHoriz, route = "More"
)

val itemsInMore = listOf<BottomNavigationBarItem>(
    BottomNavigationBarItem(
        title = "Transaction History",
        icon = Icons.Outlined.CreditCard,
        route = Screens.TransactionHistory.route
    ), BottomNavigationBarItem(
        title = "Refund", icon = Icons.Outlined.MoneyOff, route = Screens.Refund.route
    ), BottomNavigationBarItem(
        title = "Settings", icon = Icons.Outlined.Settings, route = Screens.Settings.route
    ), BottomNavigationBarItem(
        title = "Help & Support", icon = Icons.Outlined.Info, route = Screens.HelpAndSupport.route
    )
)

@Composable
fun Item(
    item: BottomNavigationBarItem,
    selected: BottomNavigationBarItem,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    maxLines: Int = 1,
) {
//    val isSelected = selected == item

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.clickable {
            onSelected()
        }, verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(50))
                .background(iconBackgroundColor), contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(24.dp),
                tint = primary500
            )
        }

        Text(
            item.title,
            textAlign = TextAlign.Center,
            minLines = minLines,
            maxLines = maxLines,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = primary500
        )
    }
}

@Composable
fun MyBottomNavigationBar(
    navController: NavController,
    showMoreItems: Boolean = false,
    onClickShowMoreItems: () -> Unit,
    bottomNavigationBarHeightInDp: Dp = 70.dp,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showSignOutConfirmationDialog by remember { mutableStateOf(false) }
    var signOutLoader by remember { mutableStateOf(false) }
    var signOutResponse: SignOutResponse? = null
    var selected by remember { mutableStateOf<BottomNavigationBarItem>(home) }

    CustomDialog(
        showDialog = showSignOutConfirmationDialog,
        title = "Confirmation",
        text = "Do you want to log out?",
        acceptButtonText = "Log Out",
        onAcceptFn = {
            scope.launch {
                signOutLoader = true

                try {
                    signOutResponse = signOut(context)
                    println("signOutResponse: $signOutResponse")

                    if (signOutResponse == null) {
                        navController.navigate(Screens.SignIn.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }

                    signOutResponse?.let {
                        if (it.success) {
                            SharedPreferences.clearSharedPreferences(context)
                            navController.navigate(Screens.SignIn.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                } catch (e: Exception) {
//                            Toast.makeText(context, "Unable to sign out", Toast.LENGTH_LONG).show()

                    SharedPreferences.clearSharedPreferences(context)
                    navController.navigate(Screens.SignIn.route) {
                        popUpTo(0) { inclusive = true }
                    }

                    println("Error: ${e.toString()}")
                } finally {
                    signOutLoader = false
                }
            }

            showSignOutConfirmationDialog = false
        },
        onDismissFn = { showSignOutConfirmationDialog = false })

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
                            containerColor = Color.White.copy(alpha = 0.1f),
                        ),
                        border = BorderStroke(2.dp, primary100.copy(alpha = 0.2f)),
                    ) {

                        Item(
                            itemsInMore[it],
                            more,
                            onSelected = { navController.navigate(itemsInMore[it].route) },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            minLines = 2,
                            maxLines = 2
                        )
                    }
                }

                item {
                    OutlinedCard(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.1f),
                        ),
                        border = BorderStroke(2.dp, primary100.copy(alpha = 0.2f)),
                    ) {

                        Item(
                            BottomNavigationBarItem(
                                title = "Log Out",
                                icon = Icons.AutoMirrored.Outlined.Logout,
                                route = Screens.SignOut.route
                            ),
                            more,
                            onSelected = { showSignOutConfirmationDialog = true },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            minLines = 2,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(bottomNavigationBarHeightInDp)
                .background(Color.White),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Item(home, selected, onSelected = {
                if (selected != home) {
                    selected = home
                    navController.navigate(selected.route)
                }
            })

            Item(foodMenu, selected, onSelected = {
                if (selected != foodMenu) {
                    selected = foodMenu
                    navController.navigate(selected.route)
                }
            })

            Item(receiveMoney, selected, onSelected = {
                if (selected != receiveMoney) {
                    selected = receiveMoney
                    navController.navigate(selected.route)
                }
            })

            Item(notifications, selected, onSelected = {
                if (selected != notifications) {
                    selected = notifications
                    navController.navigate(selected.route)
                }
            })

            Item(more, selected, onSelected = {
                selected = more
                onClickShowMoreItems()
//            navController.navigate(selected.route)
            })
        }
    }
}