package com.joesemper.fishing.domain

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.domain.viewstates.RetrofitWrapper
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.model.repository.app.MarkersRepository
import com.joesemper.fishing.model.repository.app.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val repository: MarkersRepository
) : ViewModel() {

    private val _weatherState = MutableStateFlow<RetrofitWrapper<WeatherForecast>>(RetrofitWrapper.Loading())
    val weatherState: StateFlow<RetrofitWrapper<WeatherForecast>>
        get() = _weatherState

    val markersList = mutableStateOf<MutableList<UserMapMarker>>(mutableListOf())
    val currentWeather = mutableStateOf<WeatherForecast?>(null)

    init {
        getAllMarkers()
    }

    private fun getAllMarkers() {
        viewModelScope.launch {
            repository.getAllUserMarkersList().collect {
                markersList.value = it as MutableList<UserMapMarker>
            }
        }

    }

    fun getWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            weatherRepository.getWeather(latitude, longitude).collect { result ->
                when (result) {
                    is RetrofitWrapper.Success<WeatherForecast> -> {
                        //weather.value = result.data
                        _weatherState.value = RetrofitWrapper.Success(result.data)
                        currentWeather.value = result.data
                    }
                    is RetrofitWrapper.Loading -> {
                        _weatherState.value = RetrofitWrapper.Loading()
                    }
                    is RetrofitWrapper.Error -> {
                        _weatherState.value = RetrofitWrapper.Error(result.errorType)
                    }
                }

            }
        }
    }


}
