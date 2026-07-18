package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.repository.LinkOutcome
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kimplify.cedar.logging.Cedar

sealed interface LinkState {
    data object Idle : LinkState
    data object Linking : LinkState
    data object MergeConfirm : LinkState
    data class Merging(val progress: Float?) : LinkState
    data class MergeSuccess(
        val catchesAdded: Int,
        val markersAdded: Int,
        val alreadyPresent: Int,
    ) : LinkState
    data object Success : LinkState
    data object Error : LinkState
}

class LinkAccountViewModel(
    private val repository: UserRepository,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LinkState>(LinkState.Idle)
    val uiState = _uiState.asStateFlow()

    private var pendingIdToken: String? = null

    fun linkWithGoogle(idToken: String) {
        pendingIdToken = idToken
        viewModelScope.launch {
            _uiState.value = LinkState.Linking
            repository.linkWithGoogle(idToken).fold(
                onSuccess = { outcome -> onLinkOutcome(outcome) },
                onFailure = { onLinkError(it) },
            )
        }
    }

    fun onSignInCancelled() {
        _uiState.value = LinkState.Idle
    }

    fun confirmMerge() {
        val token = pendingIdToken ?: return
        viewModelScope.launch {
            _uiState.value = LinkState.Merging(progress = null)
            repository.mergeGuestIntoGoogle(token).fold(
                onSuccess = { onLinkOutcome(it) },
                onFailure = { _uiState.value = LinkState.Error },
            )
        }
    }

    fun dismissMerge() {
        _uiState.value = LinkState.Idle
    }

    fun retry() {
        val token = pendingIdToken
        if (token != null) linkWithGoogle(token) else _uiState.value = LinkState.Idle
    }

    private fun onLinkOutcome(outcome: LinkOutcome) {
        _uiState.value = when (outcome) {
            is LinkOutcome.Linked -> LinkState.Success
            is LinkOutcome.Merged -> LinkState.MergeSuccess(
                outcome.catchesAdded, outcome.markersAdded, outcome.alreadyPresent,
            )
        }
    }

    private fun onLinkError(error: Throwable) {
        if (error is dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException) {
            _uiState.value = LinkState.MergeConfirm
            return
        }
        _uiState.value = LinkState.Error
        runCatching { analyticsTracker.logEvent(AnalyticsEvent.SignInError(error.message)) }
        runCatching { Cedar.e(error.message ?: "Link failed") }
    }
}
