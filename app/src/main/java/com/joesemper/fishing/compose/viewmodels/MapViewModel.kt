package com.joesemper.fishing.compose.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.joesemper.fishing.compose.ui.home.MapUiState
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import com.joesemper.fishing.model.repository.UserContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: UserContentRepository,
) : ViewModel() {

    private val viewStateFlow: MutableStateFlow<BaseViewState> =
        MutableStateFlow(BaseViewState.Loading(null))

    private val mapMarkers = MutableStateFlow(listOf<UserMapMarker>())

    var uiState: MutableState<MapUiState> = mutableStateOf(MapUiState.NormalMode)

    val mapView: MutableState<MapView?> = mutableStateOf(null)

    init {
        loadMarkers()
    }

    fun getAllMarkers(): StateFlow<List<UserMapMarker>> = mapMarkers

    fun subscribe(): StateFlow<BaseViewState> = viewStateFlow


    override fun onCleared() {
        super.onCleared()
        viewStateFlow.value = BaseViewState.Loading(null)
    }

    private fun loadMarkers() {
        viewModelScope.launch {
            repository.getAllUserMarkersList().collect { markers ->
                mapMarkers.value = markers as List<UserMapMarker>
            }
        }
    }

    fun addNewMarker(newMarker: RawMapMarker) {
        viewModelScope.launch {
            repository.addNewMarker(newMarker).collect { progress ->
                when (progress) {
                    is Progress.Complete -> {
                        uiState.value = MapUiState.NormalMode
                    }
                    is Progress.Loading -> {

                    }
                    is Progress.Error -> onError(progress.error)
                }
            }
        }
    }

    private fun onError(error: Throwable) {
        viewStateFlow.value = BaseViewState.Error(error)
    }
}