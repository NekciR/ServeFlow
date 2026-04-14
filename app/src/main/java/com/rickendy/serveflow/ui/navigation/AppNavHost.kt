package com.rickendy.serveflow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rickendy.serveflow.ui.screens.auth.LoginScreen
import com.rickendy.serveflow.ui.screens.cashier.CashierPaymentScreen
import com.rickendy.serveflow.ui.screens.cashier.CashierPendingScreen
import com.rickendy.serveflow.ui.screens.cashier.CashierSummaryScreen
import com.rickendy.serveflow.ui.screens.cook.CookKitchenScreen
import com.rickendy.serveflow.ui.screens.cook.CookOrderDetailScreen
import com.rickendy.serveflow.ui.screens.waiter.WaiterAddItemsScreen
import com.rickendy.serveflow.ui.screens.waiter.WaiterNewOrderScreen
import com.rickendy.serveflow.ui.screens.waiter.WaiterTableDetailScreen
import com.rickendy.serveflow.ui.screens.waiter.WaiterTablesScreen
import com.rickendy.serveflow.util.sessionFlow

@Composable
fun AppNavHost(navController: NavHostController) {
    val context = LocalContext.current
    val session by context.sessionFlow().collectAsState(initial = null)

    val startDestination = when (session?.role) {
        "waiter" -> Screen.WaiterTables.route
        "cook" -> Screen.CookKitchen.route
        "cashier" -> Screen.CashierPending.route
        else -> Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    val destination = when (role) {
                        "waiter" -> Screen.WaiterTables.route
                        "cook" -> Screen.CookKitchen.route
                        "cashier" -> Screen.CashierPending.route
                        else -> Screen.Login.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.WaiterTables.route) {
            WaiterTablesScreen(
                onTableClick = { tableId ->
                    navController.navigate(Screen.WaiterTableDetail.createRoute(tableId))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.WaiterTableDetail.route,
            arguments = listOf(navArgument("tableId") { type = NavType.IntType })
        ) { backStack ->
            val tableId = backStack.arguments?.getInt("tableId") ?: return@composable
            WaiterTableDetailScreen(
                tableId = tableId,
                onNewOrder = { navController.navigate(Screen.WaiterNewOrder.createRoute(tableId)) },
                onAddItems = { orderId -> navController.navigate(Screen.WaiterAddItems.createRoute(orderId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.WaiterNewOrder.route,
            arguments = listOf(navArgument("tableId") { type = NavType.IntType })
        ) { backStack ->
            val tableId = backStack.arguments?.getInt("tableId") ?: return@composable
            WaiterNewOrderScreen(
                tableId = tableId,
                onOrderCreated = {
                    navController.navigate(Screen.WaiterTables.route) {
                        popUpTo(Screen.WaiterTables.route) { inclusive = false }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.WaiterAddItems.route,
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) { backStack ->
            val orderId = backStack.arguments?.getInt("orderId") ?: return@composable
            WaiterAddItemsScreen(
                orderId = orderId,
                onItemsAdded = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CookKitchen.route) {
            CookKitchenScreen(
                onOrderClick = { orderId ->
                    navController.navigate(Screen.CookOrderDetail.createRoute(orderId))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.CookOrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) { backStack ->
            val orderId = backStack.arguments?.getInt("orderId") ?: return@composable
            CookOrderDetailScreen(
                orderId = orderId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CashierPending.route) {
            CashierPendingScreen(
                onOrderClick = { orderId ->
                    navController.navigate(Screen.CashierPayment.createRoute(orderId))
                },
                onSummary = { navController.navigate(Screen.CashierSummary.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.CashierPayment.route,
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) { backStack ->
            val orderId = backStack.arguments?.getInt("orderId") ?: return@composable
            CashierPaymentScreen(
                orderId = orderId,
                onPaymentComplete = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CashierSummary.route) {
            CashierSummaryScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}