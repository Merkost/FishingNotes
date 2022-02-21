package com.mobileprism.fishing.domain

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.viewstates.BaseViewState
import com.mobileprism.fishing.domain.viewstates.RetrofitWrapper
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.entity.weather.WeatherForecast
import com.mobileprism.fishing.model.repository.app.MarkersRepository
import com.mobileprism.fishing.model.repository.app.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val repository: MarkersRepository
) : ViewModel() {


    private val _weatherState = MutableStateFlow<BaseViewState<WeatherForecast>>(BaseViewState.Loading())
    val weatherState = _weatherState.asStateFlow()

    private val _weather = MutableStateFlow<WeatherForecast>(WeatherForecast())
    val weather = _weather.asStateFlow()

    private val _selectedPlace = MutableStateFlow<UserMapMarker?>(null)
    val selectedPlace = _selectedPlace.asStateFlow()

    val markersList = mutableStateListOf<UserMapMarker>()

    init {
        getAllMarkers()
    }

    private fun getAllMarkers() {
        if (markersList.isEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.getAllUserMarkersList().collect {
                    markersList.clear()
                    markersList.addAll(it as List<UserMapMarker>)
                }
            }
        }
    }

    fun getWeather(latitude: Double, longitude: Double) {
        _weatherState.value = BaseViewState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            weatherRepository.getWeather(latitude, longitude).collect { result ->
                when (result) {
                    is RetrofitWrapper.Success<WeatherForecast> -> {
                        //weather.value = result.data
                        _weatherState.value = BaseViewState.Success(result.data)
                        _weather.value = result.data
                    }
                    is RetrofitWrapper.Error -> {
                        _weatherState.value = BaseViewState.Error(result.errorType.error)
                    }
                }
            }
        }
    }

    fun setSelectedPlace(place: UserMapMarker?) {
        place?.let {
            _selectedPlace.value = it
        }

    }

}
