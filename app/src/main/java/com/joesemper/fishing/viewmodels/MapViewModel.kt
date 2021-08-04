package com.joesemper.fishing.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.data.entity.raw.RawMapMarker
import com.joesemper.fishing.data.repository.MapRepository
import com.joesemper.fishing.data.entity.common.Progress
import com.joesemper.fishing.viewmodels.viewstates.MapViewState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MapViewModel(
    private val mapRepository: MapRepository
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
            val markers = mapRepository.getAllUserMarkers()
            viewStateFlow.value = MapViewState.Success(markers)
        }
    }

//    private fun loadContent() {
//        viewModelScope.launch {
//            val userContent = mapRepository.getAllUserContent()
//            viewStateFlow.value = MapViewState.Success(userContent)
//        }
//    }

    fun addNewMarker(newMarker: RawMapMarker) {
        viewModelScope.launch {
            mapRepository.addNewMarker(newMarker).collect { progress ->
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