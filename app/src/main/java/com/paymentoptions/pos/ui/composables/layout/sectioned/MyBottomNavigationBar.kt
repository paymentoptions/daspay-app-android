package com.paymentoptions.pos.ui.composables.layout.sectioned

import MyDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Money
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.R
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.SignOutResponse
import com.paymentoptions.pos.services.apiService.endpoints.signOut
import com.paymentoptions.pos.ui.composables._components.BottomNavShape
import com.paymentoptions.pos.ui.composables._components.MyElevatedCard
import com.paymentoptions.pos.ui.composables.navigation.Screens
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import kotlinx.coroutines.launch

val BOTTOM_NAVIGATION_HEIGHT_IN_DP = 75.dp

data class BottomNavigationBarItem(
    val title: String,
    val icon: ImageVector,
    val svgIcon: Int? = null,
    val route: String,
    val hideIcon: Boolean = false,
)

val home = BottomNavigationBarItem(
    title = "Home", icon = Icons.Outlined.Dashboard, route = Screens.Dashboard.route
)

val foodMenu = BottomNavigationBarItem(
    title = "Food Menu", icon = Icons.Outlined.Fastfood, route = Screens.FoodOrderFlow.route
)

val receiveMoney = BottomNavigationBarItem(
    title = "Receive Money",
    icon = Icons.Outlined.Money,
    route = Screens.ReceiveMoneyFlow.route,
    hideIcon = true
)

val notifications = BottomNavigationBarItem(
    title = "Notifications",
    icon = Icons.Outlined.Notifications,
    route = Screens.Notifications.route
)

val more = BottomNavigationBarItem(
    title = "More", icon = Icons.Outlined.MoreHoriz, route = "More"
)

val refund = BottomNavigationBarItem(
    title = "Refund",
    icon = Icons.Outlined.MoneyOff,
    svgIcon = R.drawable.refund,
    route = Screens.Refund.route
)

val transactionHistory = BottomNavigationBarItem(
    title = "Transaction History",
    icon = Icons.Outlined.CreditCard,
    route = Screens.TransactionHistory.route
)

val settlement = BottomNavigationBarItem(
    title = "Settlement", icon = Icons.Outlined.Handshake, route = Screens.Settlement.route
)

val settings = BottomNavigationBarItem(
    title = "Settings", icon = Icons.Outlined.Settings, route = Screens.Settings.route
)

val helpAndSupport = BottomNavigationBarItem(
    title = "Help & Support", icon = Icons.Outlined.Info, route = Screens.HelpAndSupport.route
)

val itemsInMore = listOf<BottomNavigationBarItem>(
    transactionHistory,
//    notifications,
    settlement,
    settings,
    helpAndSupport,
)

var selectedBottomNavigationBarItem: BottomNavigationBarItem = home

@Composable
fun MyBottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier,
    showMoreItems: Boolean = false,
    onClickShowMoreItems: () -> Unit,
    bottomNavigationBarHeightInDp: Dp = BOTTOM_NAVIGATION_HEIGHT_IN_DP,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showSignOutConfirmationDialog by remember { mutableStateOf(false) }
    var signOutLoader by remember { mutableStateOf(false) }
    var signOutResponse: SignOutResponse? = null
//    var selected by remember { mutableStateOf<BottomNavigationBarItem>(home) }

    MyDialog(
        showDialog = showSignOutConfirmationDialog,
        title = "Confirmation Required",
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

    Column(modifier = modifier) {

        val modifier1 = if (showMoreItems) Modifier.clip(
            BottomNavShape(
                cornerRadius = with(LocalDensity.current) { 20.dp.toPx() },
                dockRadius = with(LocalDensity.current) { 38.dp.toPx() },
            )
        ) else Modifier

        if (showMoreItems) Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .padding(bottom = 30.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                items(itemsInMore.size) {

                    MyElevatedCard {
                        Item(
                            itemsInMore[it],
                            more,
                            onSelected = { navController.navigate(itemsInMore[it].route) },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            minLines = 2,
                            maxLines = 2,
                            inMore = true
                        )
                    }
                }

                item {
                    MyElevatedCard {
                        Item(
                            BottomNavigationBarItem(
                                title = "Log Out",
                                icon = Icons.AutoMirrored.Outlined.Logout,
                                route = "Dummy"
                            ),
                            more,
                            onSelected = { showSignOutConfirmationDialog = true },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            minLines = 2,
                            maxLines = 2,
                            inMore = true
                        )
                    }
                }
            }
        }

        Row(
            modifier = modifier1
                .fillMaxWidth()
                .height(bottomNavigationBarHeightInDp)
                .background(Color.White),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Item(
                home,
                selectedBottomNavigationBarItem,
                modifier = Modifier.weight(1f),
                onSelected = {
//                    if (selectedBottomNavigationBarItem !== home) {
                    selectedBottomNavigationBarItem = home
                    navController.navigate(selectedBottomNavigationBarItem.route)
//                    }
                })

            Item(
                foodMenu,
                selectedBottomNavigationBarItem,
                modifier = Modifier.weight(1f),
                onSelected = {
//                    if (selectedBottomNavigationBarItem !== foodMenu) {
                    selectedBottomNavigationBarItem = foodMenu
                    navController.navigate(selectedBottomNavigationBarItem.route)
//                    }
                })

            Item(
                receiveMoney,
                selectedBottomNavigationBarItem,
                modifier = Modifier.weight(1.5f),
                onSelected = {
//                    if (selectedBottomNavigationBarItem !== receiveMoney) {
                    selectedBottomNavigationBarItem = receiveMoney
                    navController.navigate(selectedBottomNavigationBarItem.route)
//                    }
                })

            Item(
                refund,
                selectedBottomNavigationBarItem,
                modifier = Modifier.weight(1f),
                onSelected = {
                    selectedBottomNavigationBarItem = refund
                    navController.navigate(selectedBottomNavigationBarItem.route)
                })

            Item(
                more,
                selectedBottomNavigationBarItem,
                modifier = Modifier.weight(1f),
                onSelected = {
                    selectedBottomNavigationBarItem = more
                    onClickShowMoreItems()
                })
        }
    }
}

@Composable
fun Item(
    item: BottomNavigationBarItem,
    selected: BottomNavigationBarItem,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    maxLines: Int = 1,
    inMore: Boolean = false,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.clickable {
            onSelected()
        }, verticalArrangement = Arrangement.Bottom
    ) {

        Box(
            modifier = Modifier
                .size(if (inMore) 51.dp else 39.dp)
                .clip(RoundedCornerShape(50))
                .background(if (item.hideIcon || !inMore) Color.Transparent else iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            if (!item.hideIcon) if (item.svgIcon != null) Icon(
                painter = painterResource(R.drawable.refund),
                contentDescription = item.title,
                modifier = Modifier.size(24.dp),
                tint = primary500
            )
            else Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(24.dp),
                tint = primary500
            )
        }

        Spacer(modifier = Modifier.height(if (inMore) 8.dp else 4.dp))

        Text(
            item.title,
            textAlign = TextAlign.Center,
            minLines = minLines,
            maxLines = maxLines,
            fontSize = if (inMore) 14.sp else 12.sp,
            fontWeight = if (inMore) FontWeight.Normal else FontWeight.SemiBold,
            color = if (item.hideIcon) primary100 else primary500,
        )
    }
}