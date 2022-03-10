package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.repository.app.MarkersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserPlacesViewModel(private val repository: MarkersRepository) : ViewModel() {

    private val _currentContent = MutableStateFlow<List<UserMapMarker>>(listOf())
    val currentContent: StateFlow<List<UserMapMarker>>
    get() = _currentContent

    private val _uiState = MutableStateFlow<UiState>(UiState.InProgress)
    val uiState: StateFlow<UiState>
        get() = _uiState

    init {
        loadAllUserPlaces()
    }

    private fun loadAllUserPlaces() {
        _uiState.value = UiState.InProgress
        viewModelScope.launch {
            repository.getAllUserMarkersList().collect { userPlaces ->
                _currentContent.value = userPlaces as List<UserMapMarker>
                _uiState.value = UiState.Success
            }
        }
    }
}