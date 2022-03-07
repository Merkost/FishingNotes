package com.mobileprism.fishing.viewmodels

import android.location.Geocoder
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.ui.home.map.*
import com.mobileprism.fishing.domain.viewstates.Result
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.entity.raw.RawMapMarker
import com.mobileprism.fishing.model.entity.weather.CurrentWeatherFree
import com.mobileprism.fishing.model.repository.app.MarkersRepository
import com.mobileprism.fishing.model.use_cases.GetFishActivityUseCase
import com.mobileprism.fishing.model.use_cases.GetFreeWeatherUseCase
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.map.MapUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: MarkersRepository,
    private val getFreeWeatherUseCase: GetFreeWeatherUseCase,
    private val getFishActivityUseCase: GetFishActivityUseCase,
    private val geocoder: Geocoder,
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

    private val _addNewMarkerUiState = MutableStateFlow<UiState?>(null)
    val addNewMarkerUiState = _addNewMarkerUiState.asStateFlow()

    private val _mapType = MutableStateFlow(MapTypes.roadmap)
    val mapType = _mapType.asStateFlow()
    fun onLayerSelected(layer: Int) { _mapType.value = layer }

    val mapBearing = MutableStateFlow(0f)

    val lastKnownLocation = mutableStateOf<LatLng?>(null)
    val lastMapCameraPosition = mutableStateOf<Triple<LatLng, Float, Float>?>(null)

    /**
     * A Pair of LatLng, Zoom and Bearing
     */
    private val _newMapCameraPosition = MutableSharedFlow<Triple<LatLng, Float, Float>>()
    val newMapCameraPosition = _newMapCameraPosition.asSharedFlow()

    private val _currentCameraPosition = MutableStateFlow(Triple(LatLng(0.0, 0.0), 0f, 0f))
    val currentCameraPosition = _currentCameraPosition.asStateFlow()

    private val _currentMarker: MutableStateFlow<UserMapMarker?> = MutableStateFlow(null)
    val currentMarker = _currentMarker.asStateFlow()

    val chosenPlace = mutableStateOf<String?>(null)

    private val _currentMarkerAddress = MutableStateFlow<String?>(null)
    val currentMarkerAddress = _currentMarkerAddress.asStateFlow()

    private val _currentMarkerRawDistance = MutableStateFlow<Double?>(null)
    val currentMarkerRawDistance = _currentMarkerRawDistance.asStateFlow()


    val fishActivity: MutableState<Int?> = mutableStateOf(null)
    val currentWeather: MutableState<CurrentWeatherFree?> = mutableStateOf(null)

    val windIconRotation: Float
        get() = currentWeather.value?.wind_degrees?.minus(_currentCameraPosition.value.third)
            ?: _currentCameraPosition.value.third

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
                        _addNewMarkerUiState.value = UiState.Error
                    }
                }
            }
        }
    }

    fun getCurrentWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            getFreeWeatherUseCase.invoke(latitude, longitude).collect {
                currentWeather.value = it
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

    fun saveLastCameraPosition() {
        viewModelScope.launch {
            if (!firstLaunchLocation.value)
            lastMapCameraPosition.value = currentCameraPosition.value
            userPreferences.saveLastMapCameraLocation(currentCameraPosition.value)
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
            /*lastMapCameraPosition.value =
                Pair(LatLng(it.latitude, it.longitude), DEFAULT_ZOOM)*/
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

    fun onMyLocationClick() {
        lastKnownLocation.value?.let {
            setNewCameraLocation(it)
        } ?: SnackbarManager.showMessage(R.string.cant_get_current_location)
    }

    private fun setNewCameraLocation(position: LatLng, zoom: Float = DEFAULT_ZOOM) {
        viewModelScope.launch {
            _newMapCameraPosition.emit(_currentCameraPosition.value.copy(position, zoom))
        }
    }

    fun onMarkerClicked(marker: UserMapMarker?, position: LatLng) {
        marker?.let {
            setNewCameraLocation(position, DEFAULT_ZOOM)
            _currentMarker.value = it
            _mapUiState.value = MapUiState.BottomSheetInfoMode
        }
    }

    fun setPlaceSelectionMode() {
        _mapUiState.value = MapUiState.PlaceSelectMode
    }

    fun onZoomInClick() {
        val currentCamera = _currentCameraPosition.value
        setNewCameraLocation(currentCamera.first, currentCamera.second + 2f)
    }

    fun onZoomOutClick() {
        val currentCamera = _currentCameraPosition.value
        setNewCameraLocation(currentCamera.first, currentCamera.second - 2f)
    }

    fun locationGranted(location: LatLng) {
        if (!firstLaunchLocation.value) lastKnownLocation.value = location
    }

    fun resetMapBearing() {
        viewModelScope.launch {
            _newMapCameraPosition.emit(_currentCameraPosition.value.copy(third = 0f))
        }
    }

    override fun onCleared() {
        super.onCleared()
        _addNewMarkerState.value = UiState.InProgress
    }

    fun setNewMarkerInfo(latitude: Double, longitude: Double) {
        fishActivity.value = null
        currentWeather.value = null
        getPlaceName(latitude, longitude)
        getPlaceRawDistance(latitude, longitude)
        getFishActivity(latitude, longitude)
        getCurrentWeather(latitude, longitude)
    }

    private fun getPlaceRawDistance(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.Default) {
            _currentMarkerRawDistance.value = null
            lastKnownLocation.value?.let {
                _currentMarkerRawDistance.value =
                    SphericalUtil.computeDistanceBetween(
                        LatLng(latitude, longitude),
                        LatLng(it.latitude, it.longitude)
                    )
            }
        }
    }

    private fun getPlaceName(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.Default) {
            _currentMarkerAddress.value = null
            try {
                val position = geocoder.getFromLocation(latitude, longitude, 1)
                position?.first()?.apply {
                    _currentMarkerAddress.value = if (!subAdminArea.isNullOrBlank()) {
                        subAdminArea.replaceFirstChar { it.uppercase() }
                    } else if (!adminArea.isNullOrBlank()) {
                        adminArea.replaceFirstChar { it.uppercase() }
                    } else if (!countryName.isNullOrBlank())
                        countryName.replaceFirstChar { it.uppercase() }
                    else "-"
                }
            } catch (e: Throwable) {
                //TODO: Не удается определить место в океане
                //address = context.getString(R.string.cant_recognize_place)
            }
        }
    }

    fun onCameraMove(target: LatLng, zoom: Float, bearing: Float) {
        viewModelScope.launch {
            mapBearing.value = bearing
            _currentCameraPosition.value = Triple(target, zoom, bearing)
        }
    }

    fun getFirstLaunchLocation() {
        if (firstLaunchLocation.value) {
            viewModelScope.launch {
                if (currentMarker.value == null) {
                    val fromBd = userPreferences.getLastMapCameraLocation.first()
                    _newMapCameraPosition.emit(fromBd)
                }
                firstLaunchLocation.value = false
            }
        }
    }


}

