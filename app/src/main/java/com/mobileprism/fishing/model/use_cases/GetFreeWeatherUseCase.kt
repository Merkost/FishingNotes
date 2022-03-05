package com.mobileprism.fishing.model.use_cases

import android.util.Log
import com.mobileprism.fishing.domain.viewstates.RetrofitWrapper
import com.mobileprism.fishing.model.entity.solunar.Solunar
import com.mobileprism.fishing.model.entity.weather.CurrentWeatherFree
import com.mobileprism.fishing.model.repository.app.FreeWeatherRepository
import com.mobileprism.fishing.model.repository.app.SolunarRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import java.util.*

class GetFreeWeatherUseCase(private val freeWeatherRepository: FreeWeatherRepository) {

    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
    ) = flow {
        val result = freeWeatherRepository.getCurrentWeatherFree(latitude, longitude).single()
        when (result) {
            is RetrofitWrapper.Success<CurrentWeatherFree> -> {
                emit(result.data)
            }
            is RetrofitWrapper.Error -> {
                Log.d("CURRENT WEATHER ERROR", result.errorType.error.toString())
            }
            else -> {}
        }
    }
}