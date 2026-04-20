package com.rickendy.serveflow.ui.components.dialog

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class DialogState(
    val visible: Boolean = false,
    val title: String = "",
    val message: String = "",
    val confirmText: String = "Confirm",
    val dismissText: String = "Cancel",
    val isDestructive: Boolean = false,
    val onConfirm: (() -> Unit)? = null
)

class DialogViewModel : ViewModel() {

    private val _dialog = MutableStateFlow(DialogState())
    val dialog: StateFlow<DialogState> = _dialog

    fun show(
        title: String,
        message: String,
        confirmText: String = "Confirm",
        dismissText: String = "Cancel",
        isDestructive: Boolean = false,
        onConfirm: () -> Unit
    ) {
        _dialog.value = DialogState(
            visible = true,
            title = title,
            message = message,
            confirmText = confirmText,
            dismissText = dismissText,
            isDestructive = isDestructive,
            onConfirm = onConfirm
        )
    }

    fun dismiss() {
        _dialog.value = DialogState()
    }
}