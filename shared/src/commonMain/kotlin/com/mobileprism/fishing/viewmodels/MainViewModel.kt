package com.mobileprism.fishing.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.common.SyncState
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.SyncStatusProvider
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: UserRepository,
    private val syncStatusProvider: SyncStatusProvider
) : ViewModel() {
    init { loadCurrentUser() }

    val syncState: StateFlow<SyncState> = syncStatusProvider.globalSyncState

    private val _userState = MutableStateFlow<BaseViewState<User>>(BaseViewState.Loading(null))
    val userState: StateFlow<BaseViewState<User>> = _userState.asStateFlow()

    private fun loadCurrentUser() {
        viewModelScope.launch {
            repository.currentUser
                .catch { error -> _userState.value = BaseViewState.Error(error) }
                .collectLatest { user ->
                    user?.let {
                        repository.setUserListener(it)
                        _userState.value = BaseViewState.Success(it)
                    }
                }
        }
    }
}
