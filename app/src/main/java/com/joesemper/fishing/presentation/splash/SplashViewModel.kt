package com.joesemper.fishing.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.data.auth.AuthManager
import com.joesemper.fishing.model.common.User
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class SplashViewModel(private val repository: AuthManager) : ViewModel() {

    private val mutableStateFlow: MutableStateFlow<SplashViewState> =
        MutableStateFlow(SplashViewState.Loading)

    fun subscribe(): StateFlow<SplashViewState> = mutableStateFlow

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            repository.currentUser
                .catch { error -> handleError(error) }
                .collectLatest { user -> onSuccess(user) }
        }
    }

    private fun onSuccess(user: User?) {
        mutableStateFlow.value = SplashViewState.Success(user)
    }

    private fun handleError(error: Throwable) {
        mutableStateFlow.value = SplashViewState.Error(error)
    }

}