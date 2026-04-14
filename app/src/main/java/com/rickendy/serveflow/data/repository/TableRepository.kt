package com.rickendy.serveflow.data.repository

import android.content.Context
import com.rickendy.serveflow.data.model.Table
import com.rickendy.serveflow.data.model.TableDetailResponse
import com.rickendy.serveflow.data.remote.api.NetworkClient

class TableRepository(private val context: Context) {

    private val api = NetworkClient.authenticatedApi(context)

    suspend fun getTables(): Result<List<Table>> {
        return try {
            val response = api.getTables()
            if (response.isSuccessful) {
                Result.success(response.body()!!.tables)
            } else {
                Result.failure(Exception("Failed to fetch tables"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTable(id: Int): Result<TableDetailResponse> {
        return try {
            val response = api.getTable(id)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch table"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTableStatus(id: Int, status: String): Result<Table> {
        return try {
            val response = api.updateTableStatus(id, mapOf("status" to status))
            if (response.isSuccessful) {
                Result.success(response.body()!!["table"]!!)
            } else {
                Result.failure(Exception("Failed to update table status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}