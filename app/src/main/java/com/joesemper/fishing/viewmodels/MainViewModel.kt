package com.joesemper.fishing.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.data.auth.AuthManager
import com.joesemper.fishing.data.entity.common.User
import com.joesemper.fishing.viewmodels.viewstates.MainViewState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: AuthManager) : ViewModel() {

    private val mutableStateFlow: MutableStateFlow<MainViewState> =
        MutableStateFlow(MainViewState.Loading)

    init {
        loadCurrentUser()
    }

    fun subscribe(): StateFlow<MainViewState> = mutableStateFlow

    private fun loadCurrentUser() {
        viewModelScope.launch {
            repository.currentUser
                .catch { error -> handleError(error) }
                .collectLatest { user -> onSuccess(user) }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            repository.logoutCurrentUser()
        }
    }

    private fun onSuccess(user: User?) {
        mutableStateFlow.value = MainViewState.Success(user)
    }

    private fun handleError(error: Throwable) {
        mutableStateFlow.value = MainViewState.Error(error)
    }


}