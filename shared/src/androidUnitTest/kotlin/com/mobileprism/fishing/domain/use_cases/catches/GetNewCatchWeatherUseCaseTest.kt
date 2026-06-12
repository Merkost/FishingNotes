package com.mobileprism.fishing.domain.use_cases.catches

import app.cash.turbine.test
import com.mobileprism.fishing.domain.entity.weather.PressureValues
import com.mobileprism.fishing.domain.entity.weather.TemperatureValues
import com.mobileprism.fishing.domain.entity.weather.WindSpeedValues
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.testutils.FakeWeatherRepository
import com.mobileprism.fishing.testutils.userMapMarker
import com.mobileprism.fishing.testutils.weatherForecast
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class GetNewCatchWeatherUseCaseTest {

    private val weatherRepository = FakeWeatherRepository()
    private val weatherPreferences = mockk<WeatherPreferences>()

    private fun setupPreferences() {
        every { weatherPreferences.getTemperatureUnit } returns flowOf(TemperatureValues.C)
        every { weatherPreferences.getPressureUnit } returns flowOf(PressureValues.Hpa)
        every { weatherPreferences.getWindSpeedUnit } returns flowOf(WindSpeedValues.metersps)
    }

    private fun setupWeatherMocks() {
        val forecast = weatherForecast(latitude = 55.0, longitude = 37.0)
        weatherRepository.getWeatherResults.add(Result.success(forecast))
        weatherRepository.getHistoricalWeatherResults.add(Result.success(forecast))
    }

    @Test
    fun nullPlaceReturnsFailure() = runTest {
        setupPreferences()
        val useCase = GetNewCatchWeatherUseCase(weatherRepository, weatherPreferences)

        useCase(place = null, newCatchDate = 1700000000000L).test {
            val result = awaitItem()
            assertTrue(result.isFailure)
            awaitComplete()
        }
    }

    @Test
    fun validPlaceReturnsSuccess() = runTest {
        setupPreferences()
        val place = userMapMarker(latitude = 55.0, longitude = 37.0)
        val forecast = weatherForecast(latitude = 55.0, longitude = 37.0)
        val catchDate = forecast.hourly.first().date * 1000L

        setupWeatherMocks()

        val useCase = GetNewCatchWeatherUseCase(weatherRepository, weatherPreferences)

        useCase(place = place, newCatchDate = catchDate).test {
            val result = awaitItem()
            assertTrue(result.isSuccess)
            val data = result.getOrThrow()
            assertTrue(data.lat == 55.0)
            assertTrue(data.lng == 37.0)
            awaitComplete()
        }
    }

    @Test
    fun reusesCacheWhenLocationCloseAndDateMatches() = runTest {
        setupPreferences()
        val place = userMapMarker(latitude = 55.0, longitude = 37.0)
        val forecast = weatherForecast(latitude = 55.0, longitude = 37.0)
        val catchDate = forecast.hourly.first().date * 1000L

        setupWeatherMocks()

        val useCase = GetNewCatchWeatherUseCase(weatherRepository, weatherPreferences)

        // First call - downloads weather
        useCase(place = place, newCatchDate = catchDate).test {
            assertTrue(awaitItem().isSuccess)
            awaitComplete()
        }

        // Same close location, same date - should reuse cache
        val closePlace = userMapMarker(latitude = 55.01, longitude = 37.01)
        useCase(place = closePlace, newCatchDate = catchDate).test {
            assertTrue(awaitItem().isSuccess)
            awaitComplete()
        }

        // Weather should only be fetched once total (historical, since test dates are in the past)
        assertTrue(weatherRepository.getHistoricalWeatherCalls.size == 1)
        assertTrue(weatherRepository.getWeatherCalls.isEmpty())
    }

    @Test
    fun reDownloadsWhenLocationTooFar() = runTest {
        setupPreferences()
        val place = userMapMarker(latitude = 55.0, longitude = 37.0)
        val forecast = weatherForecast(latitude = 55.0, longitude = 37.0)
        val catchDate = forecast.hourly.first().date * 1000L

        val farForecast = weatherForecast(latitude = 56.0, longitude = 38.0)
        weatherRepository.getWeatherResults.add(Result.success(forecast))
        weatherRepository.getHistoricalWeatherResults.add(Result.success(forecast))
        weatherRepository.getHistoricalWeatherResults.add(Result.success(farForecast))

        val useCase = GetNewCatchWeatherUseCase(weatherRepository, weatherPreferences)

        // First call
        useCase(place = place, newCatchDate = catchDate).test {
            assertTrue(awaitItem().isSuccess)
            awaitComplete()
        }

        // Far away location (> 0.15 distance)
        val farPlace = userMapMarker(latitude = 56.0, longitude = 38.0)
        useCase(place = farPlace, newCatchDate = catchDate).test {
            assertTrue(awaitItem().isSuccess)
            awaitComplete()
        }

        // Should have been fetched at least twice (via getHistoricalWeather since dates are in the past)
        assertTrue(weatherRepository.getHistoricalWeatherCalls.size >= 2)
    }

    @Test
    fun reDownloadsWhenDateNotInCachedList() = runTest {
        setupPreferences()
        val place = userMapMarker(latitude = 55.0, longitude = 37.0)
        val forecast = weatherForecast(latitude = 55.0, longitude = 37.0, hourlyCount = 2)
        val catchDate = forecast.hourly.first().date * 1000L

        weatherRepository.getWeatherResults.add(Result.success(forecast))
        weatherRepository.getHistoricalWeatherResults.add(Result.success(forecast))

        val useCase = GetNewCatchWeatherUseCase(weatherRepository, weatherPreferences)

        // First call - past date, uses getHistoricalWeather
        useCase(place = place, newCatchDate = catchDate).test {
            assertTrue(awaitItem().isSuccess)
            awaitComplete()
        }

        // Different date not in the hourly list but still in the past
        val differentDate = 1600000000000L
        useCase(place = place, newCatchDate = differentDate).test {
            assertTrue(awaitItem().isSuccess)
            awaitComplete()
        }

        // Should have been fetched at least twice
        assertTrue(weatherRepository.getHistoricalWeatherCalls.size >= 2)
    }
}
