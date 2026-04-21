package com.rickendy.serveflow.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")

    object WaiterTables : Screen("waiter/tables")
    object WaiterTableDetail : Screen("waiter/tables/{tableId}") {
        fun createRoute(tableId: Int) = "waiter/tables/$tableId"
    }
    object WaiterOrder : Screen("waiter/tables/{tableId}/order?orderId={orderId}") {
        fun createRoute(tableId: Int, orderId: Int? = null) =
            "waiter/tables/$tableId/order?orderId=$orderId"
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

    object CashierTakeaway : Screen("cashier/takeaway")
}