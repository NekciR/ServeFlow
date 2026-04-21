package com.rickendy.serveflow.data.repository

import android.content.Context
import com.rickendy.serveflow.data.model.AddOrderItemsRequest
import com.rickendy.serveflow.data.model.CreateOrderRequest
import com.rickendy.serveflow.data.model.Order
import com.rickendy.serveflow.data.model.OrderItem
import com.rickendy.serveflow.data.model.OrderItemRequest
import com.rickendy.serveflow.data.model.UpdateOrderItemRequest
import com.rickendy.serveflow.data.model.UpdateOrderStatusRequest
import com.rickendy.serveflow.data.remote.api.NetworkClient

class OrderRepository(private val context: Context) {

    private val api = NetworkClient.authenticatedApi(context)

    suspend fun createOrder(
        tableId: Int?,
        items: List<OrderItemRequest>,
        customerName: String? = null
    ): Result<Order> {
        return try {
            val response = api.createOrder(
                CreateOrderRequest(tableId, items, customerName)
            )
            if (response.isSuccessful) {
                Result.success(response.body()!!.order)
            } else {
                Result.failure(Exception("Failed to create order"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActiveOrders(): Result<List<Order>> {
        return try {
            val response = api.getActiveOrders()
            if (response.isSuccessful) {
                Result.success(response.body()!!.orders)
            } else {
                Result.failure(Exception("Failed to fetch active orders"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrder(id: Int): Result<Order> {
        return try {
            val response = api.getOrder(id)
            if (response.isSuccessful) {
                Result.success(response.body()!!.order)
            } else {
                Result.failure(Exception("Failed to fetch order"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(id: Int, status: String): Result<Order> {
        return try {
            val response = api.updateOrderStatus(id, UpdateOrderStatusRequest(status))
            if (response.isSuccessful) {
                Result.success(response.body()!!.order)
            } else {
                Result.failure(Exception("Failed to update order status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addOrderItems(id: Int, items: List<OrderItemRequest>): Result<Order> {
        return try {
            val response = api.addOrderItems(id, AddOrderItemsRequest(items))
            if (response.isSuccessful) {
                Result.success(response.body()!!.order)
            } else {
                Result.failure(Exception("Failed to add items"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderItem(
        orderId: Int,
        itemId: Int,
        quantity: Int? = null,
        notes: String? = null,
        status: String? = null
    ): Result<OrderItem> {
        return try {
            val response = api.updateOrderItem(
                orderId, itemId,
                UpdateOrderItemRequest(quantity, notes, status)
            )
            if (response.isSuccessful) {
                Result.success(response.body()!!["item"]!!)
            } else {
                Result.failure(Exception("Failed to update item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeOrderItem(orderId: Int, itemId: Int): Result<Unit> {
        return try {
            val response = api.removeOrderItem(orderId, itemId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to remove item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}