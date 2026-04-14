package com.rickendy.serveflow.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val password: String,
    val role: String
)

data class CreateOrderRequest(
    @SerializedName("table_id") val tableId: Int,
    val items: List<OrderItemRequest>
)

data class OrderItemRequest(
    @SerializedName("menu_item_id") val menuItemId: Int,
    val quantity: Int,
    val notes: String? = null
)

data class AddOrderItemsRequest(
    val items: List<OrderItemRequest>
)

data class UpdateOrderStatusRequest(
    val status: String
)

data class UpdateOrderItemRequest(
    val quantity: Int? = null,
    val notes: String? = null,
    val status: String? = null
)

data class CreatePaymentRequest(
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("amount_paid") val amountPaid: String,
    @SerializedName("payment_method") val paymentMethod: String
)