package com.mobileprism.fishing.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.model.datastore.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val hasCompletedOnboarding: StateFlow<Boolean?> = userPreferences.hasCompletedOnboarding
        .map<Boolean, Boolean?> { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isPromptCardDismissed: StateFlow<Boolean> = userPreferences.hasPromptCardDismissed
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferences.saveOnboardingCompleted(true)
        }
    }

    fun dismissPromptCard() {
        viewModelScope.launch {
            userPreferences.savePromptCardDismissed(true)
        }
    }
}
