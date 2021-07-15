package com.joesemper.fishing.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.model.common.MapMarker
import com.joesemper.fishing.model.common.UserCatch
import com.joesemper.fishing.model.states.AddNewCatchState
import com.joesemper.fishing.data.repository.map.MapRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MapViewModel(
    private val mapRepository: MapRepository
) : ViewModel() {

    private val mutableStateFlow: MutableStateFlow<MapViewState> =
        MutableStateFlow(MapViewState.Loading)

    fun subscribe(): StateFlow<MapViewState> = mutableStateFlow

    init {
        loadUsersMarkers()
    }

    override fun onCleared() {
        super.onCleared()
        mutableStateFlow.value = MapViewState.Loading
    }

    private fun loadUsersMarkers() {
        viewModelScope.launch {
            val userMarkers = mapRepository.getAllUserMarkers()
            mutableStateFlow.value = MapViewState.Success(userMarkers)
        }
    }

    fun addNewCatch(userCatch: UserCatch) {
        mutableStateFlow.value = MapViewState.Loading
        viewModelScope.launch {
            mapRepository.addNewCatch(userCatch).collect { addNewCatchState ->
                when(addNewCatchState) {
                    is AddNewCatchState.Success -> { }
                    is AddNewCatchState.Loading -> { }
                    is AddNewCatchState.Error -> onError(addNewCatchState.error)
                }
            }
        }
    }

    fun deleteMarker(userCatch: UserCatch) {
        viewModelScope.launch {
            mapRepository.deleteMarker(userCatch)
        }
    }

    private fun onSuccess(userMarkers: Flow<MapMarker>) {
        mutableStateFlow.value = MapViewState.Success(userMarkers)
    }

    private fun onError(error: Throwable) {
        mutableStateFlow.value = MapViewState.Error(error)
    }
}