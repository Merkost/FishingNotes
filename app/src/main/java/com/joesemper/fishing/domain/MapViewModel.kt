package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.domain.viewstates.MapViewState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: UserContentRepository
) : ViewModel() {

    private val viewStateFlow: MutableStateFlow<MapViewState> =
        MutableStateFlow(MapViewState.Loading)

    fun subscribe(): StateFlow<MapViewState> = viewStateFlow

    init {
        loadMarkers()
    }

    override fun onCleared() {
        super.onCleared()
        viewStateFlow.value = MapViewState.Loading
    }

    private fun loadMarkers() {
        viewModelScope.launch {
            val markers = repository.getAllUserMarkers()
            viewStateFlow.value = MapViewState.Success(markers)
        }
    }

    fun addNewMarker(newMarker: RawMapMarker) {
        viewModelScope.launch {
            repository.addNewMarker(newMarker).collect { progress ->
                when(progress) {
                    is Progress.Complete -> { }
                    is Progress.Loading -> { }
                    is Progress.Error -> onError(progress.error)
                }
            }
        }
    }

    private fun onError(error: Throwable) {
        viewStateFlow.value = MapViewState.Error(error)
    }
}