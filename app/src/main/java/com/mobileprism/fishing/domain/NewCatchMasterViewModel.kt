package com.mobileprism.fishing.domain

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.compose.ui.home.new_catch.NewCatchPlacesState
import com.mobileprism.fishing.compose.ui.home.new_catch.ReceivedPlaceState
import com.mobileprism.fishing.domain.viewstates.BaseViewState
import com.mobileprism.fishing.domain.viewstates.RetrofitWrapper
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.model.entity.common.Progress
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.entity.raw.RawUserCatch
import com.mobileprism.fishing.model.entity.weather.WeatherForecast
import com.mobileprism.fishing.model.repository.app.CatchesRepository
import com.mobileprism.fishing.model.repository.app.MarkersRepository
import com.mobileprism.fishing.model.repository.app.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.util.*

class NewCatchMasterViewModel(
    placeState: ReceivedPlaceState,
    private val markersRepository: MarkersRepository,
    private val catchesRepository: CatchesRepository,
    private val weatherRepository: WeatherRepository,
    private val weatherSettings: WeatherPreferences
) : ViewModel() {

    init {
        getAllUserMarkersList()
    }

    val isLocationLocked = MutableStateFlow(placeState is ReceivedPlaceState.Received)
    val isPlaceInputCorrect = MutableStateFlow(true)
    val isWeatherInputCorrect = mutableStateListOf<Boolean>()

    val currentPlace = MutableStateFlow(
        if (placeState is ReceivedPlaceState.Received) placeState.place else null
    )

    private val loadedWeather = MutableStateFlow(WeatherForecast())

    private val _uiState = MutableStateFlow<BaseViewState>(BaseViewState.Success(null))
    val uiState = _uiState.asStateFlow()

    private val _weatherState =
        MutableStateFlow<RetrofitWrapper<WeatherForecast>>(RetrofitWrapper.Loading())
    val weatherState = _weatherState.asStateFlow()

    val addPhotoState = mutableStateOf(false)

    val markersListState = MutableStateFlow<NewCatchPlacesState>(NewCatchPlacesState.NotReceived)
    val catchDate = MutableStateFlow(Date().time)
    val fishType = MutableStateFlow("")
    val fishAmount = MutableStateFlow(0)
    val fishWeight = MutableStateFlow(0.0)
    val description = MutableStateFlow("")
    val rod = MutableStateFlow("")
    val bait = MutableStateFlow("")
    val lure = MutableStateFlow("")
    val weatherPrimary = MutableStateFlow("")
    val weatherIconId = MutableStateFlow("01")
    val weatherTemperature = MutableStateFlow("0")
    val weatherPressure = MutableStateFlow("0")
    val weatherWindSpeed = MutableStateFlow("0")
    val weatherWindDeg = MutableStateFlow(0)
    val weatherMoonPhase = MutableStateFlow(0.0f)
    private val _photos = MutableStateFlow<List<Uri>>(listOf())
    val photos = _photos.asStateFlow()

    fun setSelectedPlace(place: UserMapMarker) {
        currentPlace.value = place
    }

    fun setPlaceInputError(isError: Boolean) {
        isPlaceInputCorrect.value = isError
    }

    fun setDate(date: Long) {
        catchDate.value = date
    }

    fun setFishType(fish: String) {
        fishType.value = fish
    }

    fun setFishAmount(amount: Int) {
        fishAmount.value = amount
    }

    fun setFishWeight(weight: Double) {
        fishWeight.value = weight
    }

    fun setNote(note: String) {
        description.value = note
    }

    fun setRod(rodValue: String) {
        rod.value = rodValue
    }

    fun setBait(baitValue: String) {
        bait.value = baitValue
    }

    fun setLure(lureValue: String) {
        lure.value = lureValue
    }

    fun setWeatherPrimary(weather: String) {
        weatherPrimary.value = weather
    }

    fun setWeatherTemperature(temperature: String) {
        weatherTemperature.value = temperature
    }

    fun setWeatherIconId(icon: String) {
        weatherIconId.value = icon
    }

    fun setWeatherPressure(pressure: String) {
        weatherPressure.value = pressure
    }

    fun setWeatherWindSpeed(windSpeed: String) {
        weatherWindSpeed.value = windSpeed
    }

    fun setWeatherWindDeg(windDeg: Int) {
        weatherWindDeg.value = windDeg
    }

    fun setWeatherMoonPhase(moonPhase: Float) {
        weatherMoonPhase.value = moonPhase
    }

    fun setWeatherIsError(isError: Boolean) {
        if (isError) {
            isWeatherInputCorrect.add(isError)
        } else {
            if (isWeatherInputCorrect.isNotEmpty()) {
                isWeatherInputCorrect.removeLast()
            }
        }
    }

    fun setPhotos(newPhotos: List<Uri>) {
        val result = mutableListOf<Uri>()
        result.addAll(newPhotos)
        _photos.value = result
    }

    fun loadWeather() {
        if (loadedWeather.value.hourly.first().date != catchDate.value) {
            getHistoricalWeather()
        } else {
            getWeatherForecast()
        }
    }

    private fun getWeatherForecast() {
        viewModelScope.launch(Dispatchers.IO) {
            currentPlace.value?.run {

            _weatherState.value = RetrofitWrapper.Loading()

                weatherRepository.getWeather(latitude, longitude).collect { result ->
                    when (result) {
                        is RetrofitWrapper.Success<WeatherForecast> -> {
                            loadedWeather.value = result.data
                            _weatherState.value = RetrofitWrapper.Success(result.data)
                            refreshWeatherState()
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

    private fun getHistoricalWeather() {
        viewModelScope.launch(Dispatchers.IO) {
            currentPlace.value?.run {

                _weatherState.value = RetrofitWrapper.Loading()

                weatherRepository
                    .getHistoricalWeather(latitude, longitude, (catchDate.value / 1000))
                    .collect { result ->
                        when (result) {
                            is RetrofitWrapper.Success<WeatherForecast> -> {
                                loadedWeather.value = result.data
                                _weatherState.value = RetrofitWrapper.Success(result.data)
                                refreshWeatherState()
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

    fun refreshWeatherState() {
        loadedWeather.value.run {

            viewModelScope.launch {
                weatherSettings.getPressureUnit.take(1).collectLatest {
                    setWeatherPressure(it.getPressureInt(hourly.first().pressure).toString())
                }
                weatherSettings.getWindSpeedUnit.take(1).collectLatest {
                    setWeatherWindSpeed(it.getWindSpeedInt(hourly.first().windSpeed.toDouble()))
                }
                weatherSettings.getTemperatureUnit.take(1).collectLatest {
                    setWeatherTemperature(it.getTemperature(hourly.first().temperature))
                }

                setWeatherPrimary(hourly.first().weather.first().description)
                setWeatherIconId(hourly.first().weather.first().icon)
                setWeatherWindDeg(hourly.first().windDeg)
                setWeatherMoonPhase(daily.first().moonPhase)
            }

        }
    }

    fun saveNewCatch() {
        _uiState.value = BaseViewState.Loading(0)
        val newCatch = RawUserCatch()

        viewModelScope.launch(Dispatchers.IO) {
            currentPlace.value?.let { userMapMarker ->
                catchesRepository.addNewCatch(userMapMarker.id, newCatch).collect { progress ->
                    when (progress) {
                        is Progress.Complete -> {
                            _uiState.value = BaseViewState.Success(progress)
                        }
                        is Progress.Loading -> {
                            _uiState.value = BaseViewState.Loading(progress.percents)
                        }
                        is Progress.Error -> {
                            _uiState.value = BaseViewState.Error(progress.error)
                        }
                    }
                }
            }
        }
    }

    private fun getAllUserMarkersList() {
        viewModelScope.launch(Dispatchers.IO) {
            markersRepository.getAllUserMarkersList().collect { markers ->
                markersListState.value =
                    NewCatchPlacesState.Received(markers as List<UserMapMarker>)
            }
        }
    }

}