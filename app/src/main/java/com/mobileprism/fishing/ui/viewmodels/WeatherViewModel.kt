package com.mobileprism.fishing.ui.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.weather.WeatherForecast
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.domain.repository.app.WeatherRepository
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.utils.isLocationsTooFar
import com.mobileprism.fishing.utils.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val repository: MarkersRepository,
    private val locationManager: LocationManager,
) : ViewModel() {

    val markersList = mutableStateListOf<UserMapMarker>()

    private val _weatherState =
        MutableStateFlow<BaseViewState<WeatherForecast>>(BaseViewState.Loading())
    val weatherState = _weatherState.asStateFlow()

    private val _selectedPlace = MutableStateFlow<UserMapMarker?>(null)
    val selectedPlace = _selectedPlace.asStateFlow()

    init {
        getAllMarkers()
    }

    private fun getAllMarkers() {
        if (markersList.isEmpty()) {
            viewModelScope.launch {
                repository.getAllUserMarkersList().collect {
                    markersList.clear()
                    markersList.addAll(it as List<UserMapMarker>)
                }
            }
        }
    }

    private fun getWeather(latitude: Double, longitude: Double) {
        _weatherState.value = BaseViewState.Loading()
        viewModelScope.launch {
            weatherRepository.getWeather(latitude, longitude).collect { result ->
                result.fold(
                    onSuccess = {
                        _weatherState.value = BaseViewState.Success(it)
                    },
                    onFailure = {
                        _weatherState.value = BaseViewState.Error(it)
                    }
                )
            }
        }
    }

    fun setSelectedPlace(place: UserMapMarker?) {
        place?.let {
            if (selectedPlace.value != place) {
                _selectedPlace.value = it
                getWeather(it.latitude, it.longitude)
            }
        }

    }

    fun locationGranted(newLocation: UserMapMarker) {
        val oldLocation = markersList.find { it.id == newLocation.id }

        if (oldLocation != null) {
            if (isLocationsTooFar(oldLocation, newLocation)) {
                markersList.remove(oldLocation)
                markersList.add(index = 0, element = newLocation)
            }
        } else {
            markersList.add(index = 0, element = newLocation)
        }

        if (selectedPlace.value == null) {
            setSelectedPlace(markersList.first())
        }
    }

}
