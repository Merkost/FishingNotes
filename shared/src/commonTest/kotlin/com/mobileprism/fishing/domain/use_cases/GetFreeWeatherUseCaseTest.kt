package com.mobileprism.fishing.domain.use_cases

import app.cash.turbine.test
import com.mobileprism.fishing.domain.entity.weather.CurrentWeatherFree
import com.mobileprism.fishing.testutils.FakeFreeWeatherRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest

class GetFreeWeatherUseCaseTest {

    private val freeWeatherRepository = FakeFreeWeatherRepository()

    private val sampleWeather = CurrentWeatherFree(
        cloud_pct = 25,
        feels_like = 18,
        humidity = 60,
        max_temp = 22,
        min_temp = 15,
        sunrise = 1700000000,
        sunset = 1700040000,
        temp = 20,
        wind_degrees = 180,
        wind_speed = 3.5,
    )

    @Test
    fun emitsWeatherOnSuccess() = runTest {
        freeWeatherRepository.result = Result.success(sampleWeather)

        val useCase = GetFreeWeatherUseCase(freeWeatherRepository)

        useCase(latitude = 55.0, longitude = 37.0).test {
            val result = awaitItem()
            assertEquals(sampleWeather, result)
            awaitComplete()
        }
    }

    @Test
    fun throwsOnFailure() = runTest {
        val error = RuntimeException("Weather API down")
        freeWeatherRepository.result = Result.failure(error)

        val useCase = GetFreeWeatherUseCase(freeWeatherRepository)

        useCase(latitude = 55.0, longitude = 37.0).test {
            val thrown = awaitError()
            assertNotNull(thrown)
            assertEquals("Weather API down", thrown.message)
        }
    }
}
