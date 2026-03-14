package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.weather.WeatherForecast
import com.mobileprism.fishing.domain.entity.weather.WeatherSource
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.domain.repository.app.WeatherRepository
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.utils.isLocationsTooFar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val repository: MarkersRepository,
) : ViewModel() {

    private val _weatherState =
        MutableStateFlow<BaseViewState<WeatherForecast>>(BaseViewState.Loading())
    val weatherState = _weatherState.asStateFlow()

    private val _weatherSource = MutableStateFlow<WeatherSource?>(null)
    val weatherSource: StateFlow<WeatherSource?> = _weatherSource.asStateFlow()

    private val _selectedPlace = MutableStateFlow<UserMapMarker?>(null)
    val selectedPlace = _selectedPlace.asStateFlow()

    private val _markersList = MutableStateFlow<List<UserMapMarker>>(emptyList())
    val markersList: StateFlow<List<UserMapMarker>> = _markersList.asStateFlow()

    init {
        getAllMarkers()
    }

    private fun getAllMarkers() {
        if (_markersList.value.isEmpty()) {
            viewModelScope.launch(Dispatchers.Default) {
                repository.getAllUserMarkersList().collect {
                    _markersList.value = it.filterIsInstance<UserMapMarker>()
                }
            }
        }
    }

    private fun getWeather(latitude: Double, longitude: Double) {
        _weatherState.value = BaseViewState.Loading()
        viewModelScope.launch(Dispatchers.Default) {
            val result = weatherRepository.getWeatherWithMeta(latitude, longitude)
            result.fold(
                onSuccess = { weatherResult ->
                    _weatherSource.value = weatherResult.source
                    _weatherState.value = BaseViewState.Success(weatherResult.forecast)
                },
                onFailure = {
                    _weatherState.value = BaseViewState.Error(it)
                }
            )
        }
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun retry() {
        selectedPlace.value?.let { getWeather(it.latitude, it.longitude) }
    }

    fun refresh() {
        _isRefreshing.value = true
        selectedPlace.value?.let {
            viewModelScope.launch(Dispatchers.Default) {
                val result = weatherRepository.getWeatherWithMeta(it.latitude, it.longitude)
                result.fold(
                    onSuccess = { weatherResult ->
                        _weatherSource.value = weatherResult.source
                        _weatherState.value = BaseViewState.Success(weatherResult.forecast)
                    },
                    onFailure = { error ->
                        _weatherState.value = BaseViewState.Error(error)
                    }
                )
                _isRefreshing.value = false
            }
        } ?: run { _isRefreshing.value = false }
    }

    fun setInitialPlace(place: UserMapMarker?) {
        if (_selectedPlace.value == null && place != null) {
            setSelectedPlace(place)
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
        val currentList = _markersList.value
        val oldLocation = currentList.find { it.id == newLocation.id }

        val updatedList = currentList.toMutableList()
        if (oldLocation != null) {
            if (isLocationsTooFar(oldLocation, newLocation)) {
                updatedList.remove(oldLocation)
                updatedList.add(index = 0, element = newLocation)
            }
        } else {
            updatedList.add(index = 0, element = newLocation)
        }
        _markersList.value = updatedList

        if (selectedPlace.value == null) {
            setSelectedPlace(_markersList.value.first())
        }
    }

}
