package com.mobileprism.fishing.compose.viewmodels

import android.util.Log
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.mobileprism.fishing.compose.ui.home.UiState
import com.mobileprism.fishing.compose.ui.home.map.CameraMoveState
import com.mobileprism.fishing.compose.ui.home.map.MapTypes
import com.mobileprism.fishing.compose.ui.home.map.MapUiState
import com.mobileprism.fishing.compose.ui.home.map.PointerState
import com.mobileprism.fishing.domain.viewstates.BaseViewState
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class MapViewModel(
    private val repository: MarkersRepository,
    private val freeWeatherRepository: FreeWeatherRepository,
    private val solunarRepository: SolunarRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {


    val showMarker: MutableState<Boolean> = mutableStateOf(false)
    private val viewStateFlow: MutableStateFlow<BaseViewState> =
        MutableStateFlow(BaseViewState.Loading(null))

    private var _mapMarkers: MutableStateFlow<MutableList<UserMapMarker>> =
        MutableStateFlow(mutableListOf())
    val mapMarkers: StateFlow<MutableList<UserMapMarker>>
        get() = _mapMarkers

    var mapUiState: MutableState<MapUiState> = mutableStateOf(MapUiState.NormalMode)

    private val _cameraMoveState = MutableStateFlow<CameraMoveState>(CameraMoveState.MoveFinish)
    val cameraMoveState: StateFlow<CameraMoveState>
    get() = _cameraMoveState

    private val _uiState = MutableStateFlow<UiState?>(null)
    val uiState: StateFlow<UiState?>
        get() = _uiState

    val mapType = mutableStateOf(MapTypes.roadmap)
    val mapBearing = mutableStateOf(0f)

    val firstLaunchLocation = mutableStateOf(true)

    val lastKnownLocation = mutableStateOf<LatLng?>(null)
    val lastMapCameraPosition = mutableStateOf<Pair<LatLng, Float>?>(null)

    val currentCameraPosition = mutableStateOf(Pair(LatLng(0.0, 0.0), 0f))

    var currentMarker: MutableState<UserMapMarker?> = mutableStateOf(null)

    val chosenPlace = mutableStateOf<String?>(null)

    val fishActivity: MutableState<Int?> = mutableStateOf(null)
    val currentWeather: MutableState<CurrentWeatherFree?> = mutableStateOf(null)

    val windIconRotation: Float
        get() = currentWeather.value?.wind_degrees?.minus(mapBearing.value) ?: mapBearing.value

    init {
        loadMarkers()
    }

    fun getAllMarkers(): StateFlow<List<UserMapMarker>> = _mapMarkers

    override fun onCleared() {
        super.onCleared()
        viewStateFlow.value = BaseViewState.Loading(null)
    }

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
            solunarRepository.getSolunar(latitude, longitude).collect { result ->
                when (result) {
                    is RetrofitWrapper.Success<Solunar> -> {
                        val solunar = result.data
                        val calendar = Calendar.getInstance()
                        val currentHour24 = calendar[Calendar.HOUR_OF_DAY]
                        fishActivity.value = solunar.hourlyRating[currentHour24]
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
                        currentMarker.value = updatedMarker
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