package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.repository.UserContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserPlacesViewModel(private val repository: UserContentRepository) : ViewModel() {

    val currentContent = MutableStateFlow<List<UserMapMarker>?>(null)

    init {
        loadAllUserPlaces()
    }

    private fun loadAllUserPlaces() {
        viewModelScope.launch {
            repository.getAllUserMarkersList().collect { userPlaces ->
                currentContent.value = userPlaces as List<UserMapMarker>
            }
        }
    }
}