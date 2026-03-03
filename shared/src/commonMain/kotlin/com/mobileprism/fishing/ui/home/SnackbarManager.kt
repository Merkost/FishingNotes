package com.mobileprism.fishing.ui.home

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.StringResource
import kotlin.random.Random

data class Message(
    val id: Long,
    val messageId: StringResource,
    val snackbarAction: SnackbarAction?,
    val duration: SnackbarDuration = SnackbarDuration.Short
)

data class SnackbarAction(val textId: StringResource, val action: () -> Unit = {})

/**
 * Global snackbar message manager. ViewModels and utility classes call
 * [showMessage] directly; UI consumers collect [messages] to display them.
 */
object SnackbarManager {

    private val _messages: MutableStateFlow<List<Message>> = MutableStateFlow(emptyList())
    val messages: StateFlow<List<Message>> get() = _messages.asStateFlow()

    fun showMessage(
        messageTextId: StringResource,
        snackbarAction: SnackbarAction? = null,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        _messages.update { currentMessages ->
            currentMessages + Message(
                id = Random.nextLong(),
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
