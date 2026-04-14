package com.rickendy.serveflow.data.repository

import android.content.Context
import com.rickendy.serveflow.data.model.CreatePaymentRequest
import com.rickendy.serveflow.data.model.Order
import com.rickendy.serveflow.data.model.Payment
import com.rickendy.serveflow.data.model.PaymentSummary
import com.rickendy.serveflow.data.remote.api.NetworkClient

class PaymentRepository(private val context: Context) {

    private val api = NetworkClient.authenticatedApi(context)

    suspend fun getPendingPayments(): Result<List<Order>> {
        return try {
            val response = api.getPendingPayments()
            if (response.isSuccessful) {
                Result.success(response.body()!!.orders)
            } else {
                Result.failure(Exception("Failed to fetch pending payments"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPayment(
        orderId: Int,
        amountPaid: String,
        paymentMethod: String
    ): Result<Payment> {
        return try {
            val response = api.createPayment(
                CreatePaymentRequest(orderId, amountPaid, paymentMethod)
            )
            if (response.isSuccessful) {
                Result.success(response.body()!!.payment)
            } else {
                Result.failure(Exception("Failed to create payment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPayment(id: Int): Result<Payment> {
        return try {
            val response = api.getPayment(id)
            if (response.isSuccessful) {
                Result.success(response.body()!!.payment)
            } else {
                Result.failure(Exception("Failed to fetch payment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPaymentSummary(date: String? = null): Result<PaymentSummary> {
        return try {
            val response = api.getPaymentSummary(date)
            if (response.isSuccessful) {
                Result.success(response.body()!!.summary)
            } else {
                Result.failure(Exception("Failed to fetch summary"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}