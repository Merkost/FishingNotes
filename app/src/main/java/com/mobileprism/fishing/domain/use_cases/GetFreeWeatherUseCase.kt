package com.mobileprism.fishing.domain.use_cases

import com.mobileprism.fishing.domain.repository.app.FreeWeatherRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single

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