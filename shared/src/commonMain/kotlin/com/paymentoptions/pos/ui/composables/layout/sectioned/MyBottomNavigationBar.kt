package com.paymentoptions.pos.ui.composables.layout.sectioned

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
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Filter
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Money
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.paymentoptions.pos.analytics.AnalyticsHelper
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.isDebugBuild
import com.paymentoptions.pos.network.ApiHttpException
import com.paymentoptions.pos.network.SignOutResponse
import com.paymentoptions.pos.network.endpoints.signOut
import com.paymentoptions.pos.services.apiService.TokenAutoRefresher
import com.paymentoptions.pos.ui.composables._components.BottomNavShape
import com.paymentoptions.pos.ui.composables._components.MyElevatedCard
import com.paymentoptions.pos.ui.composables._components.dialogs.MyDialog
import com.paymentoptions.pos.ui.navigation.Screens
import com.paymentoptions.pos.ui.theme.iconBackgroundColor
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.catalog_icon
import paymentoptionspos.shared.generated.resources.query_icon

val BOTTOM_NAVIGATION_HEIGHT_IN_DP = 75.dp

data class BottomNavigationBarItem(
    val title: String,
    val icon: ImageVector,
    val svgIcon: DrawableResource? = null,
    val route: String,
    val hideIcon: Boolean = false,
)

val home = BottomNavigationBarItem(
    title = "Home", icon = Icons.Outlined.Dashboard, route = Screens.Dashboard.route
)

val catalogMenu = BottomNavigationBarItem(
    title = "Catalog",
    svgIcon = Res.drawable.catalog_icon,
    icon = Icons.Outlined.Book,
    route = Screens.FoodOrderFlow.route
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

val query = BottomNavigationBarItem(
    title = "Query",
    svgIcon = Res.drawable.query_icon,
    icon = Icons.Outlined.Filter,
    route = Screens.QueryScreen.route
)

val transactionHistory = BottomNavigationBarItem(
    title = "Transaction History",
    icon = Icons.Outlined.CreditCard,
    route = "${Screens.TransactionHistory.route}?showBarChart=${false}"
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

val sendLogs = BottomNavigationBarItem(
    title = "Send Logs", icon = Icons.Outlined.Info, route = Screens.SendLogs.route
)

val itemsInMoreAdmin = listOf<BottomNavigationBarItem>(
    transactionHistory,
    settlement,
    settings,
    helpAndSupport,
)

val itemsInMoreStaff = listOf<BottomNavigationBarItem>(
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
    val scope = rememberCoroutineScope()
    var showSignOutConfirmationDialog by remember { mutableStateOf(false) }
    var signOutLoader by remember { mutableStateOf(false) }
    var signOutResponse: SignOutResponse? = null

    val moreList: ArrayList<BottomNavigationBarItem> = arrayListOf()
    if (DPStorageManager.isAdmin()) {
        moreList.addAll(itemsInMoreAdmin)
    } else {
        moreList.addAll(itemsInMoreStaff)
    }
    if (isDebugBuild) {
        moreList.add(sendLogs)
    }

    MyDialog(
        showDialog = showSignOutConfirmationDialog,
        title = "Confirmation Required",
        text = "Do you want to log out?",
        acceptButtonText = "Log Out",
        onAcceptFn = {
            AnalyticsHelper.trackCriticalButtonClick(buttonName = "Log Out Confirm", screenName = "More")
            scope.launch {
                signOutLoader = true

                try {
                    signOutResponse = signOut()
                    println("signOutResponse: $signOutResponse")

                    if (signOutResponse == null) {
                        AnalyticsHelper.trackLogout()
                        TokenAutoRefresher.getInstance()?.onUserSignedOut()
                        DPStorageManager.clearSharedPreferences()
                        navController.navigate(Screens.FingerprintScan.route) {
                            popUpTo(Screens.FingerprintScan.route) { inclusive = true }
                        }
                    }

                    signOutResponse?.let {
                        if (it.success) {
                            AnalyticsHelper.trackLogout()
                            TokenAutoRefresher.getInstance()?.onUserSignedOut()
                            DPStorageManager.clearSharedPreferences()
                            navController.navigate(Screens.AuthCheck.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                } catch (e: Exception) {
                    AnalyticsHelper.trackApiError(
                        endpoint = "signOut",
                        statusCode = (e as? ApiHttpException)?.statusCode,
                        message = e.message ?: "Sign out failed",
                        throwable = e,
                    )
                    TokenAutoRefresher.getInstance()?.onUserSignedOut()
                    DPStorageManager.clearSharedPreferences()
                    navController.navigate(Screens.AuthCheck.route) {
                        popUpTo(0) { inclusive = true }
                    }

                    println("Error: $e")
                } finally {
                    signOutLoader = false
                }
            }

            showSignOutConfirmationDialog = false
        },
        onDismissFn = { showSignOutConfirmationDialog = false })

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route.orEmpty()

    val isHomeSelected = currentRoute.startsWith(Screens.Dashboard.route) && !showMoreItems
    val isFoodSelected = currentRoute.startsWith(Screens.FoodOrderFlow.route) && !showMoreItems
    val isReceiveMoneySelected = currentRoute.startsWith(Screens.ReceiveMoneyFlow.route) && !showMoreItems
    val isQuerySelected = currentRoute.startsWith(Screens.QueryScreen.route) && !showMoreItems
    val isMoreRoute = currentRoute.startsWith(Screens.TransactionHistory.route) ||
            currentRoute.startsWith(Screens.Settlement.route) ||
            currentRoute.startsWith(Screens.Settings.route) ||
            currentRoute.startsWith(Screens.HelpAndSupport.route) ||
            currentRoute.startsWith(Screens.SendLogs.route)
    val isMoreSelected = showMoreItems || isMoreRoute

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
                items(moreList.size) {
                    MyElevatedCard {
                        Item(
                            moreList[it],
                            onSelected = { navController.navigate(moreList[it].route) },
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
                            onSelected = {
                                AnalyticsHelper.trackCriticalButtonClick(buttonName = "Log Out", screenName = "More")
                                showSignOutConfirmationDialog = true
                            },
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
                modifier = Modifier.weight(1f),
                isSelected = isHomeSelected,
                onSelected = {
                    val cur = navController.currentBackStackEntry?.destination?.route

                    if (cur != home.route) {
                        AnalyticsHelper.trackDashboardNavigation(section = home.title)
                        selectedBottomNavigationBarItem = home
                        navController.navigate(selectedBottomNavigationBarItem.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.findStartDestination().route ?: Screens.Dashboard.route) {
                                saveState = true
                            }
                        }
                        if (showMoreItems) {
                            onClickShowMoreItems()
                        }
                    }
                })

            Item(
                catalogMenu,
                modifier = Modifier.weight(1f),
                isSelected = isFoodSelected,
                onSelected = {
                    val cur = navController.currentBackStackEntry?.destination?.route

                    if (cur != catalogMenu.route) {
                        AnalyticsHelper.trackDashboardNavigation(section = catalogMenu.title)
                        selectedBottomNavigationBarItem = catalogMenu
                        navController.navigate(selectedBottomNavigationBarItem.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.findStartDestination().route ?: Screens.Dashboard.route) {
                                saveState = true
                            }
                        }
                    } else {
                        AnalyticsHelper.trackDashboardNavigation(section = catalogMenu.title)
                        selectedBottomNavigationBarItem = catalogMenu
                        navController.navigate(catalogMenu.route) {
                            launchSingleTop = true
                            popUpTo(catalogMenu.route) { inclusive = true }
                        }
                    }
                    if (showMoreItems) {
                        onClickShowMoreItems()
                    }
                })

            Item(
                receiveMoney,
                modifier = Modifier.weight(1.5f),
                isSelected = isReceiveMoneySelected,
                onSelected = {
                    val cur = navController.currentBackStackEntry?.destination?.route

                    if (cur != receiveMoney.route) {
                        AnalyticsHelper.trackDashboardNavigation(section = receiveMoney.title)
                        selectedBottomNavigationBarItem = receiveMoney
                        navController.navigate(selectedBottomNavigationBarItem.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.findStartDestination().route ?: Screens.Dashboard.route) {
                                saveState = true
                            }
                        }
                        if (showMoreItems) {
                            onClickShowMoreItems()
                        }
                    }
                })

            Item(
                query,
                modifier = Modifier.weight(1f),
                isSelected = isQuerySelected,
                onSelected = {
                    val cur = navController.currentBackStackEntry?.destination?.route

                    if (cur != query.route) {
                        AnalyticsHelper.trackDashboardNavigation(section = query.title)
                        selectedBottomNavigationBarItem = query
                        navController.navigate(selectedBottomNavigationBarItem.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.findStartDestination().route ?: Screens.Dashboard.route) {
                                saveState = true
                            }
                        }
                        if (showMoreItems) {
                            onClickShowMoreItems()
                        }
                    }
                }
            )

            Item(
                more,
                modifier = Modifier.weight(1f),
                isSelected = isMoreSelected,
                onSelected = {
                    val cur = navController.currentBackStackEntry?.destination?.route

                    if (cur != more.route) {
                        AnalyticsHelper.trackDashboardNavigation(section = more.title)
                        selectedBottomNavigationBarItem = more
                        onClickShowMoreItems()
                    }
                })
        }
    }
}

@Composable
fun Item(
    item: BottomNavigationBarItem,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    maxLines: Int = 1,
    inMore: Boolean = false,
    isSelected: Boolean = false,
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
                .background(
                    when {
                        inMore -> iconBackgroundColor
                        item.hideIcon -> Color.Transparent
                        else -> Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!item.hideIcon) if (item.svgIcon != null) Icon(
                painter = painterResource(item.svgIcon),
                contentDescription = item.title,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) primary100 else primary500
            )
            else Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) primary100 else primary500
            )
        }

        Spacer(modifier = Modifier.height(if (inMore) 8.dp else 4.dp))

        Text(
            item.title,
            textAlign = TextAlign.Center,
            minLines = minLines,
            maxLines = maxLines,
            fontSize = if (inMore) 14.sp else 12.sp,
            fontWeight = if (inMore) FontWeight.Normal else if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = when {
                inMore -> primary500
                item.hideIcon -> if (isSelected) primary100 else primary500
                isSelected -> primary100
                else -> primary500
            },
        )
    }
}
