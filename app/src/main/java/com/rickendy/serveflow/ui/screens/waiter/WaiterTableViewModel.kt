package com.rickendy.serveflow.ui.screens.waiter

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickendy.serveflow.data.model.Table
import com.rickendy.serveflow.data.remote.socket.SocketClient
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

class WaiterTablesViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val tableRepository = TableRepository(context)
    private val authRepository = AuthRepository(context)

    private val _uiState = MutableStateFlow(WaiterTablesUiState())
    val uiState: StateFlow<WaiterTablesUiState> = _uiState

    private var isSocketStarted = false

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

    fun startSocket() {
        if (isSocketStarted) return
        isSocketStarted = true

        SocketClient.connect()
        SocketClient.subscribeTables()

        SocketClient.onTablesUpdate { json ->
            val tableId = json.getInt("id")
            val status = json.getString("status")

            viewModelScope.launch {
                val updatedTables = _uiState.value.tables.map { table ->
                    if (table.id == tableId) {
                        table.copy(status = status)
                    } else table
                }

                _uiState.value = _uiState.value.copy(tables = updatedTables, isLoading = false)
            }
        }
    }
    override fun onCleared() {
        super.onCleared()
        SocketClient.off("table:update")
    }
}