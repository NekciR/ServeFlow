package com.rickendy.serveflow.util

import com.google.gson.Gson
import com.rickendy.serveflow.data.model.Order
import com.rickendy.serveflow.data.model.OrderItem
import org.json.JSONObject


fun parseOrder(json: JSONObject): Order {
    val gson = Gson()
    return gson.fromJson(json.toString(), Order::class.java)
}

fun parseOrderItem(json: JSONObject): OrderItem {
    val gson = Gson()
    return gson.fromJson(json.toString(), OrderItem::class.java)
}