package com.rickendy.serveflow.ui.screens.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickendy.serveflow.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

class LoginViewModel(private val context: Context) : ViewModel() {

    private val repository = AuthRepository(context)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(username: String, password: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            val result = repository.login(username, password)
            result.fold(
                onSuccess = { response ->
                    _uiState.value = LoginUiState(isLoading = false)
                    onSuccess(response.user.role)
                },
                onFailure = { error ->
                    _uiState.value = LoginUiState(
                        isLoading = false,
                        error = error.message ?: "Login failed"
                    )
                }
            )
        }
    }
}