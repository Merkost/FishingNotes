package com.mobileprism.fishing.domain

import android.net.Uri
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
import com.mobileprism.fishing.utils.calcMoonPhase
import com.mobileprism.fishing.utils.getClosestHourIndex
import com.mobileprism.fishing.utils.isDateInList
import com.mobileprism.fishing.utils.isLocationsTooFar
import com.mobileprism.fishing.utils.time.hoursCount
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
    val isWeatherInputCorrect = MutableStateFlow(true)

    private val _currentPlace = MutableStateFlow(
        if (placeState is ReceivedPlaceState.Received) placeState.place else null
    )
    val currentPlace = _currentPlace.asStateFlow()

    private val _placeAndTimeState = MutableStateFlow(
        CatchPlaceAndTime(
            place = if (placeState is ReceivedPlaceState.Received) placeState.place else null,
            isLocationCocked = placeState is ReceivedPlaceState.Received
        )
    )
    val placeAndTimeState = _placeAndTimeState.asStateFlow()

    private val _fishAndWeightState = MutableStateFlow(FishAndWeight())
    val fishAndWeightSate = _fishAndWeightState.asStateFlow()

    private val _catchInfoState = MutableStateFlow(CatchInfo())
    val catchInfoState = _catchInfoState.asStateFlow()

    private val _catchWeatherState = MutableStateFlow(CatchWeather())
    val catchWeatherState = _catchWeatherState.asStateFlow()

    private val loadedWeather = MutableStateFlow(WeatherForecast())

    private val _uiState = MutableStateFlow<BaseViewState>(BaseViewState.Success(null))
    val uiState = _uiState.asStateFlow()

    private val _weatherState =
        MutableStateFlow<RetrofitWrapper<WeatherForecast>>(RetrofitWrapper.Success(WeatherForecast()))
    val weatherState = _weatherState.asStateFlow()

    private val _weatherDownloadIsAvailable = MutableStateFlow(true)
    val weatherDownloadIsAvailable = _weatherDownloadIsAvailable.asStateFlow()

    private val _markersListState =
        MutableStateFlow<NewCatchPlacesState>(NewCatchPlacesState.NotReceived)
    val markersListState = _markersListState.asStateFlow()

    private val _catchDate = MutableStateFlow(Date().time)
    val catchDate = _catchDate.asStateFlow()

    private val _fishType = MutableStateFlow("")
    val fishType = _fishType.asStateFlow()

    private val _fishAmount = MutableStateFlow(0)
    val fishAmount = _fishAmount.asStateFlow()

    private val _fishWeight = MutableStateFlow(0.0)
    val fishWeight = _fishWeight.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _rod = MutableStateFlow("")
    val rod = _rod.asStateFlow()

    private val _bait = MutableStateFlow("")
    val bait = _bait.asStateFlow()

    private val _lure = MutableStateFlow("")
    val lure = _lure.asStateFlow()

    private val _weatherPrimary = MutableStateFlow("")
    val weatherPrimary = _weatherPrimary.asStateFlow()

    private val _weatherIconId = MutableStateFlow("01")
    val weatherIconId = _weatherIconId.asStateFlow()

    private val _weatherTemperature = MutableStateFlow("0")
    val weatherTemperature = _weatherTemperature.asStateFlow()

    private val _weatherPressure = MutableStateFlow("0")
    val weatherPressure = _weatherPressure.asStateFlow()

    private val _weatherWindSpeed = MutableStateFlow("0")
    val weatherWindSpeed = _weatherWindSpeed.asStateFlow()

    private val _weatherWindDeg = MutableStateFlow(0)
    val weatherWindDeg = _weatherWindDeg.asStateFlow()

    private val _weatherMoonPhase = MutableStateFlow(calcMoonPhase(catchDate.value))
    val weatherMoonPhase = _weatherMoonPhase.asStateFlow()

    private val _photos = MutableStateFlow<List<Uri>>(listOf())
    val photos = _photos.asStateFlow()

    private val _skipAvaliable: MutableStateFlow<Boolean> =
        MutableStateFlow(currentPlace.value != null && fishType.value.isNotBlank())
    val skipAvaliable = _skipAvaliable.asStateFlow()

    fun setSelectedPlace(place: UserMapMarker) {
        _currentPlace.value = place
        _placeAndTimeState.value = _placeAndTimeState.value.copy(place = place)
        checkWeatherDownloadNeed()
    }

    fun setPlaceInputError(isError: Boolean) {
        isPlaceInputCorrect.value = isError
    }

    fun setDate(date: Long) {
        _catchDate.value = date
        _placeAndTimeState.value = _placeAndTimeState.value.copy(date = date)
        checkWeatherDownloadNeed()
    }

    fun setFishType(fish: String) {
        _fishAndWeightState.value = _fishAndWeightState.value.copy(fish = fish)
        _fishType.value = fish
    }

    fun setFishAmount(amount: Int) {
        _fishAndWeightState.value = _fishAndWeightState.value.copy(fishAmount = amount)
        _fishAmount.value = amount
    }

    fun setFishWeight(weight: Double) {
        _fishAndWeightState.value = _fishAndWeightState.value.copy(fishWeight = weight)
        _fishWeight.value = weight
    }

    fun setNote(note: String) {
        _catchInfoState.value = _catchInfoState.value.copy(note = note)
        _description.value = note
    }

    fun setRod(rodValue: String) {
        _catchInfoState.value = _catchInfoState.value.copy(rod = rodValue)
        _rod.value = rodValue
    }

    fun setBait(baitValue: String) {
        _catchInfoState.value = _catchInfoState.value.copy(bait = baitValue)
        _bait.value = baitValue
    }

    fun setLure(lureValue: String) {
        _catchInfoState.value = _catchInfoState.value.copy(lure = lureValue)
        _lure.value = lureValue
    }

    fun setWeatherPrimary(weather: String) {
        _catchWeatherState.value = _catchWeatherState.value.copy(primary = weather)
        _weatherPrimary.value = weather
    }

    fun setWeatherTemperature(temperature: String) {
        _catchWeatherState.value =
            _catchWeatherState.value.copy(temperature = temperature.toFloat())
        _weatherTemperature.value = temperature
    }

    fun setWeatherIconId(icon: String) {
        _catchWeatherState.value = _catchWeatherState.value.copy(icon = icon)
        _weatherIconId.value = icon
    }

    fun setWeatherPressure(pressure: String) {
        _catchWeatherState.value = _catchWeatherState.value.copy(pressure = pressure.toInt())
        _weatherPressure.value = pressure
    }

    fun setWeatherWindSpeed(windSpeed: String) {
        _catchWeatherState.value = _catchWeatherState.value.copy(windSpeed = windSpeed.toFloat())
        _weatherWindSpeed.value = windSpeed
    }

    fun setWeatherWindDeg(windDeg: Int) {
        _catchWeatherState.value = _catchWeatherState.value.copy(windDeg = windDeg)
        _weatherWindDeg.value = windDeg
    }

    private fun setWeatherMoonPhase(moonPhase: Float) {
        _weatherMoonPhase.value = moonPhase
    }

    fun setWeatherIsError(isError: Boolean) {
        _catchWeatherState.value = _catchWeatherState.value.copy(isError = isError)
        isWeatherInputCorrect.value = !isError
    }

    fun addPhotos(newPhotos: List<Uri>) {
        _photos.value = photos.value.toMutableList().apply { addAll(newPhotos) }
    }

    fun deletePhoto(deletedPhoto: Uri) {
        _photos.value = photos.value.toMutableList().apply { remove(deletedPhoto) }
    }

    fun loadWeather() {
        if (weatherDownloadIsAvailable.value) {
            if (Date().time.hoursCount() > catchDate.value.hoursCount()) {
                getHistoricalWeather()
            } else {
                getWeatherForecast()
            }
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
                val index = getClosestHourIndex(list = hourly, date = catchDate.value)

                weatherSettings.getPressureUnit.take(1).collectLatest {
                    setWeatherPressure(it.getPressureInt(hourly[index].pressure).toString())
                }
                weatherSettings.getWindSpeedUnit.take(1).collectLatest {
                    setWeatherWindSpeed(it.getWindSpeedInt(hourly[index].windSpeed.toDouble()))
                }
                weatherSettings.getTemperatureUnit.take(1).collectLatest {
                    setWeatherTemperature(it.getTemperature(hourly[index].temperature))
                }

                setWeatherPrimary(hourly[index].weather.first().description
                    .replaceFirstChar { it.uppercase() })
                setWeatherIconId(hourly[index].weather.first().icon)
                setWeatherWindDeg(hourly[index].windDeg)
                setWeatherMoonPhase(calcMoonPhase(catchDate.value))
                checkWeatherDownloadNeed()
            }
        }
    }

    fun saveNewCatch() {
        _uiState.value = BaseViewState.Loading(0)

        viewModelScope.launch(Dispatchers.IO) {
            currentPlace.value?.let { userMapMarker ->
                val newCatch = createRawUserCatch(userMapMarker)
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
                _markersListState.value =
                    NewCatchPlacesState.Received(markers as List<UserMapMarker>)
                _placeAndTimeState.value = _placeAndTimeState.value.copy(
                    placesListState = NewCatchPlacesState.Received(markers)
                )
                return@collect
            }
        }
    }

    private fun createRawUserCatch(marker: UserMapMarker) = RawUserCatch(
        fishType = fishType.value,
        description = description.value,
        date = catchDate.value,
        fishAmount = fishAmount.value,
        fishWeight = fishWeight.value,
        fishingRodType = rod.value,
        fishingBait = bait.value,
        fishingLure = lure.value,
        markerId = marker.id,
        placeTitle = marker.title,
        isPublic = false,
        photos = photos.value,
        weatherPrimary = weatherPrimary.value,
        weatherIcon = weatherIconId.value,
        weatherTemperature = weatherTemperature.value.toFloat(),
        weatherWindSpeed = weatherWindSpeed.value.toFloat(),
        weatherWindDeg = weatherWindDeg.value,
        weatherPressure = weatherPressure.value.toInt(),
        weatherMoonPhase = weatherMoonPhase.value
    )

    private fun checkWeatherDownloadNeed() {
        val loadedWeatherPlace = UserMapMarker(
            latitude = loadedWeather.value.latitude.toDouble(),
            longitude = loadedWeather.value.longitude.toDouble()
        )
        _weatherDownloadIsAvailable.value = currentPlace.value?.let { currentPlace ->
            isLocationsTooFar(currentPlace, loadedWeatherPlace)
                    || !isDateInList(loadedWeather.value.hourly, catchDate.value)
        } ?: false
    }
}

data class CatchPlaceAndTime(
    val place: UserMapMarker? = null,
    val date: Long = Date().time,
    val placesListState: NewCatchPlacesState = NewCatchPlacesState.NotReceived,
    val isLocationCocked: Boolean,
    val isError: Boolean = (place == null),
)

data class FishAndWeight(
    val fish: String = "",
    val fishAmount: Int = 0,
    val fishWeight: Double = 0.0,
    val isError: Boolean = (fish == "")
)

data class CatchInfo(
    val rod: String = "",
    val bait: String = "",
    val lure: String = "",
    val note: String = ""
)

data class CatchWeather(
    val primary: String = "",
    val icon: String = "01",
    val temperature: Float = 0.0f,
    val windSpeed: Float = 0.0f,
    val windDeg: Int = 0,
    val pressure: Int = 0,
    val moonPhase: Float = calcMoonPhase(Date().time),
    val isLoading: Boolean = false,
    val isDownloadAvailable: Boolean = true,
    val isError: Boolean = true
)