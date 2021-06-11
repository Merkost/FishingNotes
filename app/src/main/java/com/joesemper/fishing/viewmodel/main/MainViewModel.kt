package com.joesemper.fishing.viewmodel.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.model.auth.AuthManager
import com.joesemper.fishing.model.entity.user.User
import com.joesemper.fishing.viewmodel.base.BaseViewModel
import com.joesemper.fishing.viewmodel.splash.SplashViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel(private val repository: AuthManager) : ViewModel() {

    private val mutableStateFlow: MutableStateFlow<MainViewState> =
        MutableStateFlow(MainViewState.Loading)

    init {
        loadCurrentUser()
    }

    fun subscribe(): StateFlow<MainViewState> = mutableStateFlow

    fun loadCurrentUser() {
        viewModelScope.launch {
            repository.user
                .catch { error -> handleError(error) }
                .collect { user -> onSuccess(user) }
        }
    }

    fun logOut() {

        viewModelScope.launch {
            repository.logoutCurrentUser()
        }
        mutableStateFlow.value = MainViewState.Success(null)
    }

    private fun onSuccess(user: User?) {
        mutableStateFlow.value = MainViewState.Success(user)
    }

    private fun handleError(error: Throwable) {
        mutableStateFlow.value = MainViewState.Error(error)
    }


}