package com.rickendy.serveflow.util

import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(amount: String?): String {
    val number = amount?.toDoubleOrNull() ?: return "0"
    return NumberFormat.getNumberInstance(Locale("id", "ID")).format(number)
}

fun formatRupiah(amount: Int): String {
    return NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount)
}

fun formatRupiah(amount: Long): String {
    return NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount)
}

fun formatDate(dateString: String): String {
    return try {
        val input = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val output = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val date = input.parse(dateString)
        output.format(date!!)
    } catch (e: Exception) {
        dateString
    }
}