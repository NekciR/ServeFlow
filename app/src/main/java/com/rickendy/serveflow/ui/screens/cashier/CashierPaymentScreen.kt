package com.rickendy.serveflow.ui.screens.cashier

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rickendy.serveflow.ui.components.SlidePaymentButton
import com.rickendy.serveflow.ui.screens.cook.InfoSection
import com.rickendy.serveflow.ui.theme.StatusReady
import com.rickendy.serveflow.util.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierPaymentScreen(
    orderId: Int,
    onPaymentComplete: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: CashierPaymentViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.order?.let {
                            if (it.tableId == null)
                                it.customerName ?: "Takeaway #${it.id}"
                            else
                                it.tableLabel ?: "Payment"
                        } ?: "Payment"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.error != null && uiState.order == null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.loadOrder(orderId) }) {
                            Text("Retry")
                        }
                    }
                }

                uiState.order != null -> {
                    PaymentContent(
                        uiState = uiState,
                        totalAmount = viewModel.totalAmount,
                        changeAmount = viewModel.changeAmount,
                        isPaymentValid = viewModel.isPaymentValid,
                        onAmountPaidChange = { viewModel.setAmountPaid(it) },
                        onPaymentMethodChange = { viewModel.setPaymentMethod(it) },
                        onRemoveItem = { viewModel.removeItem(it) },
                        onProcessPayment = { viewModel.processPayment(onPaymentComplete) }
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentContent(
    uiState: CashierPaymentUiState,
    totalAmount: Int,
    changeAmount: Int,
    isPaymentValid: Boolean,
    onAmountPaidChange: (String) -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onProcessPayment: () -> Unit
) {
    val order = uiState.order ?: return



    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .padding(16.dp)){
        val isLandscape = maxWidth > maxHeight

        val orderItemSection: @Composable () -> Unit = {
            OrderItemSection(
                uiState = uiState,
                totalAmount = totalAmount,
                onRemoveItem = onRemoveItem,
                isScrollable = isLandscape
                )
        }

        val paymentSection: @Composable () -> Unit = {
            Column {
                PaymentSection(
                    uiState = uiState,
                    changeAmount = changeAmount,
                    isPaymentValid = isPaymentValid,
                    onAmountPaidChange = onAmountPaidChange,
                    onPaymentMethodChange = onPaymentMethodChange,
                    onProcessPayment = onProcessPayment
                )
            }

        }


        if (isLandscape) {
            Row {
                Box(
                    Modifier
                        .weight(1f)
                        .padding(16.dp)) { orderItemSection() }
                Box(
                    Modifier
                        .weight(1.5f)
                        .padding(16.dp)) { paymentSection() }
            }
        } else {
            Column(Modifier.padding(16.dp)) {
                orderItemSection()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Payment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                paymentSection()
            }
        }
    }

//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//            .verticalScroll(rememberScrollState()),
//    ) {
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(16.dp),
//            colors = CardDefaults.cardColors(
//                containerColor = MaterialTheme.colorScheme.surface
//            ),
//            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
//        ) {
//            Column(modifier = Modifier.padding(16.dp)) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = "Order #${order.id}",
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.SemiBold
//                    )
//                    if (order.status == "ready") {
//                        Surface(
//                            shape = RoundedCornerShape(50.dp),
//                            color = StatusReady.copy(alpha = 0.12f),
//                            border = BorderStroke(1.dp, StatusReady)
//                        ) {
//                            Text(
//                                text = "Ready",
//                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
//                                style = MaterialTheme.typography.labelMedium,
//                                color = StatusReady,
//                                fontWeight = FontWeight.Medium
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(12.dp))
//                Divider()
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth(),
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    order.items?.forEach { item ->
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Column(modifier = Modifier.weight(1f)) {
//                                Text(
//                                    text = item.name ?: "",
//                                    style = MaterialTheme.typography.bodyMedium
//                                )
//                                if (!item.notes.isNullOrBlank()) {
//                                    Text(
//                                        text = item.notes,
//                                        style = MaterialTheme.typography.bodySmall,
//                                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                                    )
//                                }
//                            }
//                            Text(
//                                text = "x${item.quantity}",
//                                style = MaterialTheme.typography.bodyMedium,
//                                modifier = Modifier.padding(horizontal = 8.dp)
//                            )
//                            Text(
//                                text = "Rp ${formatRupiah(item.price)}",
//                                style = MaterialTheme.typography.bodyMedium,
//                                textAlign = TextAlign.End
//                            )
//                            IconButton(
//                                onClick = { onRemoveItem(item.id) },
//                                modifier = Modifier.size(32.dp)
//                            ) {
//                                Icon(
//                                    Icons.Default.Delete,
//                                    contentDescription = null,
//                                    tint = MaterialTheme.colorScheme.error,
//                                    modifier = Modifier.size(16.dp)
//                                )
//                            }
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(8.dp))
//                Divider()
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text(
//                        text = "Total",
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.SemiBold
//                    )
//                    Text(
//                        text = "Rp ${formatRupiah(totalAmount)}",
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Bold,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(16.dp),
//            colors = CardDefaults.cardColors(
//                containerColor = MaterialTheme.colorScheme.surface
//            ),
//            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
//        ) {
//            Column(modifier = Modifier.padding(16.dp)) {
//                Text(
//                    text = "Payment method",
//                    style = MaterialTheme.typography.labelMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                    listOf("cash", "card", "qris").forEach { method ->
//                        FilterChip(
//                            selected = uiState.paymentMethod == method,
//                            onClick = { onPaymentMethodChange(method) },
//                            label = { Text(method.uppercase()) },
//                            colors = FilterChipDefaults.filterChipColors(
//                                selectedContainerColor = MaterialTheme.colorScheme.primary,
//                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
//                            )
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                OutlinedTextField(
//                    value = uiState.amountPaid,
//                    onValueChange = onAmountPaidChange,
//                    label = { Text("Amount paid") },
//                    prefix = { Text("Rp ") },
//                    modifier = Modifier.fillMaxWidth(),
//                    keyboardOptions = KeyboardOptions(
//                        keyboardType = KeyboardType.Number
//                    ),
//                    singleLine = true
//                )
//
//                if (uiState.amountPaid.isNotBlank()) {
//                    Spacer(modifier = Modifier.height(12.dp))
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Text(
//                            text = "Change",
//                            style = MaterialTheme.typography.bodyLarge
//                        )
//                        Text(
//                            text = "Rp ${formatRupiah(changeAmount)}",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold,
//                            color = if (changeAmount >= 0)
//                                MaterialTheme.colorScheme.primary
//                            else
//                                MaterialTheme.colorScheme.error
//                        )
//                    }
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(
//            onClick = onProcessPayment,
//            enabled = isPaymentValid && !uiState.isSubmitting,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(52.dp),
//            shape = RoundedCornerShape(12.dp)
//        ) {
//            if (uiState.isSubmitting) {
//                CircularProgressIndicator(
//                    modifier = Modifier.size(20.dp),
//                    color = MaterialTheme.colorScheme.onPrimary,
//                    strokeWidth = 2.dp
//                )
//            } else {
//                Text(
//                    text = "Process Payment",
//                    style = MaterialTheme.typography.labelLarge
//                )
//            }
//        }
//    }
}

@Composable
fun OrderItemSection(
    uiState: CashierPaymentUiState,
    totalAmount: Int,
    onRemoveItem: (Int) -> Unit,
    isScrollable: Boolean
) {
    val order = uiState.order ?: return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (order.status == "ready") {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = StatusReady.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, StatusReady)
                    ) {
                        Text(
                            text = "Ready",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = StatusReady
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            val modifier = if (isScrollable) {
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            } else {
                Modifier.fillMaxWidth()
            }

            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                order.items?.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row (
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically

                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name ?: "")
                                if (!item.notes.isNullOrBlank()) {
                                    Text(
                                        item.notes,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Row (
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(" x${item.quantity} ")
                                Text("Rp ${formatRupiah(item.price)}")

                                IconButton(
                                    onClick = { onRemoveItem(item.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", fontWeight = FontWeight.SemiBold)
                Text(
                    "Rp ${formatRupiah(totalAmount)}",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PaymentSection(
    uiState: CashierPaymentUiState,
    changeAmount: Int,
    isPaymentValid: Boolean,
    onAmountPaidChange: (String) -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onProcessPayment: () -> Unit
){
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Payment method",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("cash", "card", "qris").forEach { method ->
                    FilterChip(
                        selected = uiState.paymentMethod == method,
                        onClick = { onPaymentMethodChange(method) },
                        label = { Text(method.uppercase()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.amountPaid,
                onValueChange = onAmountPaidChange,
                label = { Text("Amount paid") },
                prefix = { Text("Rp ") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true
            )

            if (uiState.amountPaid.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Change",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Rp ${formatRupiah(changeAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (changeAmount >= 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    SlidePaymentButton(
        isEnabled = isPaymentValid && !uiState.isSubmitting,
        isLoading = uiState.isSubmitting,
        onConfirmed = onProcessPayment
    )

//    Button(
//        onClick = onProcessPayment,
//        enabled = isPaymentValid && !uiState.isSubmitting,
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(52.dp),
//        shape = RoundedCornerShape(12.dp)
//    ) {
//        if (uiState.isSubmitting) {
//            CircularProgressIndicator(
//                modifier = Modifier.size(20.dp),
//                color = MaterialTheme.colorScheme.onPrimary,
//                strokeWidth = 2.dp
//            )
//        } else {
//            Text(
//                text = "Process Payment",
//                style = MaterialTheme.typography.labelLarge
//            )
//        }
//    }
}