package com.joesemper.fishing.viewmodel.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.model.auth.AuthManager
import com.joesemper.fishing.model.entity.user.User
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

class SplashViewModel(private val repository: AuthManager) : ViewModel() {

    private val mutableStateFlow: MutableStateFlow<SplashViewState> =
        MutableStateFlow(SplashViewState.Loading)

    fun subscribe(): StateFlow<SplashViewState> = mutableStateFlow

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            repository.user
                .catch { error -> handleError(error) }
                .collect { user -> onSuccess(user) }
        }
    }

    private fun onSuccess(user: User?) {
        mutableStateFlow.value = SplashViewState.Success(user)
    }

    private fun handleError(error: Throwable) {
        mutableStateFlow.value = SplashViewState.Error(error)
    }

}