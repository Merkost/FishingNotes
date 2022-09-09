package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.repository.app.FishRepository
import com.mobileprism.fishing.ui.home.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val fishRepository: FishRepository
) : ViewModel() {

    private val _fishUpdateResult = MutableStateFlow<UiState?>(null)
    val fishUpdateResult = _fishUpdateResult.asStateFlow()

    fun updateFish() {
        viewModelScope.launch {
            _fishUpdateResult.update { UiState.InProgress }
            fishRepository.updateFish().fold(
                onSuccess = {
                    _fishUpdateResult.update { UiState.Success }
                },
                onFailure = {
                    _fishUpdateResult.update { UiState.Error }
                }
            )
        }
    }
}