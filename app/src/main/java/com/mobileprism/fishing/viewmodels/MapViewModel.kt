package com.mobileprism.fishing.viewmodels

import android.location.Geocoder
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.raw.RawMapMarker
import com.mobileprism.fishing.domain.entity.weather.CurrentWeatherFree
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.domain.use_cases.*
import com.mobileprism.fishing.domain.use_cases.places.AddNewPlaceUseCase
import com.mobileprism.fishing.domain.use_cases.places.GetUserPlacesListUseCase
import com.mobileprism.fishing.domain.use_cases.places.GetUserPlacesUseCase
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.ui.home.map.*
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.utils.location.LocationManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MapViewModel(
    private val getUserPlacesUseCase: GetUserPlacesUseCase,
    private val getUserPlacesListUseCase: GetUserPlacesListUseCase,
    private val addNewPlaceUseCase: AddNewPlaceUseCase,
    private val getFreeWeatherUseCase: GetFreeWeatherUseCase,
    private val getFishActivityUseCase: GetFishActivityUseCase,
    private val getPlaceNameUseCase: GetPlaceNameUseCase,
    private val userPreferences: UserPreferences,
    private val locationManager: LocationManager,
) : ViewModel() {

    private var _mapMarkers: MutableStateFlow<MutableList<UserMapMarker>> =
        MutableStateFlow(mutableListOf())
    val mapMarkers: StateFlow<List<UserMapMarker>>
        get() = _mapMarkers

    init {

        loadUserMarkersList()
        //loadUserPlaces()
    }

    private val initialPlaceSelected = MutableStateFlow(false)

    private val _firstCameraPosition = MutableStateFlow<Triple<LatLng, Float, Float>?>(null)
    val firstCameraPosition = _firstCameraPosition.asStateFlow()

    private val _addNewMarkerState: MutableStateFlow<UiState?> = MutableStateFlow(null)
    val addNewMarkerState = _addNewMarkerState.asStateFlow()



    private val _mapUiState: MutableStateFlow<MapUiState> = MutableStateFlow(MapUiState.NormalMode)
    val mapUiState = _mapUiState.asStateFlow()

    private val _cameraMoveState = MutableStateFlow<CameraMoveState>(CameraMoveState.MoveFinish)

    private val _mapType = MutableStateFlow(MapTypes.roadmap)
    val mapType = _mapType.asStateFlow()
    fun onLayerSelected(layer: Int) {
        _mapType.value = layer
    }

    val mapBearing = MutableStateFlow(0f)

    private val _lastKnownLocation = MutableStateFlow<LatLng?>(null)
    val lastKnownLocation = _lastKnownLocation.asStateFlow()

    private val lastMapCameraPosition = mutableStateOf<Triple<LatLng, Float, Float>?>(null)

    /**
     * A Triple of LatLng, Zoom and Bearing
     */
    private val _newMapCameraPosition = MutableSharedFlow<Triple<LatLng, Float, Float>>()
    val newMapCameraPosition = _newMapCameraPosition.asSharedFlow()

    private val _currentCameraPosition = MutableStateFlow(Triple(LatLng(0.0, 0.0), 0f, 0f))
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

    val fishActivity: MutableState<Int?> = mutableStateOf(null)
    val currentWeather: MutableState<CurrentWeatherFree?> = mutableStateOf(null)

    val windIconRotation: Float
        get() = currentWeather.value?.wind_degrees?.minus(_currentCameraPosition.value.third)
            ?: _currentCameraPosition.value.third


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

    /*private fun loadUserPlaces() {
        viewModelScope.launch {
            getUserPlacesUseCase.invoke().collect {
                it.fold(
                    onAdded = { place ->
                        var currentList = mutableListOf<UserMapMarker>()
                        currentList = _mapMarkers.value
                        currentList.add(place as UserMapMarker)
                        _mapMarkers.emit(currentList)
                    },
                    onModified = { place ->
                        val oldOne = _mapMarkers.value.find { it.id == (place as UserMapMarker).id}
                        _mapMarkers.value.remove(oldOne)
                        _mapMarkers.value.add(place as UserMapMarker)
                    },
                    onDeleted = { place ->
                        val placeToDelete = place as UserMapMarker
                        if (placeToDelete == currentMarker.value) _currentMarker.value = null
                        _mapMarkers.value.remove(place as UserMapMarker)
                    }
                )
            }
        }
    }*/


    private var addNewMarkerJob: Job? = null
    fun cancelAddNewMarker() {
        addNewMarkerJob?.cancel()
        _addNewMarkerState.value = null
    }

    fun addNewMarker(newMarker: RawMapMarker) {
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

    /*fun updateCurrentPlace(markerToUpdate: UserMapMarker) {
        viewModelScope.launch {
            repository.getMapMarker(markerToUpdate.id).fold(
                onSuccess = { updatedMarker ->
                    updatedMarker.let {
                        currentMarker.value?.let {
                            _currentMarker.value = updatedMarker
                        }
                        _mapMarkers.value.apply {
                            remove(markerToUpdate)
                            add(updatedMarker)
                        }
                    }

                },
                onFailure = { }
            )
        }
    }*/

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
            viewModelScope.launch {
                _lastKnownLocation.value?.let {
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
        when {
            place == null -> initialPlaceSelected.value = false
            place.id == Constants.CURRENT_PLACE_ITEM_ID -> {
                initialPlaceSelected.value = true
                _firstCameraPosition.value =
                    currentCameraPosition.value.copy(place.latLng, second = DEFAULT_ZOOM)
            }
            else -> {
                initialPlaceSelected.value = true
                _mapUiState.value = MapUiState.BottomSheetInfoMode
                _firstCameraPosition.value =
                    currentCameraPosition.value.copy(place.latLng, second = DEFAULT_ZOOM)
            }
        }
        /*place?.let {

            initialPlaceSelected.value = true
            _firstCameraPosition.value =
                currentCameraPosition.value.copy(it.latLng, second = DEFAULT_ZOOM)
            //setNewCameraLocation(it.latLng)

        } ?: run { initialPlaceSelected.value = false }*/
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

    @OptIn(ExperimentalPermissionsApi::class)
    fun onMyLocationClick() {
        viewModelScope.launch {
            val result = locationManager.getCurrentLocationFlow().singleOrNull()
            when (result) {
                is LocationState.LocationGranted -> {
                    _lastKnownLocation.value = result.location
                    setNewCameraLocation(result.location)
                }
                else -> {
                    SnackbarManager.showMessage(R.string.cant_get_current_location)
                }
            }
        }
    }

    private fun setNewCameraLocation(position: LatLng, zoom: Float = DEFAULT_ZOOM) {
        viewModelScope.launch {
            _newMapCameraPosition.emit(_currentCameraPosition.value.copy(position, zoom))
        }
    }

    fun onMarkerClicked(marker: UserMapMarker?) {
        marker?.let {
            setNewCameraLocation(it.latLng, DEFAULT_ZOOM)
            _currentMarker.value = it
            _mapUiState.value = MapUiState.BottomSheetInfoMode
        }
    }

    fun setPlaceSelectionMode() {
        _mapUiState.value = MapUiState.PlaceSelectMode
    }

    fun onZoomInClick() {
        _currentCameraPosition.value.let {
            setNewCameraLocation(it.first, it.second + 2f)
        }
    }

    fun onZoomOutClick() {
        _currentCameraPosition.value.let {
            setNewCameraLocation(it.first, it.second - 2f)
        }
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
        _currentMarkerAddressState.value = GeocoderResult.InProgress
        _currentMarkerRawDistance.value = null
        getPlaceNameForMarkerDetails(latitude, longitude)
        //getPlaceRawDistance(latitude, longitude)
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
                            currentCameraPosition.value.first.latitude,
                            currentCameraPosition.value.first.longitude,
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


    fun onCameraMove(target: LatLng, zoom: Float, bearing: Float) {
        viewModelScope.launch {
            mapBearing.value = bearing
            _currentCameraPosition.value = Triple(target, zoom, bearing)
        }
    }

    fun getLastLocation() {
        if (!initialPlaceSelected.value) {
            currentMarker.value?.let {
                _firstCameraPosition.value =
                    currentCameraPosition.value.copy(it.latLng, DEFAULT_ZOOM)
            } ?: lastMapCameraPosition.value?.let {
                _firstCameraPosition.value = it
            } ?: getFirstLaunchLocation()
        }

    }

    private fun getFirstLaunchLocation() {
        viewModelScope.launch {
            if (currentMarker.value == null) {
                val fromBd = userPreferences.getLastMapCameraLocation.first()
                _firstCameraPosition.emit(fromBd)
            }
        }
    }

    fun resetAddNewMarkerState() {
        _addNewMarkerState.value = null
    }

}

