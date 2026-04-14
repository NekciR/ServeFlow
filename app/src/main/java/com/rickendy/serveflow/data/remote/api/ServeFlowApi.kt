package com.rickendy.serveflow.data.remote.api

import com.rickendy.serveflow.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ServeFlowApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: Map<String, String>): Response<RefreshResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("menu")
    suspend fun getMenu(): Response<MenuResponse>

    @GET("menu/{id}")
    suspend fun getMenuItem(@Path("id") id: Int): Response<Map<String, MenuItem>>

    @GET("tables")
    suspend fun getTables(): Response<TablesResponse>

    @GET("tables/{id}")
    suspend fun getTable(@Path("id") id: Int): Response<TableDetailResponse>

    @PUT("tables/{id}/status")
    suspend fun updateTableStatus(
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<Map<String, Table>>

    @POST("orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<OrderResponse>

    @GET("orders/active")
    suspend fun getActiveOrders(): Response<OrdersResponse>

    @GET("orders/{id}")
    suspend fun getOrder(@Path("id") id: Int): Response<OrderResponse>

    @PUT("orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: Int,
        @Body request: UpdateOrderStatusRequest
    ): Response<OrderResponse>

    @POST("orders/{id}/items")
    suspend fun addOrderItems(
        @Path("id") id: Int,
        @Body request: AddOrderItemsRequest
    ): Response<OrderResponse>

    @PUT("orders/{id}/items/{itemId}")
    suspend fun updateOrderItem(
        @Path("id") id: Int,
        @Path("itemId") itemId: Int,
        @Body request: UpdateOrderItemRequest
    ): Response<Map<String, OrderItem>>

    @DELETE("orders/{id}/items/{itemId}")
    suspend fun removeOrderItem(
        @Path("id") id: Int,
        @Path("itemId") itemId: Int
    ): Response<Map<String, String>>

    @GET("payments/pending")
    suspend fun getPendingPayments(): Response<OrdersResponse>

    @GET("payments/summary")
    suspend fun getPaymentSummary(@Query("date") date: String? = null): Response<PaymentSummaryResponse>

    @GET("payments/{id}")
    suspend fun getPayment(@Path("id") id: Int): Response<PaymentResponse>

    @POST("payments")
    suspend fun createPayment(@Body request: CreatePaymentRequest): Response<PaymentResponse>
}