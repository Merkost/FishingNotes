package com.mobileprism.fishing.domain.use_cases

import app.cash.turbine.test
import com.mobileprism.fishing.domain.entity.solunar.HourlyRating
import com.mobileprism.fishing.domain.entity.solunar.Solunar
import com.mobileprism.fishing.testutils.FakeSolunarRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class GetFishActivityUseCaseTest {

    private val solunarRepository = FakeSolunarRepository()

    private fun solunar(hourlyRating: HourlyRating = HourlyRating()) = Solunar(
        dayRating = 3,
        hourlyRating = hourlyRating,
        major1Start = "08:00",
        major1StartDec = 8.0,
        major1Stop = "10:00",
        major1StopDec = 10.0,
        major2Start = "20:00",
        major2StartDec = 20.0,
        major2Stop = "22:00",
        major2StopDec = 22.0,
        minor1Start = "02:00",
        minor1StartDec = 2.0,
        minor1Stop = "03:00",
        minor1StopDec = 3.0,
        minor2Start = "14:00",
        minor2StartDec = 14.0,
        minor2Stop = "15:00",
        minor2StopDec = 15.0,
        moonIllumination = 0.5,
        moonPhase = "Full Moon",
        moonRise = "18:00",
        moonRiseDec = 18.0,
        moonSet = "06:00",
        moonSetDec = 6.0,
        moonTransit = "00:00",
        moonTransitDec = 0.0,
        moonUnder = "12:00",
        moonUnderDec = 12.0,
        sunRise = "06:00",
        sunRiseDec = 6.0,
        sunSet = "18:00",
        sunSetDec = 18.0,
        sunTransit = "12:00",
        sunTransitDec = 12.0,
    )

    @Test
    fun returnsHourlyRatingForGivenHour() = runTest {
        val rating = HourlyRating(`10` = 4, `15` = 2)
        solunarRepository.result = Result.success(solunar(hourlyRating = rating))

        val useCase = GetFishActivityUseCase(solunarRepository)

        useCase(latitude = 55.0, longitude = 37.0, hour = 10).test {
            assertEquals(4, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun propagatesFailureByThrowing() = runTest {
        val error = RuntimeException("API error")
        solunarRepository.result = Result.failure(error)

        val useCase = GetFishActivityUseCase(solunarRepository)

        useCase(latitude = 55.0, longitude = 37.0, hour = 10).test {
            val thrown = awaitError()
            assertNotNull(thrown)
            assertEquals("API error", thrown.message)
        }
    }

    @Test
    fun dateFormattingIsYYYYMMDD() = runTest {
        solunarRepository.result = Result.success(solunar())

        val useCase = GetFishActivityUseCase(solunarRepository)

        useCase(latitude = 55.0, longitude = 37.0, hour = 12).test {
            awaitItem()
            awaitComplete()
        }

        val call = solunarRepository.calls.single()
        assertEquals(55.0, call[0])
        assertEquals(37.0, call[1])
        val dateStr = call[2] as String
        assertTrue(dateStr.length == 8 && dateStr.all { it.isDigit() })
    }
}
