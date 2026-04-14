package com.rickendy.serveflow.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.rickendy.serveflow.data.remote.api.TokenKeys
import com.rickendy.serveflow.data.remote.api.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class SessionState(
    val accessToken: String?,
    val role: String?,
    val userName: String?,
    val userId: String?
)

fun Context.sessionFlow(): Flow<SessionState> {
    return dataStore.data.map { prefs ->
        SessionState(
            accessToken = prefs[TokenKeys.ACCESS_TOKEN],
            role = prefs[TokenKeys.USER_ROLE],
            userName = prefs[TokenKeys.USER_NAME],
            userId = prefs[TokenKeys.USER_ID]
        )
    }
}

suspend fun Context.clearSession() {
    dataStore.edit { prefs ->
        prefs.remove(TokenKeys.ACCESS_TOKEN)
        prefs.remove(TokenKeys.REFRESH_TOKEN)
        prefs.remove(TokenKeys.USER_ROLE)
        prefs.remove(TokenKeys.USER_NAME)
        prefs.remove(TokenKeys.USER_ID)
    }
}