package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.common.FishingFirebaseUser
import com.mobileprism.fishing.domain.repository.AuthManager
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.viewstates.FishingViewState
import kotlinx.coroutines.flow.*

class MainViewModel(
    private val authManager: AuthManager,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val authState = authManager.authState.take(1)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val appTheme = userPreferences.appTheme
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}