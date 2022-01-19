package com.joesemper.fishing.compose.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.maps.model.LatLng
import com.joesemper.fishing.compose.ui.home.UiState
import com.joesemper.fishing.compose.ui.home.map.MapUiState
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.domain.viewstates.RetrofitWrapper
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import com.joesemper.fishing.model.entity.solunar.Solunar
import com.joesemper.fishing.model.entity.weather.CurrentWeatherFree
import com.joesemper.fishing.model.repository.app.FreeWeatherRepository
import com.joesemper.fishing.model.repository.app.MarkersRepository
import com.joesemper.fishing.model.repository.app.SolunarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class MapViewModel(
    private val repository: MarkersRepository,
    private val freeWeatherRepository: FreeWeatherRepository,
    private val solunarRepository: SolunarRepository,
) : ViewModel() {

    private val _showMarker = MutableStateFlow(false)
    val showMarker = _showMarker.asStateFlow()

    private val viewStateFlow: MutableStateFlow<BaseViewState> =
        MutableStateFlow(BaseViewState.Loading(null))

    private val _mapMarkers: MutableStateFlow<MutableList<UserMapMarker>> =
        MutableStateFlow(mutableListOf())
    val mapMarkers = _mapMarkers.asStateFlow()

    private val _mapUiState = MutableStateFlow<MapUiState>(MapUiState.NormalMode)
    val mapUiState = _mapUiState.asStateFlow()

//    @ExperimentalMaterialApi
//    var sheetState: BottomSheetValue = BottomSheetValue.Collapsed

    private val _uiState = MutableStateFlow<UiState?>(null)
    val uiState = _uiState.asStateFlow()

    private val _firstLaunchLocation = MutableStateFlow(true)
    val firstLaunchLocation = _firstLaunchLocation.asStateFlow()

    private val _lastKnownLocation = MutableStateFlow<LatLng?>(null)
    val lastKnownLocation = _lastKnownLocation.asStateFlow()

    private val _lastMapCameraPosition = MutableStateFlow<Pair<LatLng, Float>?>(null)
    val lastMapCameraPosition = _lastMapCameraPosition.asStateFlow()

    private val _currentMarker = MutableStateFlow<UserMapMarker?>(null)
    val currentMarker = _currentMarker.asStateFlow()

    private val _chosenPlace = MutableStateFlow<String?>(null)
    val chosenPlace = _chosenPlace.asStateFlow()

    private val _fishActivity = MutableStateFlow<Int?>(null)
    val fishActivity = _fishActivity.asStateFlow()

    private val _currentWeather = MutableStateFlow<CurrentWeatherFree?>(null)
    val currentWeather = _currentWeather.asStateFlow()

    init {
        loadMarkers()
    }

    private fun loadMarkers() {
        viewModelScope.launch {
            repository.getAllUserMarkersList().collect { markers ->
                _mapMarkers.value = markers as MutableList<UserMapMarker>
            }
        }
    }

    fun updateMapUiState(state: MapUiState) {
        _mapUiState.value = state
    }

    fun updateLastCameraPosition(position: Pair<LatLng, Float>) {
        _lastMapCameraPosition.value = position
    }

    fun updateCurrentMarker(marker: UserMapMarker?) {
        _currentMarker.value = marker
    }

    fun updateLastKnownLocation(location: LatLng) {
        _lastKnownLocation.value = location
    }

    fun setFirstLaunchLocation(value: Boolean) {
        _firstLaunchLocation.value = value
    }

    fun setChosenPlace(place: String?) {
        _chosenPlace.value = place
    }

    fun setShowMarker(value: Boolean) {
        _showMarker.value = value
    }


    override fun onCleared() {
        super.onCleared()
        viewStateFlow.value = BaseViewState.Loading(null)
    }

    fun addNewMarker(newMarker: RawMapMarker) {
        viewModelScope.launch {
            repository.addNewMarker(newMarker).collect { progress ->
                when (progress) {
                    is Progress.Complete -> {
                        _uiState.value = UiState.Success
                    }
                    is Progress.Loading -> {
                        _uiState.value = UiState.InProgress
                    }
                    is Progress.Error -> onError(progress.error)
                }
            }
        }
    }


    fun getCurrentWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            freeWeatherRepository
                .getCurrentWeatherFree(latitude, longitude).collect { result ->
                    when (result) {
                        is RetrofitWrapper.Success<CurrentWeatherFree> -> {
                            _currentWeather.value = result.data
                        }
                        is RetrofitWrapper.Error -> {
                            Log.d("CURRENT WEATHER ERROR", result.errorType.error.toString())
                            //_weatherState.value = RetrofitWrapper.Error(result.errorType)
                        }
                    }
                }
        }
    }

    fun getFishActivity(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            solunarRepository.getSolunar(latitude, longitude).collect { result ->
                when (result) {
                    is RetrofitWrapper.Success<Solunar> -> {
                        val solunar = result.data
                        val calendar = Calendar.getInstance()
                        val currentHour24 = calendar[Calendar.HOUR_OF_DAY]
                        _fishActivity.value = solunar.hourlyRating[currentHour24]
                        //_weatherState.value = RetrofitWrapper.Success(result.data)
                    }
                    is RetrofitWrapper.Error -> {
                        Log.d("SOLUNAR ERROR", result.errorType.error.toString())
                        //_weatherState.value = RetrofitWrapper.Error(result.errorType)
                    }
                }
            }
        }
    }


    private fun onError(error: Throwable) {
        viewStateFlow.value = BaseViewState.Error(error)
    }

    fun updateCurrentPlace(markerToUpdate: UserMapMarker) {
        viewModelScope.launch {
            repository.getMapMarker(markerToUpdate.id).collect { updatedMarker ->
                updatedMarker?.let {
                    currentMarker.value?.let {
                        _currentMarker.value = updatedMarker
                    }
                    _mapMarkers.value.apply {
                        remove(markerToUpdate)
                        add(updatedMarker)
                    }
                }
            }
        }
    }


}