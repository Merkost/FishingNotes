package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.model.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SplashViewModel(private val repository: UserRepository) : ViewModel() {

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