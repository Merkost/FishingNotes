package com.mobileprism.fishing.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.common.SyncState
import com.mobileprism.fishing.domain.entity.common.User
import com.mobileprism.fishing.domain.repository.SyncStatusProvider
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface RoutingDecision {
    data object Splash : RoutingDecision
    data object Onboarding : RoutingDecision
    data object Login : RoutingDecision
    data object Home : RoutingDecision
}

class MainViewModel(
    private val repository: UserRepository,
    private val syncStatusProvider: SyncStatusProvider,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    val syncState: StateFlow<SyncState> = syncStatusProvider.globalSyncState

    private val _userState = MutableStateFlow<BaseViewState<User?>>(BaseViewState.Loading(null))
    val userState: StateFlow<BaseViewState<User?>> = _userState.asStateFlow()

    val routing: StateFlow<RoutingDecision> = combine(
        userState,
        userPreferences.hasCompletedOnboarding,
    ) { userSt, onboardingDone ->
        when {
            userSt is BaseViewState.Loading -> RoutingDecision.Splash
            !onboardingDone -> RoutingDecision.Onboarding
            userSt is BaseViewState.Success && userSt.data != null -> RoutingDecision.Home
            else -> RoutingDecision.Login
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, RoutingDecision.Splash)

    init { loadCurrentUser() }

    private fun loadCurrentUser() {
        val cached = repository.cachedUser

        if (cached != null) {
            _userState.value = BaseViewState.Success(cached)
            viewModelScope.launch { repository.setUserListener(cached) }
        } else {
            _userState.value = BaseViewState.Success(null)
        }

        viewModelScope.launch {
            repository.currentUser
                .run { if (cached != null) dropWhile { it == null } else this }
                .catch { error -> _userState.value = BaseViewState.Error(error) }
                .collectLatest { user ->
                    if (user != null) {
                        repository.setUserListener(user)
                        _userState.value = BaseViewState.Success(user)
                    } else {
                        _userState.value = BaseViewState.Success(null)
                    }
                }
        }
    }
}
