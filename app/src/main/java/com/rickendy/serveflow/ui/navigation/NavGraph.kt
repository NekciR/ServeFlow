package com.rickendy.serveflow.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")

    object WaiterTables : Screen("waiter/tables")
    object WaiterTableDetail : Screen("waiter/tables/{tableId}") {
        fun createRoute(tableId: Int) = "waiter/tables/$tableId"
    }
    object WaiterNewOrder : Screen("waiter/tables/{tableId}/new-order") {
        fun createRoute(tableId: Int) = "waiter/tables/$tableId/new-order"
    }
    object WaiterAddItems : Screen("waiter/orders/{orderId}/add-items") {
        fun createRoute(orderId: Int) = "waiter/orders/$orderId/add-items"
    }

    object CookKitchen : Screen("cook/kitchen")
    object CookOrderDetail : Screen("cook/orders/{orderId}") {
        fun createRoute(orderId: Int) = "cook/orders/$orderId"
    }

    object CashierPending : Screen("cashier/pending")
    object CashierPayment : Screen("cashier/payment/{orderId}") {
        fun createRoute(orderId: Int) = "cashier/payment/$orderId"
    }
    object CashierSummary : Screen("cashier/summary")
}