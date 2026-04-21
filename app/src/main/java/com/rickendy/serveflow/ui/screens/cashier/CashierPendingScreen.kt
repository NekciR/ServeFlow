package com.rickendy.serveflow.ui.screens.cashier

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rickendy.serveflow.data.model.Order
import com.rickendy.serveflow.ui.theme.StatusPending
import com.rickendy.serveflow.ui.theme.StatusReady
import com.rickendy.serveflow.util.calculateTotal
import com.rickendy.serveflow.util.formatDate
import com.rickendy.serveflow.util.formatRupiah
import com.rickendy.serveflow.util.sessionFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierPendingScreen(
    onOrderClick: (Int) -> Unit,
    onTakeaway: () -> Unit,
    onSummary: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: CashierPendingViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val session by context.sessionFlow().collectAsState(initial = null)
    val pullToRefreshState = rememberPullToRefreshState()
    val isConnected by viewModel.isConnected.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadActiveOrders()
        viewModel.startSocket()
    }

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.loadActiveOrders()
            pullToRefreshState.endRefresh()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = session?.userName ?: "-",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Cashier",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336),
                                shape = CircleShape
                            )
                    )
                    IconButton(onClick = { viewModel.loadActiveOrders() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onSummary) {
                        Icon(Icons.Default.BarChart, contentDescription = "Summary")
                    }
                    IconButton(onClick = { viewModel.logout(onLogout) }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onTakeaway,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Takeaway") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            when {
                uiState.isLoading && uiState.orders.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.error != null && uiState.orders.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.loadActiveOrders() }) {
                            Text("Retry")
                        }
                    }
                }

                uiState.orders.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.TableBar,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No active orders",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.orders, key = { it.id }) { order ->
                            CashierOrderCard(
                                order = order,
                                onClick = { onOrderClick(order.id) }
                            )
                        }
                    }
                }
            }

            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierOrderCard(
    order: Order,
    onClick: () -> Unit
) {
    val isTakeaway = order.tableId == null
    val statusColor = when (order.status) {
        "pending" -> StatusPending
        "ready" -> StatusReady
        else -> StatusPending
    }

    val animatedColor by animateColorAsState(
        targetValue = statusColor,
        label = "statusColor"
    )

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, animatedColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isTakeaway) Icons.Default.DeliveryDining
                        else Icons.Default.TableBar,
                        contentDescription = null,
                        tint = animatedColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isTakeaway)
                                order.customerName ?: "Takeaway #${order.id}"
                            else
                                order.tableLabel ?: "Table",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Order #${order.id} · ${formatDate(order.createdAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = animatedColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, animatedColor)
                    ) {
                        Text(
                            text = if (order.status == "ready") "Ready" else "Pending",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = animatedColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rp ${formatRupiah(calculateTotal(order.items))}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (!order.items.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = order.items.joinToString(", ") { "${it.name} x${it.quantity}" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}