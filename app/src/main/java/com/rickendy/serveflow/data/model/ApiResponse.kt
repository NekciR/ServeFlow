package com.rickendy.serveflow.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    val user: User
)

data class RefreshResponse(
    @SerializedName("accessToken") val accessToken: String
)

data class MenuResponse(
    val menu: Map<String, List<MenuItem>>
)

data class TablesResponse(
    val tables: List<Table>
)

data class TableDetailResponse(
    val table: Table,
    val activeOrder: Order?
)

data class OrderResponse(
    val order: Order
)

data class OrdersResponse(
    val orders: List<Order>
)

data class PaymentResponse(
    val payment: Payment
)

data class PaymentSummaryResponse(
    val date: String,
    val summary: PaymentSummary
)