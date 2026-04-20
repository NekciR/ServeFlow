package com.rickendy.serveflow.ui.components.dialog

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rickendy.serveflow.ui.components.ConfirmationBottomSheet

@Composable
fun GlobalDialogHost() {
    val dialogVM: DialogViewModel =
        viewModel(LocalContext.current as ComponentActivity)

    val state by dialogVM.dialog.collectAsState()

    ConfirmationBottomSheet(
        visible = state.visible,
        title = state.title,
        message = state.message,
        confirmText = state.confirmText,
        dismissText = state.dismissText,
        isDestructive = state.isDestructive,
        onConfirm = {
            state.onConfirm?.invoke()
            dialogVM.dismiss()
        },
        onDismiss = { dialogVM.dismiss() }
    )
}