package com.joesemper.fishing.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.data.entity.RawUserCatch
import com.joesemper.fishing.model.common.content.UserCatch
import com.joesemper.fishing.data.repository.map.MapRepository
import com.joesemper.fishing.model.common.content.MapMarker
import com.joesemper.fishing.model.common.Progress
import com.joesemper.fishing.model.common.content.Content
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MapViewModel(
    private val mapRepository: MapRepository
) : ViewModel() {

    private val viewStateFlow: MutableStateFlow<MapViewState> =
        MutableStateFlow(MapViewState.Loading)

    fun subscribe(): StateFlow<MapViewState> = viewStateFlow

    init {
        loadContent()
    }

    override fun onCleared() {
        super.onCleared()
        viewStateFlow.value = MapViewState.Loading
    }

    private fun loadContent() {
        viewModelScope.launch {
            val userContent = mapRepository.getAllUserContent()
            viewStateFlow.value = MapViewState.Success(userContent)
        }
    }

    fun addNewCatch(newCatch: RawUserCatch) {
//        viewStateFlow.value = MapViewState.Loading
        viewModelScope.launch {
            mapRepository.addNewCatch(newCatch).collect { progress ->
                when(progress) {
                    is Progress.Complete -> { }
                    is Progress.Loading -> { }
                    is Progress.Error -> onError(progress.error)
                }
            }
        }
    }

//    suspend fun getMarker(markerId: String): MapMarker? {
////        return mapRepository.getMarker(markerId)
//    }

    fun deleteMarker(userCatch: UserCatch) {
        viewModelScope.launch {
            mapRepository.deleteMarker(userCatch)
        }
    }

    private fun onSuccess(userMarkers: Flow<Content>) {
        viewStateFlow.value = MapViewState.Success(userMarkers)
    }

    private fun onError(error: Throwable) {
        viewStateFlow.value = MapViewState.Error(error)
    }
}