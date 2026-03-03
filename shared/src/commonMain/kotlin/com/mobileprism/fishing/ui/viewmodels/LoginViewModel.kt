package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.utils.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: UserRepository,
    private val analyticsTracker: AnalyticsTracker,
    private val logger: Logger,
) : ViewModel() {

    private val _uiState: MutableStateFlow<BaseViewState<User?>> =
        MutableStateFlow(BaseViewState.Success<User?>(null))
    val uiState = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            repository.currentUser
                .catch { error -> handleError(error) }
                .collectLatest { user -> user?.let { onSuccess(it) } }
        }
    }

    private fun onSuccess(user: User) {
        viewModelScope.launch {
            _uiState.value = BaseViewState.Loading(null)
            repository.addNewUser(user).fold(
                onSuccess = {
                    _uiState.value = BaseViewState.Success(user)
                },
                onFailure = {
                    _uiState.value = BaseViewState.Error(it)
                }
            )
        }
    }

    private fun handleError(error: Throwable) {
        _uiState.value = BaseViewState.Error(error)
    }

    fun firebaseSignInWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.credential(idToken, null)
                Firebase.auth.signInWithCredential(credential)
            } catch (e: Exception) {
                analyticsTracker.logEvent(AnalyticsEvent.SignInError(e.message))
                logger.log(e.message)
                _uiState.value = BaseViewState.Error(e)
            }
        }
    }

    fun onGoogleSignInFailed() {
        _uiState.value = BaseViewState.Error(Exception(GOOGLE_SIGN_IN_ERROR))
    }

    companion object {
        internal const val GOOGLE_SIGN_IN_ERROR = "Google sign-in failed"
    }
}
