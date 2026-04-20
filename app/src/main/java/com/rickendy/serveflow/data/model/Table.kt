package com.rickendy.serveflow.data.model

import com.google.gson.annotations.SerializedName

data class Table(
    val id: Int,
    @SerializedName("branch_id") val branchId: Int,
    val label: String,
    val status: String,
    @SerializedName("deleted_at") val deletedAt: String?,
    @SerializedName("waiter_id") val waiterId: Int?
)