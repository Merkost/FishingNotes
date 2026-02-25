package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.mobileprism.fishing.domain.entity.common.Progress
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel(
    private val repository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
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
            repository.addNewUser(user).collect { progress ->
                when (progress) {
                    is Progress.Complete -> {
                        _uiState.value = BaseViewState.Success(user)
                    }
                    is Progress.Loading -> {
                        _uiState.value = BaseViewState.Loading(null)
                    }
                    is Progress.Error -> {
                        _uiState.value = BaseViewState.Error(progress.error)
                    }
                }
            }
        }
    }

    private fun handleError(error: Throwable) {
        _uiState.value = BaseViewState.Error(error)
    }

    fun firebaseSignInWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                firebaseAuth.signInWithCredential(credential).await()
            } catch (e: Exception) {
                analyticsTracker.logEvent(AnalyticsEvent.SignInError(e.message))
                logger.log(e.message)
                _uiState.value = BaseViewState.Error(e)
            }
        }
    }

    fun onGoogleSignInFailed() {
        _uiState.value = BaseViewState.Error(Exception("Google sign-in failed"))
    }

}