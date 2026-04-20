package com.rickendy.serveflow.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.rickendy.serveflow.ui.screens.waiter.WaiterOrderScreen
import com.rickendy.serveflow.ui.screens.waiter.WaiterTableDetailScreen
import com.rickendy.serveflow.ui.screens.waiter.WaiterTablesScreen
import com.rickendy.serveflow.util.sessionFlow

@Composable
fun AppNavHost(navController: NavHostController) {
    val context = LocalContext.current
    val session by context.sessionFlow().collectAsState(initial = null)

    val startDestination = remember(session?.accessToken) {
        when (session?.role) {
            "waiter" -> Screen.WaiterTables.route
            "cook" -> Screen.CookKitchen.route
            "cashier" -> Screen.CashierPending.route
            else -> Screen.Login.route
        }
    }

    if (session == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        return
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
                onNewOrder = { navController.navigate(Screen.WaiterOrder.createRoute(tableId)) },
                onAddItems = { orderId -> navController.navigate(Screen.WaiterOrder.createRoute(tableId, orderId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.WaiterOrder.route,
            arguments = listOf(
                navArgument("tableId") { type = NavType.IntType },
                navArgument("orderId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStack ->
            val tableId = backStack.arguments?.getInt("tableId") ?: return@composable
            val orderId = backStack.arguments?.getString("orderId")?.toIntOrNull()
            WaiterOrderScreen(
                tableId = tableId,
                orderId = orderId,
                onSuccess = {
                    navController.navigate(Screen.WaiterTables.route) {
                        popUpTo(Screen.WaiterTables.route) { inclusive = false }
                    }
                },
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