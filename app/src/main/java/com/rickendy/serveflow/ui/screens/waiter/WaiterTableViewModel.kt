package com.rickendy.serveflow.ui.screens.waiter

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickendy.serveflow.data.model.Table
import com.rickendy.serveflow.data.repository.AuthRepository
import com.rickendy.serveflow.data.repository.TableRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class WaiterTablesUiState(
    val isLoading: Boolean = false,
    val tables: List<Table> = emptyList(),
    val error: String? = null
)

class WaiterTablesViewModel(private val context: Context) : ViewModel() {

    private val tableRepository = TableRepository(context)
    private val authRepository = AuthRepository(context)

    private val _uiState = MutableStateFlow(WaiterTablesUiState())
    val uiState: StateFlow<WaiterTablesUiState> = _uiState

    init {
        loadTables()
    }

    fun loadTables() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            tableRepository.getTables().fold(
                onSuccess = { tables ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        tables = tables
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLogout()
        }
    }
}