package com.rickendy.serveflow.ui.screens.cashier

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rickendy.serveflow.data.model.Order
import com.rickendy.serveflow.data.remote.socket.SocketClient
import com.rickendy.serveflow.data.repository.AuthRepository
import com.rickendy.serveflow.data.repository.OrderRepository
import com.rickendy.serveflow.util.parseOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CashierPendingUiState(
    val isLoading: Boolean = false,
    val orders: List<Order> = emptyList(),
    val error: String? = null
)

class CashierPendingViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val orderRepository = OrderRepository(context)
    private val authRepository = AuthRepository(context)

    private val _uiState = MutableStateFlow(CashierPendingUiState())
    val uiState: StateFlow<CashierPendingUiState> = _uiState

    private var isSocketStarted = false
    val isConnected = SocketClient.connectionState

    init {
        loadActiveOrders()
    }

    fun loadActiveOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            orderRepository.getActiveOrders().fold(
                onSuccess = { orders ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        orders = orders
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

    fun startSocket() {
        if (isSocketStarted) return
        isSocketStarted = true

        SocketClient.connect()
        SocketClient.subscribeCashier()

        SocketClient.onOrderUpdated { json ->
            val order = parseOrder(json)
            viewModelScope.launch {
                val current = _uiState.value.orders.toMutableList()
                val index = current.indexOfFirst { it.id == order.id }
                when {
                    index != -1 && order.status == "paid" -> current.removeAt(index)
                    index != -1 -> current[index] = order
                    else -> if (order.status != "paid") current.add(0, order)
                }
                _uiState.value = _uiState.value.copy(orders = current)
            }
        }
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLogout()
        }
    }

    override fun onCleared() {
        super.onCleared()
        SocketClient.off("order:updated")
    }
}