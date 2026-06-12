package com.mobileprism.fishing.domain.use_cases.catches

import app.cash.turbine.test
import com.mobileprism.fishing.testutils.FakeCatchesRepositoryRead
import com.mobileprism.fishing.testutils.userCatch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class GetCatchStatisticsUseCaseTest {

    @Test
    fun emptyListReturnsZeroStats() = runTest {
        val useCase = GetCatchStatisticsUseCase(FakeCatchesRepositoryRead(catches = emptyList()))

        useCase().test {
            val stats = awaitItem()
            assertEquals(0, stats.totalCatches)
            assertEquals(0.0, stats.totalWeight)
            assertEquals(0.0, stats.averageWeight)
            assertNull(stats.heaviestCatch)
            assertEquals(0, stats.totalSpecies)
            assertEquals("", stats.mostCaughtSpecies)
            assertTrue(stats.catchesByMonth.isEmpty())
            assertTrue(stats.catchesBySpecies.isEmpty())
            assertTrue(stats.weightBySpecies.isEmpty())
            assertTrue(stats.catchesByWeather.isEmpty())
            assertTrue(stats.catchesByTemperatureRange.isEmpty())
            assertTrue(stats.catchesByMoonPhase.isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun singleCatchReturnsCorrectTotals() = runTest {
        val catch = userCatch(fishType = "Bass", fishWeight = 3.0, weatherPrimary = "Clear")
        val useCase = GetCatchStatisticsUseCase(FakeCatchesRepositoryRead(catches = listOf(catch)))

        useCase().test {
            val stats = awaitItem()
            assertEquals(1, stats.totalCatches)
            assertEquals(3.0, stats.totalWeight)
            assertEquals(3.0, stats.averageWeight)
            assertEquals(catch, stats.heaviestCatch)
            assertEquals(1, stats.totalSpecies)
            assertEquals("Bass", stats.mostCaughtSpecies)
            awaitComplete()
        }
    }

    @Test
    fun multipleCatchesCorrectTotalsAndAverageWeight() = runTest {
        val catches = listOf(
            userCatch(id = "c1", fishType = "Bass", fishWeight = 2.0, weatherPrimary = "Clear"),
            userCatch(id = "c2", fishType = "Trout", fishWeight = 4.0, weatherPrimary = "Clear"),
            userCatch(id = "c3", fishType = "Bass", fishWeight = 3.0, weatherPrimary = "Clear"),
        )
        val useCase = GetCatchStatisticsUseCase(FakeCatchesRepositoryRead(catches = catches))

        useCase().test {
            val stats = awaitItem()
            assertEquals(3, stats.totalCatches)
            assertEquals(9.0, stats.totalWeight)
            assertEquals(3.0, stats.averageWeight)
            assertEquals(2, stats.totalSpecies)
            awaitComplete()
        }
    }

    @Test
    fun heaviestCatchIdentification() = runTest {
        val heavy = userCatch(id = "c2", fishWeight = 10.0, weatherPrimary = "Clear")
        val catches = listOf(
            userCatch(id = "c1", fishWeight = 2.0, weatherPrimary = "Clear"),
            heavy,
            userCatch(id = "c3", fishWeight = 5.0, weatherPrimary = "Clear"),
        )
        val useCase = GetCatchStatisticsUseCase(FakeCatchesRepositoryRead(catches = catches))

        useCase().test {
            val stats = awaitItem()
            assertEquals(heavy, stats.heaviestCatch)
            awaitComplete()
        }
    }

    @Test
    fun speciesGroupingSkipsBlankFishType() = runTest {
        val catches = listOf(
            userCatch(id = "c1", fishType = "Bass", fishWeight = 2.0, weatherPrimary = "Clear"),
            userCatch(id = "c2", fishType = "", fishWeight = 1.0, weatherPrimary = "Clear"),
            userCatch(id = "c3", fishType = "  ", fishWeight = 1.0, weatherPrimary = "Clear"),
            userCatch(id = "c4", fishType = "Trout", fishWeight = 3.0, weatherPrimary = "Clear"),
        )
        val useCase = GetCatchStatisticsUseCase(FakeCatchesRepositoryRead(catches = catches))

        useCase().test {
            val stats = awaitItem()
            assertEquals(2, stats.totalSpecies)
            assertTrue(stats.catchesBySpecies.containsKey("Bass"))
            assertTrue(stats.catchesBySpecies.containsKey("Trout"))
            awaitComplete()
        }
    }

    @Test
    fun temperatureRangeBelowZero() = runTest {
        val catches = listOf(
            userCatch(id = "c1", weatherTemperature = -5.0f, weatherPrimary = "Snow"),
        )
        val useCase = GetCatchStatisticsUseCase(FakeCatchesRepositoryRead(catches = catches))

        useCase().test {
            val stats = awaitItem()
            assertEquals(1, stats.catchesByTemperatureRange["< 0\u00B0C"])
            awaitComplete()
        }
    }

    @Test
    fun temperatureRangeZeroToTen() = runTest {
        val catches = listOf(
            userCatch(id = "c1", weatherTemperature = 5.0f, weatherPrimary = "Cloudy"),
        )
        val useCase = GetCatchStatisticsUseCase(FakeCatchesRepositoryRead(catches = catches))

        useCase().test {
            val stats = awaitItem()
            assertEquals(1, stats.catchesByTemperatureRange["0-10\u00B0C"])
            awaitComplete()
        }
    }

    @Test
    fun temperatureRangeTenToTwenty() = runTest {
        val catches = listOf(
            userCatch(id = "c1", weatherTemperature = 15.0f, weatherPrimary = "Clear"),
        )
        val useCase = GetCatchStatisticsUseCase(FakeCatchesRepositoryRead(catches = catches))

        useCase().test {
            val stats = awaitItem()
            assertEquals(1, stats.catchesByTemperatureRange["10-20\u00B0C"])
            awaitComplete()
        }
    }

    @Test
    fun temperatureRangeTwentyToThirty() = runTest {
        val catches = listOf(
            userCatch(id = "c1", weatherTemperature = 25.0f, weatherPrimary = "Clear"),
        )
        val useCase = GetCatchStatisticsUseCase(FakeCatchesRepositoryRead(catches = catches))

        useCase().test {
            val stats = awaitItem()
            assertEquals(1, stats.catchesByTemperatureRange["20-30\u00B0C"])
            awaitComplete()
        }
    }

    @Test
    fun temperatureRangeThirtyPlus() = runTest {
        val catches = listOf(
            userCatch(id = "c1", weatherTemperature = 35.0f, weatherPrimary = "Clear"),
        )
        val useCase = GetCatchStatisticsUseCase(FakeCatchesRepositoryRead(catches = catches))

        useCase().test {
            val stats = awaitItem()
            assertEquals(1, stats.catchesByTemperatureRange["30+\u00B0C"])
            awaitComplete()
        }
    }

    @Test
    fun moonPhaseNameBoundaries() = runTest {
        val catches = listOf(
            userCatch(id = "c1", weatherMoonPhase = 0.0f, weatherPrimary = "Clear"),
            userCatch(id = "c2", weatherMoonPhase = 0.2f, weatherPrimary = "Clear"),
            userCatch(id = "c3", weatherMoonPhase = 0.5f, weatherPrimary = "Clear"),
            userCatch(id = "c4", weatherMoonPhase = 0.7f, weatherPrimary = "Clear"),
            userCatch(id = "c5", weatherMoonPhase = 0.9f, weatherPrimary = "Clear"),
        )
        val useCase = GetCatchStatisticsUseCase(FakeCatchesRepositoryRead(catches = catches))

        useCase().test {
            val stats = awaitItem()
            // 0.0 -> New Moon (< 0.125), 0.9 -> New Moon (>= 0.875)
            assertEquals(2, stats.catchesByMoonPhase["New Moon"])
            // 0.2 -> Waxing (0.125..0.375)
            assertEquals(1, stats.catchesByMoonPhase["Waxing"])
            // 0.5 -> Full Moon (0.375..0.625)
            assertEquals(1, stats.catchesByMoonPhase["Full Moon"])
            // 0.7 -> Waning (0.625..0.875)
            assertEquals(1, stats.catchesByMoonPhase["Waning"])
            awaitComplete()
        }
    }

    @Test
    fun weatherGrouping() = runTest {
        val catches = listOf(
            userCatch(id = "c1", weatherPrimary = "Clear"),
            userCatch(id = "c2", weatherPrimary = "Clear"),
            userCatch(id = "c3", weatherPrimary = "Rain"),
        )
        val useCase = GetCatchStatisticsUseCase(FakeCatchesRepositoryRead(catches = catches))

        useCase().test {
            val stats = awaitItem()
            assertEquals(2, stats.catchesByWeather["Clear"])
            assertEquals(1, stats.catchesByWeather["Rain"])
            awaitComplete()
        }
    }
}
