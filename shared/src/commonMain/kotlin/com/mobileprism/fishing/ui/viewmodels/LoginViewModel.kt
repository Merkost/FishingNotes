package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.kimplify.cedar.logging.Cedar

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Signing : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String?) : LoginUiState
}

class LoginViewModel(
    private val repository: UserRepository,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    init {
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            repository.currentUser
                .catch { error -> onSignInError(error) }
                .collectLatest { user ->
                    if (user != null) persistUser(user)
                }
        }
    }

    private fun persistUser(user: User) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Signing
            repository.addNewUser(user).fold(
                onSuccess = { _uiState.value = LoginUiState.Success },
                onFailure = { onSignInError(it) },
            )
        }
    }

    fun firebaseSignInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Signing
            try {
                val credential = GoogleAuthProvider.credential(idToken, null)
                Firebase.auth.signInWithCredential(credential)
            } catch (e: Exception) {
                onSignInError(e)
            }
        }
    }

    fun onGoogleSignInCancelled() {
        _uiState.value = LoginUiState.Idle
    }

    private fun onSignInError(error: Throwable) {
        _uiState.value = LoginUiState.Error(error.message)
        runCatching { analyticsTracker.logEvent(AnalyticsEvent.SignInError(error.message)) }
        runCatching { Cedar.e(error.message ?: "Sign-in failed") }
    }
}
