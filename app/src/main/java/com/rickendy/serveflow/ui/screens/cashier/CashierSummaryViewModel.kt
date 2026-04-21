package com.rickendy.serveflow.ui.screens.cashier

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rickendy.serveflow.data.model.PaymentSummary
import com.rickendy.serveflow.data.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class CashierSummaryUiState(
    val isLoading: Boolean = false,
    val summary: PaymentSummary? = null,
    val selectedDate: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    val error: String? = null
)

class CashierSummaryViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val paymentRepository = PaymentRepository(context)

    private val _uiState = MutableStateFlow(CashierSummaryUiState())
    val uiState: StateFlow<CashierSummaryUiState> = _uiState

    init {
        loadSummary()
    }

    fun loadSummary(date: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            paymentRepository.getPaymentSummary(date).fold(
                onSuccess = { summary ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        summary = summary,
                        selectedDate = date ?: _uiState.value.selectedDate
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
}