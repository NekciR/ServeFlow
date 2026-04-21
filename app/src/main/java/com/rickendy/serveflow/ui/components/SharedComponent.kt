package com.rickendy.serveflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch

@Composable
fun CircleContainer(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    backgroundColor: Color = Color.Blue,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        contentAlignment = contentAlignment,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
    ) {
        content()
    }
}

@Composable
fun ConfirmationDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = confirmText,
                    color = if (isDestructive)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationBottomSheet(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDestructive: Boolean = false,
    isLoading: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDestructive)
                            MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(confirmText)
                    }
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(dismissText)
                }
            }
        }
    }
}

@Composable
fun SlidePaymentButton(
    isEnabled: Boolean,
    isLoading: Boolean,
    onConfirmed: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val trackWidth = 300.dp
    val thumbSize = 52.dp

    val trackWidthPx = with(density) { trackWidth.toPx() }
    val thumbSizePx = with(density) { thumbSize.toPx() }
    val maxOffset = trackWidthPx - thumbSizePx

    val offsetX = remember { Animatable(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isEnabled)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
    ) {


        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Slide to Process Payment",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isEnabled)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.toInt(), 0) }
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isEnabled)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
                .pointerInput(isEnabled, isLoading) {
                    if (!isEnabled || isLoading) return@pointerInput

                    detectDragGestures(
                        onDragEnd = {
                            if (offsetX.value > maxOffset * 0.8f) {
                                scope.launch {
                                    offsetX.animateTo(maxOffset)
                                    onConfirmed()
                                }
                            } else {
                                scope.launch {
                                    offsetX.animateTo(0f)
                                }
                            }
                        }
                    ) { change, dragAmount ->
                        change.consumeAllChanges()

                        val newOffset = (offsetX.value + dragAmount.x)
                            .coerceIn(0f, maxOffset)

                        scope.launch {
                            offsetX.snapTo(newOffset)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = if (isEnabled)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}