package com.rickendy.serveflow.ui.screens.waiter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.rickendy.serveflow.data.model.MenuItem
import com.rickendy.serveflow.data.model.OrderItemRequest
import com.rickendy.serveflow.data.repository.MenuRepository
import com.rickendy.serveflow.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int,
    val notes: String? = null
)

data class WaiterOrderUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val menu: Map<String, List<MenuItem>> = emptyMap(),
    val selectedCategory: String = "All",
    val cart: Map<Int, CartItem> = emptyMap(),
    val error: String? = null
)

class WaiterOrderViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val menuRepository = MenuRepository(context)
    private val orderRepository = OrderRepository(context)

    private val _uiState = MutableStateFlow(WaiterOrderUiState())
    val uiState: StateFlow<WaiterOrderUiState> = _uiState

    fun loadMenu() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            menuRepository.getMenu().fold(
                onSuccess = { menu ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        menu = menu
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

    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun addToCart(item: MenuItem) {
        val cart = _uiState.value.cart.toMutableMap()
        val existing = cart[item.id]
        cart[item.id] =
            existing?.copy(quantity = existing.quantity + 1) ?: CartItem(menuItem = item, quantity = 1)
        _uiState.value = _uiState.value.copy(cart = cart)
    }

    fun removeFromCart(item: MenuItem) {
        val cart = _uiState.value.cart.toMutableMap()
        val existing = cart[item.id] ?: return
        if (existing.quantity <= 1) {
            cart.remove(item.id)
        } else {
            cart[item.id] = existing.copy(quantity = existing.quantity - 1)
        }
        _uiState.value = _uiState.value.copy(cart = cart)
    }

    fun setNote(menuItemId: Int, note: String) {
        val cart = _uiState.value.cart.toMutableMap()
        val existing = cart[menuItemId] ?: return
        cart[menuItemId] = existing.copy(notes = note.ifBlank { null })
        _uiState.value = _uiState.value.copy(cart = cart)
    }

    fun submitOrder(tableId: Int, orderId: Int?, onSuccess: () -> Unit) {
        val items = _uiState.value.cart.values.map {
            OrderItemRequest(
                menuItemId = it.menuItem.id,
                quantity = it.quantity,
                notes = it.notes
            )
        }

        if (items.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)

            val result = if (orderId == null) {
                orderRepository.createOrder(tableId, items)
            } else {
                orderRepository.addOrderItems(orderId, items)
            }

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = error.message
                    )
                }
            )
        }
    }

    val categories: List<String>
        get() = listOf("All") + _uiState.value.menu.keys.sorted()

    val filteredMenu: List<MenuItem>
        get() {
            val state = _uiState.value
            return if (state.selectedCategory == "All") {
                state.menu.values.flatten()
            } else {
                state.menu[state.selectedCategory] ?: emptyList()
            }
        }

    val totalPrice: Int
        get() = _uiState.value.cart.values.sumOf {
            it.quantity * (it.menuItem.price.toDoubleOrNull()?.toInt() ?: 0)
        }



    val totalItems: Int
        get() = _uiState.value.cart.values.sumOf { it.quantity }
}