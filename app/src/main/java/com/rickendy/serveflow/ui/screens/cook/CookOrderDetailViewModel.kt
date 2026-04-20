package com.rickendy.serveflow.ui.screens.cook

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rickendy.serveflow.data.model.Order
import com.rickendy.serveflow.data.remote.socket.SocketClient
import com.rickendy.serveflow.data.repository.OrderRepository
import com.rickendy.serveflow.util.parseOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CookOrderDetailUiState(
    val isLoading: Boolean = false,
    val order: Order? = null,
    val error: String? = null
)

class CookOrderDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val orderRepository = OrderRepository(context)

    private val _uiState = MutableStateFlow(CookOrderDetailUiState())
    val uiState: StateFlow<CookOrderDetailUiState> = _uiState

    fun loadOrder(orderId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            orderRepository.getOrder(orderId).fold(
                onSuccess = { order ->
                    _uiState.value = _uiState.value.copy(isLoading = false, order = order)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
                }
            )
        }
    }

    fun updateOrderStatus(orderId: Int, status: String) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, status).fold(
                onSuccess = { order ->
                    _uiState.value = _uiState.value.copy(order = order)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            )
        }
    }

    fun updateItemStatus(orderId: Int, itemId: Int, status: String) {
        viewModelScope.launch {
            orderRepository.updateOrderItem(
                orderId = orderId,
                itemId = itemId,
                status = status
            ).fold(
                onSuccess = { updatedItem ->
                    val current = _uiState.value.order ?: return@fold
                    val updatedItems = current.items?.map {
                        if (it.id == itemId) {
                            it.copy(status = updatedItem.status)
                        } else it
                    }
                    _uiState.value = _uiState.value.copy(
                        order = current.copy(items = updatedItems)
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            )
        }
    }

    fun startSocket(orderId: Int) {
        SocketClient.connect()

        SocketClient.onOrderUpdated { json ->
            val order = parseOrder(json)
            if (order.id == orderId) {
                _uiState.value = _uiState.value.copy(order = order)
            }
        }

        SocketClient.onOrderItemAdded { json ->
            val newOrder = parseOrder(json)

            if (newOrder.id != orderId) return@onOrderItemAdded

            viewModelScope.launch {
                val current = _uiState.value.order ?: return@launch

                val currentItems = current.items?.toMutableList() ?: mutableListOf()

                val newItems = newOrder.items ?: emptyList()

                for (newItem in newItems) {
                    val index = currentItems.indexOfFirst {
                        it.id == newItem.id
                    }

                    if (index != -1) {
                        currentItems[index] = currentItems[index].copy(
                            quantity = newItem.quantity,
                            status = newItem.status
                        )
                    } else {
                        currentItems.add(newItem)
                    }
                }

                _uiState.value = _uiState.value.copy(
                    order = current.copy(items = currentItems)
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        SocketClient.off("order:updated")
        SocketClient.off("order:item_added")
    }
}