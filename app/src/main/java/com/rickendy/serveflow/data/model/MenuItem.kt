package com.rickendy.serveflow.data.model

import com.google.gson.annotations.SerializedName

data class MenuItem(
    val id: Int,
    @SerializedName("branch_id") val branchId: Int,
    val name: String,
    val category: String?,
    val price: String,
    val available: Boolean,
    @SerializedName("deleted_at") val deletedAt: String?
)