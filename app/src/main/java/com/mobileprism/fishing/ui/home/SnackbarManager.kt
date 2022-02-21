package com.mobileprism.fishing.ui.home

import androidx.annotation.StringRes
import androidx.compose.material.SnackbarDuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*

data class Message(
    val id: Long,
    @StringRes val messageId: Int,
    val snackbarAction: SnackbarAction?,
    val duration: SnackbarDuration = SnackbarDuration.Short
)

data class SnackbarAction(@StringRes val textId: Int, val action: () -> Unit = {})

/**
 * Class responsible for managing Snackbar messages to show on the screen
 */
object SnackbarManager {

    private val _messages: MutableStateFlow<List<Message>> = MutableStateFlow(emptyList())
    val messages: StateFlow<List<Message>> get() = _messages.asStateFlow()

    fun showMessage(
        @StringRes messageTextId: Int,
        snackbarAction: SnackbarAction? = null,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        _messages.update { currentMessages ->
            currentMessages + Message(
                id = UUID.randomUUID().mostSignificantBits,
                messageId = messageTextId,
                snackbarAction = snackbarAction,
                duration = duration
            )
        }
    }

    fun setMessageShown(messageId: Long) {
        _messages.update { currentMessages ->
            currentMessages.filterNot { it.id == messageId }
        }
    }
}


