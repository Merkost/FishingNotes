package com.joesemper.fishing.viewmodel.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.model.auth.AuthManager
import com.joesemper.fishing.model.entity.user.User
import kotlinx.coroutines.cancel
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