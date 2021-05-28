package com.joesemper.fishing.viewmodel.weather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.joesemper.fishing.model.weather.datasource.WeatherRepository
import com.joesemper.fishing.model.weather.entity.WeatherState
import kotlinx.coroutines.*

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    private var liveDataForViewToObserve: MutableLiveData<WeatherState> = MutableLiveData()

    private val viewModelCoroutineScope = CoroutineScope(
        Dispatchers.Main
                + SupervisorJob()
                + CoroutineExceptionHandler { _, throwable ->
            handleError(throwable)
        })

    init {
        getData(-33.852f, 151.211f)
    }

    fun subscribe(): LiveData<WeatherState> = liveDataForViewToObserve

    fun getData(lat: Float, lon: Float) {
        liveDataForViewToObserve.value = WeatherState.Loading(null)
        cancelJob()
        viewModelCoroutineScope.launch { loadForecast(lat, lon) }
    }

    private suspend fun loadForecast(lat: Float, lon: Float) =
        withContext(Dispatchers.IO) {
            liveDataForViewToObserve.postValue(WeatherState.Success(repository.getData(lat, lon)))
        }

    override fun onCleared() {
        super.onCleared()
        cancelJob()
    }

    private fun handleError(error: Throwable) {
        liveDataForViewToObserve.postValue(WeatherState.Error(error))
    }

    private fun cancelJob() {
        viewModelCoroutineScope.coroutineContext.cancelChildren()
    }

}