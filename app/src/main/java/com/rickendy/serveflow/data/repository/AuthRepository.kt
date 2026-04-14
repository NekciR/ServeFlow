package com.rickendy.serveflow.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.rickendy.serveflow.data.model.LoginRequest
import com.rickendy.serveflow.data.model.LoginResponse
import com.rickendy.serveflow.data.remote.api.NetworkClient
import com.rickendy.serveflow.data.remote.api.TokenKeys
import com.rickendy.serveflow.data.remote.api.dataStore

class AuthRepository(private val context: Context) {

    private val api = NetworkClient.unauthenticatedApi

    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                context.dataStore.edit { prefs ->
                    prefs[TokenKeys.ACCESS_TOKEN] = body.accessToken
                    prefs[TokenKeys.REFRESH_TOKEN] = body.refreshToken
                    prefs[TokenKeys.USER_ROLE] = body.user.role
                    prefs[TokenKeys.USER_NAME] = body.user.name
                    prefs[TokenKeys.USER_ID] = body.user.id.toString()
                }
                Result.success(body)
            } else {
                Result.failure(Exception("Invalid credentials"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        try {
            NetworkClient.authenticatedApi(context).logout()
        } catch (e: Exception) {
            // idk, do nothing? ke hapus juga hehe
        } finally {
            context.dataStore.edit { prefs ->
                prefs.remove(TokenKeys.ACCESS_TOKEN)
                prefs.remove(TokenKeys.REFRESH_TOKEN)
                prefs.remove(TokenKeys.USER_ROLE)
                prefs.remove(TokenKeys.USER_NAME)
                prefs.remove(TokenKeys.USER_ID)
            }
        }
    }
}