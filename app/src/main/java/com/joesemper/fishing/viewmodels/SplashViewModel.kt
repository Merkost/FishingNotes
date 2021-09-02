package com.joesemper.fishing.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.data.auth.AuthManager
import com.joesemper.fishing.data.entity.common.User
import com.joesemper.fishing.viewmodels.viewstates.BaseViewState
import com.joesemper.fishing.data.repository.UserRepository
import com.joesemper.fishing.viewmodels.viewstates.SplashViewState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class SplashViewModel(private val repository: AuthManager) : ViewModel() {

    private val mutableStateFlow: MutableStateFlow<BaseViewState> =
        MutableStateFlow(BaseViewState.Loading(null))

    fun subscribe(): StateFlow<BaseViewState> = mutableStateFlow

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
        mutableStateFlow.value = BaseViewState.Success(user)
    }

    private fun handleError(error: Throwable) {
        mutableStateFlow.value = BaseViewState.Error(error)
    }

}