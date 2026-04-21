package com.rickendy.serveflow.ui.screens.cook

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rickendy.serveflow.data.model.Order
import com.rickendy.serveflow.data.model.OrderItem
import com.rickendy.serveflow.ui.theme.StatusInProgress
import com.rickendy.serveflow.ui.theme.StatusPending
import com.rickendy.serveflow.ui.theme.StatusReady
import com.rickendy.serveflow.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookOrderDetailScreen(
    orderId: Int,
    onBack: () -> Unit
) {
    val viewModel: CookOrderDetailViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    val isConnected by viewModel.isConnected.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
        viewModel.startSocket(orderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(uiState.order?.tableLabel ?: "Order Detail")
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336),
                                    shape = CircleShape
                                )
                        )

//                        Spacer(modifier = Modifier.width(6.dp))
//
//                        Text(
//                            text = if (isConnected) "Live" else "Offline",
//                            style = MaterialTheme.typography.labelMedium
//                        )
                    }

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
                uiState.isLoading && uiState.order == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.error != null -> {
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
                    CookOrderContent(
                        order = uiState.order!!,
                        onUpdateOrderStatus = { status ->
                            viewModel.updateOrderStatus(orderId, status)
                        },
                        onUpdateItemStatus = { itemId, status ->
                            viewModel.updateItemStatus(orderId, itemId, status)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CookOrderContent(
    order: Order,
    onUpdateOrderStatus: (String) -> Unit,
    onUpdateItemStatus: (Int, String) -> Unit
) {
    val allItemsDone = order.items?.all { it.status == "done" } == true

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .padding(16.dp)) {

        val isLandscape = maxWidth > maxHeight
        
        val infoSection: @Composable () -> Unit = {
            InfoSection(order = order)
        }
        
        val orderItemSection: @Composable () -> Unit = {
            OrderItemSection(
                order = order,
                onUpdateOrderStatus = onUpdateOrderStatus ,
                onUpdateItemStatus = onUpdateItemStatus,
                allItemsDone = allItemsDone
            )
        }
        


        if (isLandscape) {
            Row {
                Box(
                    Modifier
                        .weight(1f)
                        .padding(16.dp)) { infoSection() }
                Box(
                    Modifier
                        .weight(1.5f)
                        .padding(16.dp)) { orderItemSection() }
            }
        } else {
            Column(Modifier.padding(16.dp)) {
                infoSection()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                orderItemSection()
            }
        }

    }

//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
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
//                    Column {
//                        Text(
//                            text = "Order #${order.id}",
//                            style = MaterialTheme.typography.titleLarge,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                        Text(
//                            text = order.waiterName ?: "",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                        Text(
//                            text = formatDate(order.createdAt),
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//
//                    OrderStatusChip(status = order.status)
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Text(
//            text = "Items",
//            style = MaterialTheme.typography.titleMedium,
//            fontWeight = FontWeight.SemiBold,
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//
//        LazyColumn(
//            modifier = Modifier.weight(1f),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            items(order.items ?: emptyList(), key = {it.id}) { item ->
//                CookItemCard(
//                    item = item,
//                    onToggle = {
//                        val newStatus = if (item.status == "done") "cooking" else "done"
//                        onUpdateItemStatus(item.id, newStatus)
//                    }
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        when (order.status) {
//            "pending" -> {
//                Button(
//                    onClick = { onUpdateOrderStatus("in_progress") },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text("Start Cooking")
//                }
//            }
//            "in_progress" -> {
//                Button(
//                    onClick = { onUpdateOrderStatus("ready") },
//                    modifier = Modifier.fillMaxWidth(),
//                    enabled = allItemsDone
//                ) {
//                    Text(if (allItemsDone) "Siap dihidangkan" else "Tandai semua pesanan terlebih dahulu")
//                }
//            }
//            "ready" -> {
//                Surface(
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(12.dp),
//                    color = StatusReady.copy(alpha = 0.12f)
//                ) {
//                    Text(
//                        text = "Pesanan siap — menunggu waiter",
//                        modifier = Modifier.padding(16.dp),
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = StatusReady,
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//            }
//        }
//    }
}

@Composable
fun InfoSection(
    order: Order,
) {
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
                Column {
                    Text(
                        text = "Order #${order.id}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = order.waiterName ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDate(order.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OrderStatusChip(status = order.status)
            }
        }
    }
}

@Composable
fun OrderItemSection(
    order: Order,
    onUpdateOrderStatus: (String) -> Unit,
    onUpdateItemStatus: (Int, String) -> Unit,
    allItemsDone: Boolean,
){
    Column {


        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(order.items ?: emptyList(), key = {it.id}) { item ->
                CookItemCard(
                    item = item,
                    onToggle = {
                        val newStatus = if (item.status == "done") "cooking" else "done"
                        onUpdateItemStatus(item.id, newStatus)
                    }
                )
            }
        }

//        Spacer(modifier = Modifier.height(16.dp))

//        when (order.status) {
//            "pending" -> {
//                Button(
//                    onClick = { onUpdateOrderStatus("in_progress") },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text("Start Cooking")
//                }
//            }
//            "in_progress" -> {
//                Button(
//                    onClick = { onUpdateOrderStatus("ready") },
//                    modifier = Modifier.fillMaxWidth(),
//                    enabled = allItemsDone
//                ) {
//                    Text(if (allItemsDone) "Siap dihidangkan" else "Tandai semua pesanan terlebih dahulu")
//                }
//            }
//            "ready" -> {
//                Surface(
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(12.dp),
//                    color = StatusReady.copy(alpha = 0.12f)
//                ) {
//                    Text(
//                        text = "Pesanan siap — menunggu waiter",
//                        modifier = Modifier.padding(16.dp),
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = StatusReady,
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//            }
//        }
    }
    
}

@Composable
fun CookItemCard(
    item: OrderItem,
    onToggle: () -> Unit
) {
    val isDone = item.status == "done"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone)
                StatusReady.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            1.dp,
            if (isDone) StatusReady.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (!item.notes.isNullOrBlank()) {
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "x${item.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (isDone) Icons.Default.CheckCircle
                    else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isDone) StatusReady
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun OrderStatusChip(status: String) {
    val color = when (status) {
        "pending" -> StatusPending
        "in_progress" -> StatusInProgress
        "ready" -> StatusReady
        else -> StatusPending
    }

    val text = when (status) {
        "pending" -> "Pending"
        "in_progress" -> "In Progress"
        "ready" -> "Ready"
        else -> status
    }

    Surface(
        shape = RoundedCornerShape(50.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}