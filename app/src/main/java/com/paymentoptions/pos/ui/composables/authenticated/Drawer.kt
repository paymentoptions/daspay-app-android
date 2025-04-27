import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Support
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.paymentoptions.pos.R
import kotlinx.coroutines.launch

data class NavItem(
    val label: String,
    val title: String,
    val icon: ImageVector,
    val route: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Drawer(navController1: NavController): Unit {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    val navItems = listOf(
        NavItem("Sale", "Make Payment", Icons.Default.PointOfSale, "saleScreen"),
        NavItem("Settlement", "Settlement", Icons.Default.LockClock, "settlementScreen"),
        NavItem("History", "History", Icons.Default.History, "historyScreen"),
        NavItem("Pre-Auth", "Pre-Auth", Icons.Default.AutoFixHigh, "preAuthScreen"),
        NavItem("Refund", "Refund", Icons.Default.Money, "refundScreen"),
        NavItem("Settings", "Settings", Icons.Default.Settings, "settingsScreen"),
        NavItem("Help & Support", "Help & Support", Icons.Default.Support, "helpAndSupportScreen")
    )

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentTitle = navItems.find { it.route == currentRoute }?.title ?: navItems[0].title


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {

                Image(
                    painter = painterResource(id = R.drawable.logo), // Replace with your logo
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(150.dp)
                        .padding(all = 10.dp)
                )

                navItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(item.route)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {


        Text("Drawer Screen")
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentTitle) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },

            ) {
            NavHost(
                navController = navController,
                startDestination = "saleScreen",
                modifier = Modifier.padding(it)
            ) {
                composable("helpAndSupportScreen") { HelpAndSupportScreen(navController) }
                composable("historyScreen") { HistoryScreen(navController) }
                composable("preAuthScreen") { PreAuthScreen(navController) }
                composable("refundScreen") { RefundScreen(navController) }
                composable("saleScreen") { SaleScreen(navController) }
                composable("settingsScreen") { SettingsScreen(navController) }
                composable("settlementScreen") { SettlementScreen(navController) }
            }
        }
    }
}


