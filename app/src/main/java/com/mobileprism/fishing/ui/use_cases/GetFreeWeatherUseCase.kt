package com.mobileprism.fishing.ui.use_cases

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
        freeWeatherRepository.getCurrentWeatherFree(latitude, longitude).single().fold(
            onSuccess = {
                emit(it)
            },
            onFailure = {
                // TODO: emit error
            }
        )
    }
}