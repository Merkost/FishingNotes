package com.joesemper.fishing.presentation.weather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.joesemper.fishing.data.repository.weather.WeatherRepository
import kotlinx.coroutines.*

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    private var liveDataForViewToObserve: MutableLiveData<WeatherViewState> = MutableLiveData()

    private val viewModelCoroutineScope = CoroutineScope(
        Dispatchers.Main
                + SupervisorJob()
                + CoroutineExceptionHandler { _, throwable ->
            handleError(throwable)
        })

    init {
        getData(-33.852f, 151.211f)
    }

    fun subscribe(): LiveData<WeatherViewState> = liveDataForViewToObserve

    fun getData(lat: Float, lon: Float) {
        liveDataForViewToObserve.value = WeatherViewState.Loading(null)
        cancelJob()
        viewModelCoroutineScope.launch { loadForecast(lat, lon) }
    }

    private suspend fun loadForecast(lat: Float, lon: Float) =
        withContext(Dispatchers.IO) {
            liveDataForViewToObserve.postValue(
                WeatherViewState.Success(
                    repository.getData(
                        lat,
                        lon
                    )
                )
            )
        }

    override fun onCleared() {
        super.onCleared()
        cancelJob()
    }

    private fun handleError(error: Throwable) {
        liveDataForViewToObserve.postValue(WeatherViewState.Error(error))
    }

    private fun cancelJob() {
        viewModelCoroutineScope.coroutineContext.cancelChildren()
    }

}