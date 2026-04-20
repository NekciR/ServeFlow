package com.rickendy.serveflow.ui.screens.waiter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.rickendy.serveflow.data.model.Order
import com.rickendy.serveflow.data.model.OrderItem
import com.rickendy.serveflow.data.model.Table
import com.rickendy.serveflow.data.remote.socket.SocketClient
import com.rickendy.serveflow.data.repository.OrderRepository
import com.rickendy.serveflow.data.repository.TableRepository
import com.rickendy.serveflow.util.calculateTotal
import com.rickendy.serveflow.util.parseOrder
import com.rickendy.serveflow.util.parseOrderItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


data class WaiterTableDetailUiState(
    val isLoading: Boolean = false,
    val table: Table? = null,
    val order: Order? = null,
    val totalPrice: Int = 0,
    val error: String? = null
)
class WaiterTableDetailViewModel(application: Application, private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val tableRepository = TableRepository(context)

    private val _uiState = MutableStateFlow(WaiterTableDetailUiState())
    val uiState: StateFlow<WaiterTableDetailUiState> = _uiState

    private var isSocketStarted = false

    fun startSocket(tableId: Int) {
        if (isSocketStarted) return
        isSocketStarted = true

        SocketClient.connect()
        SocketClient.subscribeTable(tableId)

        listenSocketEvents()
    }



    private fun listenSocketEvents() {

//        SocketClient.onOrderNew { json ->
//            val order = parseOrder(json)
//
//            viewModelScope.launch {
//                _uiState.value = _uiState.value.copy(
//                    order = order,
//                    table = _uiState.value.table?.copy(status = "occupied"),
//                    totalPrice = calculateTotal(order.items)
//                )
//            }
//        }

        SocketClient.onOrderItemAdded { json ->
            val newItem = parseOrderItem(json)

            viewModelScope.launch {
                val currentOrder = _uiState.value.order ?: return@launch
                val currentItems = currentOrder.items?.toMutableList() ?: mutableListOf()

                val index = currentItems.indexOfFirst {
                    it.menuItemId == newItem.menuItemId &&
                            it.notes == newItem.notes
                }

                if (index != -1) {
                    val existing = currentItems[index]
                    currentItems[index] = existing.copy(
                        quantity = existing.quantity + newItem.quantity
                    )
                } else {
                    currentItems.add(newItem)
                }

                val updatedOrder = currentOrder.copy(items = currentItems)

                _uiState.value = _uiState.value.copy(
                    order = updatedOrder,
                    totalPrice = calculateTotal(currentItems)
                )
            }
        }

        SocketClient.onOrderUpdated { json ->
            val order = parseOrder(json)

            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(
                    order = order,
                    totalPrice = calculateTotal(order.items)
                )
            }
        }

        SocketClient.onPaymentCompleted {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(
                    order = null,
                    totalPrice = 0
                )
            }
        }
    }

    fun loadTableDetail(tableId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val tableResult = tableRepository.getTable(tableId)

            tableResult.fold(
                onSuccess = { table ->

                    if(table.activeOrder != null){
                        val activeOrder = table.activeOrder
                        val items = activeOrder.items ?: emptyList()

                        _uiState.value = WaiterTableDetailUiState(
                            isLoading = false,
                            table = table.table,
                            order = activeOrder,
                            totalPrice = calculateTotal(items)
                        )

                    }else{
                        _uiState.value = WaiterTableDetailUiState(
                            isLoading = false,
                            table = table.table
                        )
                    }

                },
                onFailure = {
                    _uiState.value = WaiterTableDetailUiState(
                        isLoading = false,
                        error = it.message
                    )
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()

        SocketClient.off("order:new")
        SocketClient.off("order:updated")
        SocketClient.off("order:item_added")
        SocketClient.off("payment:completed")
    }
}