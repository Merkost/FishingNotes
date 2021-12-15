package com.joesemper.fishing.domain

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.raw.NewCatchWeather
import com.joesemper.fishing.model.entity.raw.RawUserCatch
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.model.mappers.getWeatherNameByIcon
import com.joesemper.fishing.model.repository.app.CatchesRepository
import com.joesemper.fishing.model.repository.app.MarkersRepository
import com.joesemper.fishing.model.repository.app.WeatherRepository
import com.joesemper.fishing.utils.time.toHours
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class NewCatchViewModel(
    private val markersRepo: MarkersRepository,
    private val catchesRepo: CatchesRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BaseViewState>(BaseViewState.Success(null))
    val uiState: StateFlow<BaseViewState>
        get() = _uiState

    val noErrors = mutableStateOf(true)

    val marker: MutableState<UserMapMarker?> = mutableStateOf(null)
    val weather: MutableStateFlow<WeatherForecast?> = MutableStateFlow(null)

    val fishType = mutableStateOf("")
    val description = mutableStateOf("")
    val fishAmount = mutableStateOf("1")
    val weight = mutableStateOf("0.1")
    val date = mutableStateOf(0L)
    val rod = mutableStateOf("")
    val bite = mutableStateOf("")
    val lure = mutableStateOf("")

    val weatherToSave = mutableStateOf(NewCatchWeather())
    val moonPhase = mutableStateOf(0.0f)

    val images = mutableStateListOf<Uri>()

    fun getWeather() {
        viewModelScope.launch {
            //weather.value?.let {
              //  weather.value = null
            //weather.value = it} ?:
            //TODO: Weather Loading State
            marker.value?.run {

                weatherRepository.getWeather(latitude, longitude).collect {
                    weather.value = it
                }
            }
        }
    }


    fun getHistoricalWeather() = runBlocking {
        marker.value?.run {
            weatherRepository.getHistoricalWeather(latitude, longitude, (date.value / 1000))
        }
    }

    fun addPhoto(uri: Uri) {
        images.add(uri)
    }

    fun deletePhoto(uri: Uri) {
        images.remove(uri)
    }

    fun getAllUserMarkersList() = markersRepo.getAllUserMarkersList() as Flow<List<UserMapMarker>>

    private fun saveNewCatch(newCatch: RawUserCatch) {
        _uiState.value = BaseViewState.Loading(0)
        viewModelScope.launch {
            marker.value?.let { userMapMarker ->
                catchesRepo.addNewCatch(userMapMarker.id, newCatch).collect { progress ->
                    when (progress) {
                        is Progress.Complete -> {
                            _uiState.value = BaseViewState.Success(progress)
                        }
                        is Progress.Loading -> {
                            _uiState.value = BaseViewState.Loading(progress.percents)
                        }
                        is Progress.Error -> {
                            _uiState.value =
                                BaseViewState.Error(progress.error)
                        }
                    }
                }
            }
        }
    }

    fun isInputCorrect(): Boolean {
        return fishType.value.isNotBlank() && !marker.value?.title.isNullOrEmpty() && noErrors.value
    }

    fun createNewUserCatch() {
        viewModelScope.launch {
            if (isInputCorrect()) {
                marker.value?.let { userMapMarker ->
                    val hour = date.value.toHours().toInt()
                    weather.value?.let { forecast ->
                        saveNewCatch(
                            RawUserCatch(
                                fishType = fishType.value,
                                description = description.value,
                                date = date.value.toLong(),
                                fishAmount = fishAmount.value.toInt(),
                                fishWeight = weight.value.toDouble(),
                                fishingRodType = rod.value,
                                fishingBait = bite.value,
                                fishingLure = lure.value,
                                markerId = userMapMarker.id,
                                placeTitle = userMapMarker.title,
                                isPublic = false,
                                photos = images,
                                weatherPrimary = weatherToSave.value.weatherDescription,
                                weatherIcon = weatherToSave.value.icon,
                                weatherTemperature = weatherToSave.value.temperatureInC.toFloat(),
                                weatherWindSpeed = weatherToSave.value.windInMs.toFloat(),
                                weatherWindDeg = forecast.hourly[hour].windDeg,
                                weatherPressure = weatherToSave.value.pressureInMmhg,
                                weatherMoonPhase = weatherToSave.value.moonPhase
                            )
                        )
                    }
                }
            }
        }

    }
}
