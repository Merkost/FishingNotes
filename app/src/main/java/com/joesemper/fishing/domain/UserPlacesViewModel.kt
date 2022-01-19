package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.compose.ui.home.UiState
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.repository.app.MarkersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserPlacesViewModel(private val repository: MarkersRepository) : ViewModel() {

    private val _currentContent = MutableStateFlow<List<UserMapMarker>>(listOf())
    val currentContent = _currentContent.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.InProgress)
    val uiState = _uiState.asStateFlow()

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