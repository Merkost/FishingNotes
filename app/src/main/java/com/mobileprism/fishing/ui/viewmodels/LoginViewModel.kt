package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.model.entity.common.Progress
import com.mobileprism.fishing.model.entity.common.User
import com.mobileprism.fishing.model.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {

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

}