package com.rickendy.serveflow.data.model

import com.google.gson.annotations.SerializedName

data class Order(
    val id: Int,
    @SerializedName("table_id") val tableId: Int?,
    @SerializedName("waiter_id") val waiterId: Int,
    @SerializedName("customer_name") val customerName: String?,
    val status: String,
    @SerializedName("table_label") val tableLabel: String?,
    @SerializedName("waiter_name") val waiterName: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val items: List<OrderItem>?
)

data class OrderItem(
    val id: Int,
    @SerializedName("order_id") val orderId: Int?,
    @SerializedName("menu_item_id") val menuItemId: Int,
    val name: String?,
    val price: String?,
    val quantity: Int,
    val notes: String?,
    val status: String
)