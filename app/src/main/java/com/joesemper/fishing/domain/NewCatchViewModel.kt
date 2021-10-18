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
import com.joesemper.fishing.model.entity.raw.RawUserCatch
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.model.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class NewCatchViewModel(
    private val repository: UserContentRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BaseViewState>(BaseViewState.Success(null))
    val uiState: StateFlow<BaseViewState>
        get() = _uiState

    val noErrors = mutableStateOf(true)

    val marker: MutableState<UserMapMarker> = mutableStateOf(UserMapMarker())
    val weather: MutableState<WeatherForecast?> = mutableStateOf(null)

    val fishType = mutableStateOf("")
    val description = mutableStateOf("")
    val fishAmount = mutableStateOf("0")
    val weight = mutableStateOf("0")
    val date = mutableStateOf(0L)
    val rod = mutableStateOf("")
    val bite = mutableStateOf("")
    val lure = mutableStateOf("")

    val images = mutableStateListOf<Uri>()

    fun getWeather() = runBlocking {
        marker.value.run {
            weatherRepository.getWeather(latitude, longitude)
        }
    }

    fun getHistoricalWeather() = runBlocking {
        marker.value.run {
            weatherRepository.getHistoricalWeather(latitude, longitude, (date.value / 1000))
        }
    }

    fun addPhoto(uri: Uri) {
        images.add(uri)
    }

    fun deletePhoto(uri: Uri) {
        images.remove(uri)
    }

    fun getAllUserMarkersList() = repository.getAllUserMarkersList() as Flow<List<UserMapMarker>>

    private fun addNewCatch(newCatch: RawUserCatch) {
        _uiState.value = BaseViewState.Loading(0)
        viewModelScope.launch {
            repository.addNewCatch(marker.value.id, newCatch).collect { progress ->
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

    fun isInputCorrect(): Boolean {
        return fishType.value.isNotBlank() && marker.value.title.isNotEmpty() && noErrors.value
    }

    fun createNewUserCatch(photos: List<File>): Boolean {
        if (isInputCorrect()) {
            addNewCatch(
                RawUserCatch(
                    fishType = fishType.value,
                    description = description.value,
                    date = date.value.toLong(),
                    fishAmount = fishAmount.value.toInt(),
                    fishWeight = weight.value.toDouble(),
                    fishingRodType = rod.value,
                    fishingBait = bite.value,
                    fishingLure = lure.value,
                    markerId = marker.value.id,
                    isPublic = false,
                    photos = photos
                )
            )
            return true
        } else return false
    }

}