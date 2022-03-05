package com.mobileprism.fishing.viewmodels

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.ui.home.map.*
import com.mobileprism.fishing.domain.viewstates.BaseViewState
import com.mobileprism.fishing.domain.viewstates.Result
import com.mobileprism.fishing.domain.viewstates.RetrofitWrapper
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.model.entity.common.Progress
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.entity.raw.RawMapMarker
import com.mobileprism.fishing.model.entity.solunar.Solunar
import com.mobileprism.fishing.model.entity.weather.CurrentWeatherFree
import com.mobileprism.fishing.model.repository.app.FreeWeatherRepository
import com.mobileprism.fishing.model.repository.app.MarkersRepository
import com.mobileprism.fishing.model.repository.app.SolunarRepository
import com.mobileprism.fishing.model.use_cases.GetFishActivityUseCase
import com.mobileprism.fishing.ui.home.map.MapUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class MapViewModel(
    private val repository: MarkersRepository,
    private val freeWeatherRepository: FreeWeatherRepository,
    private val getFishActivityUseCase: GetFishActivityUseCase,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val firstLaunchLocation = mutableStateOf(true)

    val showMarker: MutableState<Boolean> = mutableStateOf(false)
    private val _addNewMarkerState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Success)
    val addNewMarkerState = _addNewMarkerState.asStateFlow()

    private var _mapMarkers: MutableStateFlow<MutableList<UserMapMarker>> =
        MutableStateFlow(mutableListOf())
    val mapMarkers: StateFlow<MutableList<UserMapMarker>>
        get() = _mapMarkers

    private val _mapUiState: MutableStateFlow<MapUiState> = MutableStateFlow(MapUiState.NormalMode)
    val mapUiState = _mapUiState.asStateFlow()

    private val _cameraMoveState = MutableStateFlow<CameraMoveState>(CameraMoveState.MoveFinish)
    val cameraMoveState = _cameraMoveState.asStateFlow()

    private val _uiState = MutableStateFlow<UiState?>(null)
    val uiState = _uiState.asStateFlow()

    val mapType = mutableStateOf(MapTypes.roadmap)
    val mapBearing = mutableStateOf(0f)

    val lastKnownLocation = mutableStateOf<LatLng?>(null)
    val lastMapCameraPosition = mutableStateOf<Pair<LatLng, Float>?>(null)

    val currentCameraPosition = mutableStateOf(Pair(LatLng(0.0, 0.0), 0f))

    private val _currentMarker: MutableStateFlow<UserMapMarker?> = MutableStateFlow(null)
    val currentMarker = _currentMarker.asStateFlow()

    val chosenPlace = mutableStateOf<String?>(null)

    val fishActivity: MutableState<Int?> = mutableStateOf(null)
    val currentWeather: MutableState<CurrentWeatherFree?> = mutableStateOf(null)

    val windIconRotation: Float
        get() = currentWeather.value?.wind_degrees?.minus(mapBearing.value) ?: mapBearing.value

    init {
        loadMarkers()
    }

    fun getAllMarkers(): StateFlow<List<UserMapMarker>> = _mapMarkers

    fun setCameraMoveState(newState: CameraMoveState) {
        _cameraMoveState.value = newState
    }

    private fun loadMarkers() {
        viewModelScope.launch {
            repository.getAllUserMarkersList().collect { markers ->
                _mapMarkers.value = markers as MutableList<UserMapMarker>
            }
        }
    }

    fun addNewMarker(newMarker: RawMapMarker) {
        _addNewMarkerState.value = UiState.InProgress
        viewModelScope.launch {
            repository.addNewMarker(newMarker).collect { progress ->
                when (progress) {
                    is Result.Success -> {
                        _addNewMarkerState.value = UiState.Success
                    }
                    is Result.Error -> {
                        _uiState.value = UiState.Error
                    }
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
                            currentWeather.value = result.data
                        }
                        is RetrofitWrapper.Error -> {
                            Log.d("CURRENT WEATHER ERROR", result.errorType.error.toString())
                            //_weatherState.value = RetrofitWrapper.Error(result.errorType)
                        }
                        else -> {}
                    }
                }
        }
    }

    fun getFishActivity(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            getFishActivityUseCase.invoke(latitude, longitude).collect {
                fishActivity.value = it
            }
        }
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

    fun setLastMapCameraPosition(value: Pair<LatLng, Float>) {
        lastMapCameraPosition.value = value
    }

    fun saveLastCameraPosition(pair: Pair<LatLng, Float>) {
        viewModelScope.launch {
            userPreferences.saveLastMapCameraLocation(pair)
        }
    }

    fun quickAddPlace(name: String) {
        if (mapUiState.value is MapUiState.NormalMode) {
            viewModelScope.launch {
                lastKnownLocation.value?.let {
                    addNewMarker(
                        RawMapMarker(
                            name,
                            latitude = it.latitude,
                            longitude = it.longitude,
                        )
                    )
                }
            }
        }
    }

    fun setPlace(place: UserMapMarker?) {
        place?.let {
            if (it.id.isNotEmpty()) {
                _currentMarker.value = place
            }
            lastMapCameraPosition.value =
                Pair(LatLng(it.latitude, it.longitude), DEFAULT_ZOOM)
        }
    }

    fun setAddingPlace(addPlaceOnStart: Boolean) {
        when {
            addPlaceOnStart -> _mapUiState.value = MapUiState.PlaceSelectMode
            _currentMarker.value != null -> _mapUiState.value = MapUiState.BottomSheetInfoMode
        }
    }

    fun resetMapUiState() {
        _mapUiState.value = MapUiState.NormalMode
        _currentMarker.value = null
    }

    fun onMarkerClicked(marker: UserMapMarker?) {
        marker?.let {
            _currentMarker.value = it
            _mapUiState.value = MapUiState.BottomSheetInfoMode
        }
    }

    fun setPlaceSelectionMode() {
        _mapUiState.value = MapUiState.PlaceSelectMode
    }

    fun locationGranted(location: LatLng) {
        lastKnownLocation.value = location
        if (firstLaunchLocation.value) {
            viewModelScope.launch {
                if (currentMarker.value == null) {
                    lastMapCameraPosition.value = userPreferences.getLastMapCameraLocation.first()
                }
                firstLaunchLocation.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _addNewMarkerState.value = UiState.InProgress
    }

}

