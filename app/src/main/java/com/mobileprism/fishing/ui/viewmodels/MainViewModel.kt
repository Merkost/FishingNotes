package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(private val repository: UserRepository) : ViewModel() {

    init {
        loadCurrentUser()
    }


    val userState: MutableStateFlow<User?> = MutableStateFlow(null)
    var user: User? = null

    val mutableStateFlow: MutableStateFlow<BaseViewState<User>> =
        MutableStateFlow(BaseViewState.Loading(null))

    private fun loadCurrentUser() {
        viewModelScope.launch {
            repository.currentUser
                .catch { error -> handleError(error) }
                .collectLatest { user -> user?.let { onSuccess(user) } }
        }
    }

    private fun onSuccess(user: User) {
        this.user = user
        viewModelScope.launch {
            repository.setUserListener(user)
        }
        mutableStateFlow.value = BaseViewState.Success(user)
    }

    private fun handleError(error: Throwable) {
        mutableStateFlow.value = BaseViewState.Error(error)
    }
}