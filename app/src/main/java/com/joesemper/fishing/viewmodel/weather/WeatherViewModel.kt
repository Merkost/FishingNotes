package com.joesemper.fishing.viewmodel.weather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.joesemper.fishing.model.weather.entity.WeatherState
import kotlinx.coroutines.*

class WeatherViewModel(private val interactor: IWeatherInteractor<WeatherState>) : ViewModel() {

    private val _mutableLiveData: MutableLiveData<WeatherState> = MutableLiveData()
    private val liveDataForViewToObserve: LiveData<WeatherState> = _mutableLiveData

    private val viewModelCoroutineScope = CoroutineScope(
            Dispatchers.Main
                    + SupervisorJob()
                    + CoroutineExceptionHandler { _, throwable ->
                handleError(throwable)
            })

    fun subscribe(): LiveData<WeatherState> {
        getData(-33.852f, 151.211f)
        return liveDataForViewToObserve
    }

    fun getData(lat: Float, lon: Float) {
        _mutableLiveData.value = WeatherState.Loading(null)
        cancelJob()
        viewModelCoroutineScope.launch { startInteractor(lat, lon) }
    }

    private suspend fun startInteractor(lat: Float, lon: Float) =
            withContext(Dispatchers.IO) {
                _mutableLiveData.postValue(interactor.getData(lat, lon))
            }

    override fun onCleared() {
        super.onCleared()
        cancelJob()
    }

    private fun handleError(error: Throwable) {
        _mutableLiveData.postValue(WeatherState.Error(error))
    }

    private fun cancelJob() {
        viewModelCoroutineScope.coroutineContext.cancelChildren()
    }


}