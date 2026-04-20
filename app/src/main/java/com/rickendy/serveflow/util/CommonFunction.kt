package com.rickendy.serveflow.util

import com.rickendy.serveflow.data.model.OrderItem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import java.text.NumberFormat
import java.util.Locale

fun calculateTotal(items: List<OrderItem>?): Int {
    return items?.sumOf {
        it.quantity * (it.price?.toDoubleOrNull()?.toInt() ?: 0)
    } ?: 0
}