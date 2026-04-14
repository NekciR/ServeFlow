package com.rickendy.serveflow.data.model

import com.google.gson.annotations.SerializedName

data class Payment(
    val id: Int,
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("cashier_id") val cashierId: Int,
    @SerializedName("cashier_name") val cashierName: String?,
    @SerializedName("table_label") val tableLabel: String?,
    @SerializedName("total_amount") val totalAmount: String,
    @SerializedName("amount_paid") val amountPaid: String,
    @SerializedName("change_given") val changeGiven: String,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("paid_at") val paidAt: String,
    val items: List<PaymentItem>?
)

data class PaymentItem(
    val name: String,
    val price: String,
    val quantity: Int,
    val subtotal: String
)

data class PaymentSummary(
    val date: String,
    @SerializedName("total_transactions") val totalTransactions: Int,
    @SerializedName("total_revenue") val totalRevenue: String,
    @SerializedName("cash_revenue") val cashRevenue: String,
    @SerializedName("card_revenue") val cardRevenue: String,
    @SerializedName("qris_revenue") val qrisRevenue: String
)