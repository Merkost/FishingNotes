package com.mobileprism.fishing.domain

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.compose.ui.home.new_catch.NewCatchBuilder
import com.mobileprism.fishing.compose.ui.home.new_catch.NewCatchBuilderImpl
import com.mobileprism.fishing.compose.ui.home.new_catch.NewCatchPlacesState
import com.mobileprism.fishing.compose.ui.home.new_catch.ReceivedPlaceState
import com.mobileprism.fishing.domain.viewstates.BaseViewState
import com.mobileprism.fishing.domain.viewstates.RetrofitWrapper
import com.mobileprism.fishing.model.entity.common.Progress
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.entity.weather.WeatherForecast
import com.mobileprism.fishing.model.repository.app.CatchesRepository
import com.mobileprism.fishing.model.repository.app.MarkersRepository
import com.mobileprism.fishing.model.repository.app.WeatherRepository
import com.mobileprism.fishing.utils.time.toDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class NewCatchMasterViewModel(
    placeState: ReceivedPlaceState,
    private val markersRepository: MarkersRepository,
    private val catchesRepository: CatchesRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    init {
        getAllUserMarkersList()
    }

    private val builder: NewCatchBuilder = NewCatchBuilderImpl()

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
    val weatherTemperature = MutableStateFlow(0.0f)
    val weatherPressure = MutableStateFlow(0)
    val weatherWindSpeed = MutableStateFlow(0.0f)
    val weatherWindDeg = MutableStateFlow(0)
    val weatherMoonPhase = MutableStateFlow(0.0f)


    fun setSelectedPlace(place: UserMapMarker) {
        currentPlace.value = place
        builder.setPlaceId(place.id)
        builder.setPlaceTitle(place.title)
    }

    fun setPlaceInputError(isError: Boolean) {
        isPlaceInputCorrect.value = isError
    }

    fun setDate(date: Long) {
        catchDate.value = date
        builder.setDate(date)
    }

    fun setFishType(fish: String) {
        fishType.value = fish
        builder.setFishType(fish)
    }

    fun setFishAmount(amount: Int) {
        fishAmount.value = amount
        builder.setFishAmount(amount)
    }

    fun setFishWeight(weight: Double) {
        fishWeight.value = weight
        builder.setFishWeight(weight)
    }

    fun setNote(note: String) {
        description.value = note
        builder.setDescription(note)
    }

    fun setRod(rodValue: String) {
        rod.value = rodValue
        builder.setRodType(rodValue)
    }

    fun setBait(baitValue: String) {
        bait.value = baitValue
        builder.setBait(baitValue)
    }

    fun setLure(lureValue: String) {
        lure.value = lureValue
        builder.setLure(lureValue)
    }

    fun setWeatherPrimary(weather: String) {
        weatherPrimary.value = weather
        builder.setWeatherPrimary(weather)
    }

    fun setWeatherTemperature(temperature: Int) {
        weatherTemperature.value = temperature.toFloat()
        builder.setWeatherTemperature(temperature.toFloat())
    }

    fun setWeatherIconId(icon: String) {
        weatherIconId.value = icon
        builder.setWeatherIcon(icon)
    }

    fun setWeatherPressure(pressure: Int) {
        weatherPressure.value = pressure
        builder.setWeatherPressure(pressure)
    }

    fun setWeatherWindSpeed(windSpeed: Float) {
        weatherWindSpeed.value = windSpeed
        builder.setWeatherWindSpeed(windSpeed)
    }

    fun setWeatherWindDeg(windDeg: Int) {
        weatherWindDeg.value = windDeg
        builder.setWeatherWindDegrees(windDeg)
    }

    fun setWeatherMoonPhase(moonPhase: Float) {
        weatherMoonPhase.value = moonPhase
        builder.setWeatherMoonPhase(moonPhase)
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

    fun loadWeather() {
        if (catchDate.value.toDate() != Date().time.toDate()) {
            getHistoricalWeather()
        } else {
            getWeatherForecast()
        }
    }

    private fun getWeatherForecast() {
        viewModelScope.launch {
            currentPlace.value?.run {
                _weatherState.value = RetrofitWrapper.Loading()

                weatherRepository.getWeather(latitude, longitude).collect { result ->

                    when (result) {
                        is RetrofitWrapper.Success<WeatherForecast> -> {
                            loadedWeather.value = result.data
                            _weatherState.value = RetrofitWrapper.Success(result.data)
                            updateWeatherState()
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
        viewModelScope.launch {
            currentPlace.value?.run {
                _weatherState.value = RetrofitWrapper.Loading()
                weatherRepository
                    .getHistoricalWeather(latitude, longitude, (catchDate.value / 1000))
                    .collect { result ->
                        when (result) {
                            is RetrofitWrapper.Success<WeatherForecast> -> {
                                loadedWeather.value = result.data
                                _weatherState.value = RetrofitWrapper.Success(result.data)
                                updateWeatherState()
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

    private fun updateWeatherState() {
        loadedWeather.value.run {
            setWeatherPrimary(hourly.first().weather.first().description)
            setWeatherIconId(hourly.first().weather.first().icon)
            setWeatherTemperature(hourly.first().temperature.toInt())
            setWeatherPressure(hourly.first().pressure)
            setWeatherWindSpeed(hourly.first().windSpeed)
            setWeatherWindDeg(hourly.first().windDeg)
            setWeatherMoonPhase(daily.first().moonPhase)
        }

    }


    fun saveNewCatch() {
        _uiState.value = BaseViewState.Loading(0)
        val newCatch = builder.create()

        viewModelScope.launch {
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
        viewModelScope.launch {
            markersRepository.getAllUserMarkersList().collect { markers ->
                markersListState.value =
                    NewCatchPlacesState.Received(markers as List<UserMapMarker>)
            }
        }
    }

//    override fun onCleared() {
//        super.onCleared()
//        calendar.timeInMillis = Date().time
//    }

}