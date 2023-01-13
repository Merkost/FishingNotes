package com.mobileprism.fishing.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.BuildConfig
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.weather.NewCatchWeatherData
import com.mobileprism.fishing.domain.use_cases.catches.GetNewCatchWeatherUseCase
import com.mobileprism.fishing.domain.use_cases.places.GetUserPlacesListUseCase
import com.mobileprism.fishing.domain.use_cases.catches.SaveNewCatchUseCase
import com.mobileprism.fishing.ui.home.new_catch.NewCatchPlacesState
import com.mobileprism.fishing.ui.home.new_catch.ReceivedPlaceState
import com.mobileprism.fishing.model.entity.FishingWeather
import com.mobileprism.fishing.ui.utils.toDoubleExOrNull
import com.mobileprism.fishing.ui.viewstates.NewCatchViewState
import com.mobileprism.fishing.utils.calcMoonPhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

class NewCatchMasterViewModel(
    placeState: ReceivedPlaceState,
    private val getNewCatchWeatherUseCase: GetNewCatchWeatherUseCase,
    private val saveNewCatchUseCase: SaveNewCatchUseCase,
    private val getUserPlacesListUseCase: GetUserPlacesListUseCase
) : ViewModel() {

    init {
        getAllUserMarkersList()
    }

    private val _placeAndTimeState = MutableStateFlow(
        CatchPlaceAndTimeState(
            place = if (placeState is ReceivedPlaceState.Received) placeState.place else null,
            isLocationCocked = placeState is ReceivedPlaceState.Received
        )
    )
    val placeAndTimeState = _placeAndTimeState.asStateFlow()

    private val _fishAndWeightState = MutableStateFlow(FishAndWeightState(fish = if (BuildConfig.DEBUG) "Fish" else ""))
    val fishAndWeightSate = _fishAndWeightState.asStateFlow()

    private val _catchInfoState = MutableStateFlow(CatchInfoState())
    val catchInfoState = _catchInfoState.asStateFlow()

    private val _catchWeatherState = MutableStateFlow(CatchWeatherState())
    val catchWeatherState = _catchWeatherState.asStateFlow()

    private val _uiState = MutableStateFlow<NewCatchViewState>(NewCatchViewState.Editing)
    val uiState = _uiState.asStateFlow()

    private val _photos = MutableStateFlow<List<Uri>>(listOf())
    val photos = _photos.asStateFlow()

    private val _skipAvailable: MutableStateFlow<Boolean> =
        MutableStateFlow(placeAndTimeState.value.place != null && fishAndWeightSate.value.fish.isNotBlank())
    val skipAvailable = _skipAvailable.asStateFlow()

    fun setSelectedPlace(place: UserMapMarker) {
        _placeAndTimeState.value = _placeAndTimeState.value.copy(place = place)
        _catchWeatherState.value = CatchWeatherState(isDownloadAvailable = true)
    }

    fun setPlaceInputError(isError: Boolean) {
        _placeAndTimeState.value = _placeAndTimeState.value.copy(isInputCorrect = isError)
    }

    fun setDate(date: Long) {
        _placeAndTimeState.value = _placeAndTimeState.value.copy(date = date)
        _catchWeatherState.value = CatchWeatherState(isDownloadAvailable = true)
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

    fun setWeatherTemperature(temperature: String) {
        if (temperature.toIntOrNull() != null && temperature.length <= 3) {
            _catchWeatherState.update { it.copy(temperature = temperature) }
        }
    }

    fun setWeather(weather: FishingWeather) {
        _catchWeatherState.update { it.copy(weather = weather) }
    }

    fun setWeatherPressure(pressure: String) {
        if(pressure.toDoubleOrNull() != null && pressure.endsWith(".").not()) {
            _catchWeatherState.update { it.copy(pressure = pressure) }

        }
    }

    fun setWeatherWindSpeed(windSpeed: String) {
        if (windSpeed.toDoubleExOrNull() != null && windSpeed.length <= 4) {
            _catchWeatherState.update { it.copy(windSpeed = windSpeed) }
        }
    }

    fun setWeatherWindDeg(windDeg: Int) {
        _catchWeatherState.value = _catchWeatherState.value.copy(windDeg = windDeg)
    }

    private fun setWeatherMoonPhase(moonPhase: Float) {
        _catchWeatherState.value = _catchWeatherState.value.copy(moonPhase = moonPhase)
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
                getNewCatchWeatherUseCase(
                    placeAndTimeState.value.place,
                    placeAndTimeState.value.date
                ).collectLatest { result ->
                    result.fold(
                        onSuccess = { forecast ->
                            _catchWeatherState.value =
                                _catchWeatherState.value.copy(
                                    isLoading = false,
                                    isDownloadAvailable = false
                                )
                            refreshWeatherState(forecast)
                        },
                        onFailure = {
                            _catchWeatherState.value =
                                _catchWeatherState.value.copy(isLoading = false, isError = true)
                        },
                    )
                }
            }
        }
    }

    private fun refreshWeatherState(weather: NewCatchWeatherData) {
        _catchWeatherState.value = _catchWeatherState.value.copy(
            weather = weather.fishingWeather,
            temperature = weather.temperature,
            windSpeed = weather.windSpeed,
            windDeg = weather.windDeg,
            pressure = weather.pressure,
            moonPhase = weather.moonPhase,
            isLoading = false,
            isError = false
        )
    }

    fun saveNewCatch() {
        _uiState.value = NewCatchViewState.SavingNewCatch

        viewModelScope.launch(Dispatchers.IO) {
            placeAndTimeState.value.place?.let {
                val newCatch = createNewCatchData()
                saveNewCatchUseCase(newCatch).collect { progress ->
                    progress.fold(
                        onSuccess = {
                            _uiState.value = NewCatchViewState.Complete
                        },
                        onFailure = {
                            _uiState.value = NewCatchViewState.Error(it)
                        }
                    )
                }
            }
        }
    }

    private fun getAllUserMarkersList() {
        viewModelScope.launch(Dispatchers.IO) {
            getUserPlacesListUseCase().collect { markers ->
                _placeAndTimeState.value = _placeAndTimeState.value.copy(
                    placesListState = NewCatchPlacesState.Received(markers)
                )
                return@collect
            }
        }
    }

    private fun createNewCatchData() = NewUserCatchData(
        placeAndTimeState = placeAndTimeState.value,
        fishAndWeightState = fishAndWeightSate.value,
        catchInfoState = catchInfoState.value,
        catchWeatherState = catchWeatherState.value,
        photos = photos.value
    )
}

data class CatchPlaceAndTimeState(
    val place: UserMapMarker? = null,
    val date: Long = Date().time,
    val placesListState: NewCatchPlacesState = NewCatchPlacesState.NotReceived,
    val isLocationCocked: Boolean,
    val isInputCorrect: Boolean = (place != null),
)

data class FishAndWeightState(
    val fish: String = "",
    val fishAmount: Int = 0,
    val fishWeight: Double = 0.0,
    val isInputCorrect: Boolean = (fish != "")
)

data class CatchInfoState(
    val rod: String = "",
    val bait: String = "",
    val lure: String = "",
    val note: String = ""
)

data class CatchWeatherState(
    val weather: FishingWeather = FishingWeather.SUN,
    val temperature: String = "0",
    val windSpeed: String = "0",
    val windDeg: Int = 0,
    val pressure: String = "0",
    val moonPhase: Float = calcMoonPhase(Date().time),
    val isLoading: Boolean = false,
    val isDownloadAvailable: Boolean = true,
    val isError: Boolean = true
)

data class NewUserCatchData(
    val placeAndTimeState: CatchPlaceAndTimeState,
    val fishAndWeightState: FishAndWeightState,
    val catchInfoState: CatchInfoState,
    val catchWeatherState: CatchWeatherState,
    val photos: List<Uri>
)