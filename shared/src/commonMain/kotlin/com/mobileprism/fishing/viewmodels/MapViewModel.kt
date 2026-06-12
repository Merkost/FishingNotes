package com.mobileprism.fishing.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.raw.RawMapMarker
import com.mobileprism.fishing.domain.entity.weather.CurrentWeatherFree
import com.mobileprism.fishing.domain.use_cases.*
import com.mobileprism.fishing.domain.use_cases.places.AddNewPlaceUseCase
import com.mobileprism.fishing.domain.use_cases.places.GetUserPlacesListUseCase
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.ui.home.map.*
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.utils.ValidationUtils
import com.mobileprism.fishing.utils.location.LocationManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MapViewModel(
    private val getUserPlacesListUseCase: GetUserPlacesListUseCase,
    private val addNewPlaceUseCase: AddNewPlaceUseCase,
    private val getFreeWeatherUseCase: GetFreeWeatherUseCase,
    private val getFishActivityUseCase: GetFishActivityUseCase,
    private val getPlaceNameUseCase: PlaceNameResolver,
    private val userPreferences: UserPreferences,
    private val locationManager: LocationManager,
) : ViewModel() {

    private var _mapMarkers: MutableStateFlow<MutableList<UserMapMarker>> =
        MutableStateFlow(mutableListOf())
    val mapMarkers: StateFlow<List<UserMapMarker>>
        get() = _mapMarkers

    private val initialPlaceSelected = MutableStateFlow(false)

    private val _firstCameraPosition = MutableStateFlow<MapCameraState?>(null)
    val firstCameraPosition = _firstCameraPosition.asStateFlow()

    private val _addNewMarkerState: MutableStateFlow<UiState?> = MutableStateFlow(null)
    val addNewMarkerState = _addNewMarkerState.asStateFlow()

    private val _mapUiState: MutableStateFlow<MapUiState> = MutableStateFlow(MapUiState.NormalMode)
    val mapUiState = _mapUiState.asStateFlow()

    private val _cameraMoveState = MutableStateFlow<CameraMoveState>(CameraMoveState.MoveFinish)

    private val _mapType = MutableStateFlow(AppMapType.Roadmap)
    val mapType = _mapType.asStateFlow()
    fun onLayerSelected(layer: AppMapType) {
        _mapType.value = layer
    }

    val mapBearing = MutableStateFlow(0f)

    private val _lastKnownLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val lastKnownLocation = _lastKnownLocation.asStateFlow()

    private val _myLocationButtonState =
        MutableStateFlow<MyLocationButtonState>(MyLocationButtonState.Ready)
    val myLocationButtonState = _myLocationButtonState.asStateFlow()

    private val lastMapCameraPosition = MutableStateFlow<MapCameraState?>(null)

    private val _newMapCameraPosition = MutableSharedFlow<MapCameraState>()
    val newMapCameraPosition = _newMapCameraPosition.asSharedFlow()

    private val _currentCameraPosition = MutableStateFlow(MapCameraState())
    val currentCameraPosition = _currentCameraPosition.asStateFlow()

    private val _currentMarker: MutableStateFlow<UserMapMarker?> = MutableStateFlow(null)
    val currentMarker = _currentMarker.asStateFlow()

    private val _currentMarkerAddressState =
        MutableStateFlow<GeocoderResult>(GeocoderResult.InProgress)
    val currentMarkerAddressState = _currentMarkerAddressState.asStateFlow()

    private val _placeTileViewNameState = MutableStateFlow<PlaceTileState>(PlaceTileState())
    val placeTileViewNameState = _placeTileViewNameState.asStateFlow()

    private val _currentMarkerRawDistance = MutableStateFlow<Double?>(null)
    val currentMarkerRawDistance = _currentMarkerRawDistance.asStateFlow()

    private val _fishActivity = MutableStateFlow<Int?>(null)
    val fishActivity = _fishActivity.asStateFlow()
    private val _currentWeather = MutableStateFlow<CurrentWeatherFree?>(null)
    val currentWeather = _currentWeather.asStateFlow()

    private val _fishActivityLoading = MutableStateFlow(false)
    private val _currentWeatherLoading = MutableStateFlow(false)
    val placeStatsLoading = combine(
        _fishActivityLoading,
        _currentWeatherLoading,
        _fishActivity,
        _currentWeather,
    ) { fishActivityLoading, currentWeatherLoading, fishActivity, currentWeather ->
        fishActivity == null &&
            currentWeather == null &&
            (fishActivityLoading || currentWeatherLoading)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val windIconRotation = _currentWeather.combine(_currentCameraPosition) { weather, camera ->
        weather?.wind_degrees?.minus(camera.bearing) ?: camera.bearing
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0f)

    init {
        loadUserMarkersList()
    }

    fun setCameraMoveState(newState: CameraMoveState) {
        _cameraMoveState.value = newState
    }

    private fun loadUserMarkersList() {
        viewModelScope.launch {
            getUserPlacesListUseCase.invoke().collect { markers ->
                _mapMarkers.value = markers as MutableList<UserMapMarker>
                if (!markers.contains(currentMarker.value)) {
                    resetMapUiState()
                }
            }
        }
    }

    private var addNewMarkerJob: Job? = null
    fun cancelAddNewMarker() {
        addNewMarkerJob?.cancel()
        _addNewMarkerState.value = null
    }

    fun addNewMarker(newMarker: RawMapMarker) {
        if (!ValidationUtils.isCoordinateValid(newMarker.latitude, newMarker.longitude)) {
            SnackbarManager.showMessage(Res.string.invalid_coordinates)
            _addNewMarkerState.value = UiState.Error
            return
        }
        _addNewMarkerState.value = UiState.InProgress
        addNewMarkerJob = viewModelScope.launch {
            addNewPlaceUseCase.invoke(newMarker).single().fold(
                onSuccess = {
                    _addNewMarkerState.value = UiState.Success
                },
                onFailure = {
                    _addNewMarkerState.value = UiState.Error
                }
            )
        }
    }

    private var currentWeatherJob: Job? = null
    fun getCurrentWeather(latitude: Double, longitude: Double) {
        currentWeatherJob?.cancel()
        currentWeatherJob = viewModelScope.launch {
            _currentWeatherLoading.value = true
            try {
                getFreeWeatherUseCase.invoke(latitude, longitude).collect {
                    _currentWeather.value = it
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                _currentWeather.value = null
            } finally {
                _currentWeatherLoading.value = false
            }
        }
    }

    private var fishActivityJob: Job? = null
    fun getFishActivity(latitude: Double, longitude: Double) {
        fishActivityJob?.cancel()
        fishActivityJob = viewModelScope.launch {
            _fishActivityLoading.value = true
            try {
                getFishActivityUseCase.invoke(latitude, longitude).collect {
                    _fishActivity.value = it
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                _fishActivity.value = null
            } finally {
                _fishActivityLoading.value = false
            }
        }
    }

    fun saveLastCameraPosition() {
        viewModelScope.launch {
            if (!initialPlaceSelected.value) {
                userPreferences.saveLastMapCameraLocation(currentCameraPosition.value)
            }
            lastMapCameraPosition.value = currentCameraPosition.value
        }
    }

    fun quickAddPlace(name: String) {
        if (mapUiState.value is MapUiState.NormalMode) {
            val trimmedName = name.trim().take(ValidationUtils.MAX_PLACE_NAME_LENGTH)
            viewModelScope.launch {
                _lastKnownLocation.value?.let { (lat, lng) ->
                    addNewMarker(
                        RawMapMarker(
                            trimmedName.ifEmpty { name },
                            latitude = lat,
                            longitude = lng,
                        )
                    )
                }
            }
        }
    }

    fun setPlace(place: UserMapMarker?) {
        when {
            place == null -> initialPlaceSelected.value = false
            place.id == Constants.CURRENT_PLACE_ITEM_ID -> {
                initialPlaceSelected.value = true
                _firstCameraPosition.value =
                    _currentCameraPosition.value.copy(
                        latitude = place.latitude,
                        longitude = place.longitude,
                        zoom = DEFAULT_ZOOM
                    )
            }
            else -> {
                initialPlaceSelected.value = true
                _currentMarker.value = place
                _mapUiState.value = MapUiState.BottomSheetInfoMode
                _firstCameraPosition.value =
                    _currentCameraPosition.value.copy(
                        latitude = place.latitude,
                        longitude = place.longitude,
                        zoom = DEFAULT_ZOOM
                    )
            }
        }
    }

    fun setAddingPlace(addPlaceOnStart: Boolean) {
        when {
            addPlaceOnStart -> _mapUiState.value = MapUiState.PlaceSelectMode
        }
    }

    fun resetMapUiState() {
        _mapUiState.value = MapUiState.NormalMode
        _currentMarker.value = null
    }

    private var myLocationJob: Job? = null
    fun onMyLocationClick() {
        myLocationJob?.cancel()
        myLocationJob = viewModelScope.launch {
            _myLocationButtonState.value = MyLocationButtonState.Searching
            val result = runCatching {
                withTimeoutOrNull(CURRENT_LOCATION_TIMEOUT_MS) {
                    locationManager.getCurrentLocationFlow().firstOrNull()
                }
            }.getOrNull()
            when (result) {
                is LocationState.LocationGranted -> {
                    _lastKnownLocation.value = Pair(result.latitude, result.longitude)
                    setNewCameraLocation(result.latitude, result.longitude)
                    _myLocationButtonState.value = MyLocationButtonState.Ready
                }
                LocationState.NoPermission -> {
                    _myLocationButtonState.value = MyLocationButtonState.NeedsPermission
                    SnackbarManager.showMessage(Res.string.location_permissions_required)
                }
                LocationState.GpsNotEnabled -> {
                    _myLocationButtonState.value = MyLocationButtonState.GpsDisabled
                    SnackbarManager.showMessage(Res.string.gps_is_off)
                }
                LocationState.Unavailable,
                null -> {
                    _myLocationButtonState.value = MyLocationButtonState.Unavailable
                    SnackbarManager.showMessage(Res.string.unable_to_get_location)
                }
            }
        }
    }

    fun onMyLocationGpsDisabled() {
        _myLocationButtonState.value = MyLocationButtonState.GpsDisabled
        SnackbarManager.showMessage(Res.string.gps_is_off)
    }

    private fun setNewCameraLocation(latitude: Double, longitude: Double, zoom: Float = DEFAULT_ZOOM) {
        viewModelScope.launch {
            _newMapCameraPosition.emit(
                _currentCameraPosition.value.copy(latitude = latitude, longitude = longitude, zoom = zoom)
            )
        }
    }

    fun onMarkerClicked(marker: UserMapMarker?) {
        marker?.let {
            setNewCameraLocation(it.latitude, it.longitude, DEFAULT_ZOOM)
            _currentMarker.value = it
            _mapUiState.value = MapUiState.BottomSheetInfoMode
        }
    }

    fun setPlaceSelectionMode() {
        _mapUiState.value = MapUiState.PlaceSelectMode
    }

    fun onZoomInClick() {
        _currentCameraPosition.value.let {
            setNewCameraLocation(it.latitude, it.longitude, it.zoom + 2f)
        }
    }

    fun onZoomOutClick() {
        _currentCameraPosition.value.let {
            setNewCameraLocation(it.latitude, it.longitude, it.zoom - 2f)
        }
    }

    fun resetMapBearing() {
        viewModelScope.launch {
            _newMapCameraPosition.emit(_currentCameraPosition.value.copy(bearing = 0f))
        }
    }

    override fun onCleared() {
        super.onCleared()
        _addNewMarkerState.value = UiState.InProgress
    }

    fun setNewMarkerInfo(latitude: Double, longitude: Double) {
        _fishActivity.value = null
        _currentWeather.value = null
        _currentMarkerAddressState.value = GeocoderResult.InProgress
        _currentMarkerRawDistance.value = null
        getPlaceNameForMarkerDetails(latitude, longitude)
        getFishActivity(latitude, longitude)
        getCurrentWeather(latitude, longitude)
    }

    private var placeTileNameJob: Job? = null
    fun cancelPlaceTileNameJob() {
        placeTileNameJob?.cancel()
    }

    fun getPlaceTileViewName() {
        placeTileNameJob = viewModelScope.launch(Dispatchers.Default) {
            _cameraMoveState.collectLatest {
                when (it) {
                    CameraMoveState.MoveStart -> {
                        _placeTileViewNameState.value = _placeTileViewNameState.value.copy(
                            geocoderResult = GeocoderResult.InProgress,
                            pointerState = PointerState.ShowMarker
                        )
                    }
                    CameraMoveState.MoveFinish -> {
                        delay(1200)
                        getPlaceNameUseCase.invoke(
                            currentCameraPosition.value.latitude,
                            currentCameraPosition.value.longitude,
                        ).collect { result ->
                            _placeTileViewNameState.value =
                                _placeTileViewNameState.value.copy(
                                    geocoderResult = result,
                                    pointerState = PointerState.HideMarker
                                )
                        }
                    }
                }
            }
        }
    }

    private fun getPlaceNameForMarkerDetails(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.Default) {
            getPlaceNameUseCase.invoke(latitude, longitude).collect { result ->
                _currentMarkerAddressState.value = result
            }
        }
    }

    fun onCameraMove(latitude: Double, longitude: Double, zoom: Float, bearing: Float) {
        viewModelScope.launch {
            mapBearing.value = bearing
            _currentCameraPosition.value = MapCameraState(latitude, longitude, zoom, bearing)
        }
    }

    fun getLastLocation() {
        if (!initialPlaceSelected.value) {
            currentMarker.value?.let {
                _firstCameraPosition.value =
                    _currentCameraPosition.value.copy(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        zoom = DEFAULT_ZOOM
                    )
            } ?: lastMapCameraPosition.value?.let {
                _firstCameraPosition.value = it
            } ?: getFirstLaunchLocation()
        }
    }

    private fun getFirstLaunchLocation() {
        viewModelScope.launch {
            if (currentMarker.value == null) {
                val fromBd = userPreferences.getLastMapCameraLocation.first()
                if (!initialPlaceSelected.value) {
                    _firstCameraPosition.emit(fromBd)
                }
            }
        }
    }

    fun resetAddNewMarkerState() {
        _addNewMarkerState.value = null
    }

    private companion object {
        const val CURRENT_LOCATION_TIMEOUT_MS = 10_000L
    }
}
