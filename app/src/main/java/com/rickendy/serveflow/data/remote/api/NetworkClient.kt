package com.rickendy.serveflow.data.remote.api

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val Context.dataStore by preferencesDataStore(name = "serveflow_prefs")

object TokenKeys {
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val USER_ROLE = stringPreferencesKey("user_role")
    val USER_NAME = stringPreferencesKey("user_name")
    val USER_ID = stringPreferencesKey("user_id")
}

class AuthInterceptor(private val context: Context) : okhttp3.Interceptor {
    override fun intercept(chain: okhttp3.Interceptor.Chain): Response {
        val token = runBlocking {
            context.dataStore.data.first()[TokenKeys.ACCESS_TOKEN]
        }
        val request = chain.request().newBuilder().apply {
            token?.let { addHeader("Authorization", "Bearer $it") }
        }.build()
        return chain.proceed(request)
    }
}

class TokenAuthenticator(private val context: Context) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = runBlocking {
            context.dataStore.data.first()[TokenKeys.REFRESH_TOKEN]
        } ?: return null

        val refreshResponse = runBlocking {
            NetworkClient.unauthenticatedApi.refresh(mapOf("refreshToken" to refreshToken))
        }

        return if (refreshResponse.isSuccessful) {
            val newToken = refreshResponse.body()?.accessToken ?: return null
            runBlocking {
                context.dataStore.edit { prefs ->
                    prefs[TokenKeys.ACCESS_TOKEN] = newToken
                }
            }
            response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        } else {
            null
        }
    }
}

object NetworkClient {
    const val BASE_URL = "https://serveflow-api.rickendy.my.id/"

    val unauthenticatedApi: ServeFlowApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServeFlowApi::class.java)
    }

    fun authenticatedApi(context: Context): ServeFlowApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .authenticator(TokenAuthenticator(context))
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServeFlowApi::class.java)
    }
}