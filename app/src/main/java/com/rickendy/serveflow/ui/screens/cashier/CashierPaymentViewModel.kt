package com.rickendy.serveflow.ui.screens.cashier

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rickendy.serveflow.data.model.Order
import com.rickendy.serveflow.data.model.OrderItemRequest
import com.rickendy.serveflow.data.model.Payment
import com.rickendy.serveflow.data.repository.OrderRepository
import com.rickendy.serveflow.data.repository.PaymentRepository
import com.rickendy.serveflow.util.calculateTotal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CashierPaymentUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val order: Order? = null,
    val amountPaid: String = "",
    val paymentMethod: String = "cash",
    val payment: Payment? = null,
    val error: String? = null
)

class CashierPaymentViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val orderRepository = OrderRepository(context)
    private val paymentRepository = PaymentRepository(context)

    private val _uiState = MutableStateFlow(CashierPaymentUiState())
    val uiState: StateFlow<CashierPaymentUiState> = _uiState

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

    fun setAmountPaid(amount: String) {
        _uiState.value = _uiState.value.copy(amountPaid = amount)
    }

    fun setPaymentMethod(method: String) {
        _uiState.value = _uiState.value.copy(paymentMethod = method)
    }

    fun addItems(items: List<OrderItemRequest>, onSuccess: () -> Unit) {
        val orderId = _uiState.value.order?.id ?: return
        viewModelScope.launch {
            orderRepository.addOrderItems(orderId, items).fold(
                onSuccess = { order ->
                    _uiState.value = _uiState.value.copy(order = order)
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            )
        }
    }

    fun removeItem(itemId: Int) {
        val orderId = _uiState.value.order?.id ?: return
        viewModelScope.launch {
            orderRepository.removeOrderItem(orderId, itemId).fold(
                onSuccess = {
                    loadOrder(orderId)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            )
        }
    }

    val totalAmount: Int
        get() = calculateTotal(_uiState.value.order?.items)

    val changeAmount: Int
        get() {
            val paid = _uiState.value.amountPaid.toIntOrNull() ?: 0
            return maxOf(0, paid - totalAmount)
        }

    val isPaymentValid: Boolean
        get() {
            val paid = _uiState.value.amountPaid.toIntOrNull() ?: 0
            return paid >= totalAmount && totalAmount > 0
        }

    fun processPayment(onSuccess: () -> Unit) {
        val order = _uiState.value.order ?: return
        val amountPaid = _uiState.value.amountPaid
        val paymentMethod = _uiState.value.paymentMethod

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            paymentRepository.createPayment(
                orderId = order.id,
                amountPaid = amountPaid,
                paymentMethod = paymentMethod
            ).fold(
                onSuccess = { payment ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        payment = payment
                    )
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
}