package com.joesemper.fishing.compose.viewmodels

import android.util.Log
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
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
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.model.repository.app.MarkersRepository
import com.joesemper.fishing.model.repository.app.SolunarRepository
import com.joesemper.fishing.model.repository.app.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class MapViewModel(
    private val repository: MarkersRepository,
    private val weatherRepository: WeatherRepository,
    private val solunarRepository: SolunarRepository,
) : ViewModel() {

    val showMarker: MutableState<Boolean> = mutableStateOf(false)
    private val viewStateFlow: MutableStateFlow<BaseViewState> =
        MutableStateFlow(BaseViewState.Loading(null))

    private var _mapMarkers: MutableStateFlow<MutableList<UserMapMarker>>
        = MutableStateFlow(mutableListOf())
    val mapMarkers: StateFlow<MutableList<UserMapMarker>>
        get() = _mapMarkers

    var mapUiState: MutableState<MapUiState> = mutableStateOf(MapUiState.NormalMode)

    @ExperimentalMaterialApi
    var sheetState: BottomSheetValue = BottomSheetValue.Collapsed

    private val _uiState = MutableStateFlow<UiState?>(null)
    val uiState: StateFlow<UiState?>
        get() = _uiState

    val firstLaunchLocation = mutableStateOf(true)

    val lastKnownLocation = mutableStateOf<LatLng?>(null)
    val lastMapCameraPosition = mutableStateOf<Pair<LatLng, Float>?>(null)

    var currentMarker: MutableState<UserMapMarker?> = mutableStateOf(null)

    val chosenPlace = mutableStateOf<String?>(null)

    val fishActivity: MutableState<Int?> = mutableStateOf(null)

    init {
        loadMarkers()
    }

    fun getAllMarkers(): StateFlow<List<UserMapMarker>> = _mapMarkers

    override fun onCleared() {
        super.onCleared()
        viewStateFlow.value = BaseViewState.Loading(null)
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

    fun getWeather(latitude: Double, longitude: Double) =
        weatherRepository.getWeather(latitude, longitude)

    fun getFishActivity(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            solunarRepository.getSolunar(latitude,longitude).collect { result ->
                when (result) {
                    is RetrofitWrapper.Success<Solunar> -> {
                        val solunar = result.data
                        val calendar = Calendar.getInstance()
                        val currentHour24 = calendar[Calendar.HOUR_OF_DAY]
                        fishActivity.value = solunar.hourlyRating.`17`
                        //_weatherState.value = RetrofitWrapper.Success(result.data)
                    }
                    is RetrofitWrapper.Loading -> {
                        //_weatherState.value = RetrofitWrapper.Loading()
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


}