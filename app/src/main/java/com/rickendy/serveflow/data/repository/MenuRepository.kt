package com.rickendy.serveflow.data.repository

import android.content.Context
import com.rickendy.serveflow.data.model.MenuItem
import com.rickendy.serveflow.data.remote.api.NetworkClient

class MenuRepository(private val context: Context) {

    private val api = NetworkClient.authenticatedApi(context)

    suspend fun getMenu(): Result<Map<String, List<MenuItem>>> {
        return try {
            val response = api.getMenu()
            if (response.isSuccessful) {
                Result.success(response.body()!!.menu)
            } else {
                Result.failure(Exception("Failed to fetch menu"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}