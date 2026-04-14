package com.rickendy.serveflow.data.model

data class User(
    val id: Int,
    val username: String,
    val name: String,
    val role: String,
    val gender: String?
)