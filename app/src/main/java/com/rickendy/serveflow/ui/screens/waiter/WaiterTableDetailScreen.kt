package com.rickendy.serveflow.ui.screens.waiter

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TableBar
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
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rickendy.serveflow.data.model.Order
import com.rickendy.serveflow.data.model.Table
import com.rickendy.serveflow.ui.components.CircleContainer
import com.rickendy.serveflow.ui.theme.StatusInProgress
import com.rickendy.serveflow.ui.theme.StatusPaid
import com.rickendy.serveflow.ui.theme.StatusPending
import com.rickendy.serveflow.ui.theme.StatusReady
import com.rickendy.serveflow.ui.theme.StatusServed
import com.rickendy.serveflow.util.formatDate
import com.rickendy.serveflow.util.formatRupiah
import com.rickendy.serveflow.util.sessionFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaiterTableDetailScreen(tableId : Int,onNewOrder: () -> Unit, onAddItems: (Int) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: WaiterTableDetailViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val session by context.sessionFlow().collectAsState(initial = null)

    BackHandler {
        onBack()
    }

    LaunchedEffect(tableId) {
        viewModel.loadTableDetail(tableId)
        viewModel.startSocket(tableId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.table?.label ?: "Table Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )

            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading && uiState.table == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
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
                        TextButton(onClick = { viewModel.loadTableDetail(tableId) }) {
                            Text("Retry")
                        }
                    }
                }

                uiState.table != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                    ) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            TableInfoCard(uiState.table!!)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        OrderBottomSheet(
                            order = uiState.order,
                            totalPrice = uiState.totalPrice,
                            onStartOrder = onNewOrder,
                            onAddItems = onAddItems,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TableInfoCard(table: Table) {
    val isOccupied = table.status.equals("occupied", ignoreCase = true)
    val containerColor = if (isOccupied)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.primaryContainer

    val statusColor = when (table.status) {
        "available" -> MaterialTheme.colorScheme.primary
        "occupied" -> MaterialTheme.colorScheme.error
//        "bill_requested" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
//        border = BorderStroke(1.dp, statusColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleContainer (
                backgroundColor = containerColor,
            ) {
                Icon(
                    imageVector = Icons.Default.TableBar,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = "Tempat",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Text(
                    text = table.label,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (isOccupied) "Occupied" else "Available",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }

            }
        }

    }
}

@Composable
fun OrderBottomSheet(
    order: Order?,
    totalPrice: Int,
    onStartOrder: () -> Unit,
    onAddItems: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp)
            ),
        shape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        tonalElevation = 12.dp,
//        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
    ) {

        if ((order?.items == null) || order.items.isEmpty()) {
            EmptyOrderContent(onStartOrder)
        } else {
            OrderContent(order, totalPrice, onAddItems)
        }
    }
}

@Composable
fun EmptyOrderContent(onStartOrder: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("No order yet")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onStartOrder) {
            Text("Start Order")
        }
    }
}

@Composable
fun OrderContent(
    order: Order,
    totalPrice : Int,
    onAddItems: (Int) -> Unit
) {
//    val containerColor = when (order.status)
//        MaterialTheme.colorScheme.errorContainer
//    else
//        MaterialTheme.colorScheme.primaryContainer
//
    val statusColor = when (order.status) {
        "pending" -> StatusPending
        "in_progress" -> StatusInProgress
        "ready" -> StatusReady
        "served" -> StatusServed
        "paid" -> StatusPaid
        else -> StatusPending
    }

    val statusText = when (order.status) {
        "pending" -> "Pending"
        "in_progress" -> "In Progress"
        "ready" -> "Ready"
        "served" -> "Served"
        "paid" -> "Paid"
        else -> "-"
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Order #${order.id}",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "${formatDate(order.createdAt)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                )
            }

            Surface(
                shape = RoundedCornerShape(50.dp),
                color = Color.Transparent,
                border = BorderStroke(width = 1.dp, color = statusColor)
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }



        }

        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(order.items!!) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "${item.name}",
                            style = MaterialTheme.typography.bodyLarge,
                        )

                        if(!item.notes.isNullOrBlank()){
                            Text(
                                text = "${item.notes}",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                    }


                    Text(
                        text = "x${item.quantity}",
                        modifier = Modifier.weight(0.5f)
                    )

                    Text(
                        text = "Rp ${formatRupiah(item.price)}",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

        }

        Divider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total")
            Text("Rp ${formatRupiah(totalPrice)}")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            OutlinedButton(
                onClick = {onAddItems(order.id)},
                modifier = Modifier.weight(1f)
            ) {
                Text("+ Add Items")
            }
        }
    }
}
