package com.mobileprism.fishing.domain

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.viewstates.BaseViewState
import com.mobileprism.fishing.domain.viewstates.Resource
import com.mobileprism.fishing.model.entity.common.Progress
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.entity.weather.WeatherForecast
import com.mobileprism.fishing.model.use_cases.GetNewCatchWeatherUseCase
import com.mobileprism.fishing.model.use_cases.GetUserCatchesUseCase
import com.mobileprism.fishing.model.use_cases.SaveNewCatchUseCase
import com.mobileprism.fishing.ui.home.new_catch.NewCatchPlacesState
import com.mobileprism.fishing.ui.home.new_catch.ReceivedPlaceState
import com.mobileprism.fishing.utils.calcMoonPhase
import com.mobileprism.fishing.utils.isDateInList
import com.mobileprism.fishing.utils.isLocationsTooFar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

class NewCatchMasterViewModel(
    placeState: ReceivedPlaceState,
    private val getNewCatchWeatherUseCase: GetNewCatchWeatherUseCase,
    private val getUserCatchesUseCase: GetUserCatchesUseCase,
    private val saveNewCatchUseCase: SaveNewCatchUseCase
) : ViewModel() {

    init {
        getAllUserMarkersList()
    }

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

    private val _photos = MutableStateFlow<List<Uri>>(listOf())
    val photos = _photos.asStateFlow()

    private val _skipAvailable: MutableStateFlow<Boolean> =
        MutableStateFlow(placeAndTimeState.value.place != null && fishAndWeightSate.value.fish.isNotBlank())
    val skipAvailable = _skipAvailable.asStateFlow()

    fun setSelectedPlace(place: UserMapMarker) {
        _placeAndTimeState.value = _placeAndTimeState.value.copy(place = place)
        checkWeatherDownloadNeed()
    }

    fun setPlaceInputError(isError: Boolean) {
        _placeAndTimeState.value = _placeAndTimeState.value.copy(isInputCorrect = isError)
    }

    fun setDate(date: Long) {
        _placeAndTimeState.value = _placeAndTimeState.value.copy(date = date)
        checkWeatherDownloadNeed()
    }

    fun setFishType(fish: String) {
        _fishAndWeightState.value =
            _fishAndWeightState.value.copy(fish = fish, isInputCorrect = fish.isNotBlank())
    }

    fun setFishAmount(amount: Int) {
        _fishAndWeightState.value = _fishAndWeightState.value.copy(fishAmount = amount)
    }

    fun setFishWeight(weight: Double) {
        _fishAndWeightState.value = _fishAndWeightState.value.copy(fishWeight = weight)
    }

    fun setNote(note: String) {
        _catchInfoState.value = _catchInfoState.value.copy(note = note)
    }

    fun setRod(rodValue: String) {
        _catchInfoState.value = _catchInfoState.value.copy(rod = rodValue)
    }

    fun setBait(baitValue: String) {
        _catchInfoState.value = _catchInfoState.value.copy(bait = baitValue)
    }

    fun setLure(lureValue: String) {
        _catchInfoState.value = _catchInfoState.value.copy(lure = lureValue)
    }

    fun setWeatherPrimary(weather: String) {
        _catchWeatherState.value = _catchWeatherState.value.copy(primary = weather)
    }

    fun setWeatherTemperature(temperature: String) {
        _catchWeatherState.value =
            _catchWeatherState.value.copy(temperature = temperature)
    }

    fun setWeatherIconId(icon: String) {
        _catchWeatherState.value = _catchWeatherState.value.copy(icon = icon)
    }

    fun setWeatherPressure(pressure: String) {
        _catchWeatherState.value = _catchWeatherState.value.copy(pressure = pressure)
    }

    fun setWeatherWindSpeed(windSpeed: String) {
        _catchWeatherState.value = _catchWeatherState.value.copy(windSpeed = windSpeed)
    }

    fun setWeatherWindDeg(windDeg: Int) {
        _catchWeatherState.value = _catchWeatherState.value.copy(windDeg = windDeg)
    }

    private fun setWeatherMoonPhase(moonPhase: Float) {
        _catchWeatherState.value = _catchWeatherState.value.copy(moonPhase = moonPhase)
    }

    fun setWeatherIsError(isError: Boolean) {
        _catchWeatherState.value = _catchWeatherState.value.copy(isInputCorrect = !isError)
    }

    fun addPhotos(newPhotos: List<Uri>) {
        _photos.value = photos.value.toMutableList().apply { addAll(newPhotos) }
    }

    fun deletePhoto(deletedPhoto: Uri) {
        _photos.value = photos.value.toMutableList().apply { remove(deletedPhoto) }
    }

    fun loadWeather() {
        placeAndTimeState.value.place?.let {
            _catchWeatherState.value = _catchWeatherState.value.copy(isLoading = true)

            viewModelScope.launch(Dispatchers.IO) {
                getNewCatchWeatherUseCase.invoke(
                    placeAndTimeState.value.place,
                    placeAndTimeState.value.date
                )
                    .collectLatest { result ->
                        when (result) {
                            is Resource.Success -> {
                                loadedWeather.value = result.data!!
                                _catchWeatherState.value = _catchWeatherState.value.copy(
                                    isLoading = false,
                                    isError = false
                                )
                                refreshWeatherState()
                            }
                            is Resource.Error -> {
                                _catchWeatherState.value =
                                    _catchWeatherState.value.copy(isError = true)
                            }
                        }
                    }
            }
        }
    }

    fun refreshWeatherState() {

        loadedWeather.value.run {
//            viewModelScope.launch {
//                val index = getClosestHourIndex(list = hourly, date = placeAndTimeState.value.date)
//
//                weatherSettingsImpl.getPressureUnit.take(1).collectLatest {
//                    setWeatherPressure(it.getPressureInt(hourly[index].pressure).toString())
//                }
//                weatherSettingsImpl.getWindSpeedUnit.take(1).collectLatest {
//                    setWeatherWindSpeed(it.getWindSpeedInt(hourly[index].windSpeed.toDouble()))
//                }
//                weatherSettingsImpl.getTemperatureUnit.take(1).collectLatest {
//                    setWeatherTemperature(it.getTemperature(hourly[index].temperature))
//                }
//
//                setWeatherPrimary(hourly[index].weather.first().description
//                    .replaceFirstChar { it.uppercase() })
//                setWeatherIconId(hourly[index].weather.first().icon)
//                setWeatherWindDeg(hourly[index].windDeg)
//                setWeatherMoonPhase(calcMoonPhase(placeAndTimeState.value.date))
//                checkWeatherDownloadNeed()
//            }
        }
    }

    fun saveNewCatch() {
        _uiState.value = BaseViewState.Loading(0)

        viewModelScope.launch(Dispatchers.IO) {
            placeAndTimeState.value.place?.let {
                val newCatch = createNewCatchData()
                saveNewCatchUseCase(newCatch).collect { progress ->
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
            getUserCatchesUseCase().collect { markers ->
                _placeAndTimeState.value = _placeAndTimeState.value.copy(
                    placesListState = NewCatchPlacesState.Received(markers as List<UserMapMarker>)
                )
                return@collect
            }
        }
    }

    private fun createNewCatchData() = NewUserCatchData(
        placeAndTime = placeAndTimeState.value,
        fishAndWeight = fishAndWeightSate.value,
        catchInfo = catchInfoState.value,
        catchWeather = catchWeatherState.value,
        photos = photos.value
    )

    private fun checkWeatherDownloadNeed() {
        val lastLoadedWeatherPlace = UserMapMarker(
            latitude = loadedWeather.value.latitude.toDouble(),
            longitude = loadedWeather.value.longitude.toDouble()
        )
        _catchWeatherState.value =
            _catchWeatherState.value.copy(isDownloadAvailable = placeAndTimeState.value.place?.let { currentPlace ->
                isLocationsTooFar(currentPlace, lastLoadedWeatherPlace)
                        || !isDateInList(loadedWeather.value.hourly, placeAndTimeState.value.date)
            } ?: false)
    }
}

data class CatchPlaceAndTime(
    val place: UserMapMarker? = null,
    val date: Long = Date().time,
    val placesListState: NewCatchPlacesState = NewCatchPlacesState.NotReceived,
    val isLocationCocked: Boolean,
    val isInputCorrect: Boolean = (place != null),
)

data class FishAndWeight(
    val fish: String = "",
    val fishAmount: Int = 0,
    val fishWeight: Double = 0.0,
    val isInputCorrect: Boolean = (fish != "")
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
    val temperature: String = "0",
    val windSpeed: String = "0",
    val windDeg: Int = 0,
    val pressure: String = "0",
    val moonPhase: Float = calcMoonPhase(Date().time),
    val isLoading: Boolean = false,
    val isDownloadAvailable: Boolean = true,
    val isInputCorrect: Boolean = (primary != ""),
    val isError: Boolean = true
)

data class NewUserCatchData(
    val placeAndTime: CatchPlaceAndTime,
    val fishAndWeight: FishAndWeight,
    val catchInfo: CatchInfo,
    val catchWeather: CatchWeather,
    val photos: List<Uri>
)