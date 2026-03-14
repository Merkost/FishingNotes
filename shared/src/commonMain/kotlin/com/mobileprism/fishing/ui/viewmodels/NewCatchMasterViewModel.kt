package com.mobileprism.fishing.ui.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.weather.NewCatchWeatherData
import com.mobileprism.fishing.domain.use_cases.catches.GetNewCatchWeatherUseCase
import com.mobileprism.fishing.domain.use_cases.places.GetUserPlacesListUseCase
import com.mobileprism.fishing.domain.use_cases.catches.SaveNewCatchUseCase
import com.mobileprism.fishing.ui.home.new_catch.NewCatchPlacesState
import com.mobileprism.fishing.ui.home.new_catch.ReceivedPlaceState
import com.mobileprism.fishing.ui.viewstates.NewCatchViewState
import com.mobileprism.fishing.utils.calcMoonPhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.mobileprism.fishing.utils.ValidationUtils
import kotlin.time.Clock

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

    private val _fishAndWeightState = MutableStateFlow(FishAndWeightState())
    val fishAndWeightState = _fishAndWeightState.asStateFlow()

    private val _catchInfoState = MutableStateFlow(CatchInfoState())
    val catchInfoState = _catchInfoState.asStateFlow()

    private val _catchWeatherState = MutableStateFlow(CatchWeatherState())
    val catchWeatherState = _catchWeatherState.asStateFlow()

    private val _uiState = MutableStateFlow<NewCatchViewState>(NewCatchViewState.Editing)
    val uiState = _uiState.asStateFlow()

    private val _photos = MutableStateFlow<List<String>>(listOf())
    val photos = _photos.asStateFlow()

    private val _uploadProgress = MutableStateFlow<PhotoUploadProgress?>(null)
    val uploadProgress = _uploadProgress.asStateFlow()

    private val _skipAvailable: MutableStateFlow<Boolean> =
        MutableStateFlow(placeAndTimeState.value.place != null && fishAndWeightState.value.fish.isNotBlank())
    val skipAvailable = _skipAvailable.asStateFlow()

    fun setSelectedPlace(place: UserMapMarker) {
        _placeAndTimeState.value = _placeAndTimeState.value.copy(place = place)
        _catchWeatherState.value = CatchWeatherState(isDownloadAvailable = true)
    }

    fun setPlaceInputError(isError: Boolean) {
        _placeAndTimeState.value = _placeAndTimeState.value.copy(isInputCorrect = isError)
    }

    fun setDate(date: Long) {
        val clampedDate = ValidationUtils.clampDate(date)
        _placeAndTimeState.value = _placeAndTimeState.value.copy(date = clampedDate)
        _catchWeatherState.value = CatchWeatherState(isDownloadAvailable = true)
    }

    fun setFishType(fish: String) {
        _fishAndWeightState.value =
            _fishAndWeightState.value.copy(fish = fish, isInputCorrect = fish.isNotBlank())
    }

    fun setFishAmount(amount: Int) {
        val clamped = amount.coerceIn(ValidationUtils.MIN_FISH_AMOUNT, ValidationUtils.MAX_FISH_AMOUNT)
        _fishAndWeightState.value = _fishAndWeightState.value.copy(fishAmount = clamped)
    }

    fun setFishWeight(weight: Double) {
        val clamped = weight.coerceIn(ValidationUtils.MIN_FISH_WEIGHT_KG, ValidationUtils.MAX_FISH_WEIGHT_KG)
        _fishAndWeightState.value = _fishAndWeightState.value.copy(fishWeight = clamped)
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

    fun addPhotos(newPhotos: List<String>) {
        _photos.value = photos.value.toMutableList().apply { addAll(newPhotos) }
    }

    fun deletePhoto(deletedPhoto: String) {
        _photos.value = photos.value.toMutableList().apply { remove(deletedPhoto) }
    }

    fun loadWeather() {
        placeAndTimeState.value.place?.let {
            _catchWeatherState.value = _catchWeatherState.value.copy(isLoading = true)

            viewModelScope.launch(Dispatchers.Default) {
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
            primary = weather.primary,
            icon = weather.icon,
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
        _uploadProgress.value = null

        viewModelScope.launch(Dispatchers.Default) {
            placeAndTimeState.value.place?.let {
                val newCatch = createNewCatchData()
                saveNewCatchUseCase(newCatch) { uploaded, total ->
                    _uploadProgress.value = PhotoUploadProgress(uploaded, total)
                }.collect { progress ->
                    progress.fold(
                        onSuccess = {
                            _uploadProgress.value = null
                            _uiState.value = NewCatchViewState.Complete
                        },
                        onFailure = {
                            _uploadProgress.value = null
                            _uiState.value = NewCatchViewState.Error(it)
                        }
                    )
                }
            }
        }
    }

    private fun getAllUserMarkersList() {
        viewModelScope.launch(Dispatchers.Default) {
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
        fishAndWeightState = fishAndWeightState.value,
        catchInfoState = catchInfoState.value,
        catchWeatherState = catchWeatherState.value,
        photos = photos.value
    )
}

@Immutable
data class CatchPlaceAndTimeState(
    val place: UserMapMarker? = null,
    val date: Long = Clock.System.now().toEpochMilliseconds(),
    val placesListState: NewCatchPlacesState = NewCatchPlacesState.NotReceived,
    val isLocationCocked: Boolean,
    val isInputCorrect: Boolean = (place != null),
)

@Immutable
data class FishAndWeightState(
    val fish: String = "",
    val fishAmount: Int = 0,
    val fishWeight: Double = 0.0,
    val isInputCorrect: Boolean = (fish != "")
)

@Immutable
data class CatchInfoState(
    val rod: String = "",
    val bait: String = "",
    val lure: String = "",
    val note: String = ""
)

@Immutable
data class CatchWeatherState(
    val primary: String = "",
    val icon: String = "01",
    val temperature: String = "0",
    val windSpeed: String = "0",
    val windDeg: Int = 0,
    val pressure: String = "0",
    val moonPhase: Float = calcMoonPhase(Clock.System.now().toEpochMilliseconds()),
    val isLoading: Boolean = false,
    val isDownloadAvailable: Boolean = true,
    val isInputCorrect: Boolean = (primary != ""),
    val isError: Boolean = true
)

data class NewUserCatchData(
    val placeAndTimeState: CatchPlaceAndTimeState,
    val fishAndWeightState: FishAndWeightState,
    val catchInfoState: CatchInfoState,
    val catchWeatherState: CatchWeatherState,
    val photos: List<String>
)

@Immutable
data class PhotoUploadProgress(
    val uploaded: Int,
    val total: Int
)