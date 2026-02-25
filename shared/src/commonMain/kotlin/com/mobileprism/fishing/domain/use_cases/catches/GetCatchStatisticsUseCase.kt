package com.mobileprism.fishing.domain.use_cases.catches

import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.statistics.CatchStatistics
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryRead
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class GetCatchStatisticsUseCase(private val repository: CatchesRepositoryRead) {

    operator fun invoke(): Flow<CatchStatistics> {
        return repository.getAllUserCatchesList()
            .map { catches -> buildStatistics(catches) }
            .flowOn(Dispatchers.Default)
    }

    private fun buildStatistics(catches: List<UserCatch>): CatchStatistics {
        if (catches.isEmpty()) {
            return CatchStatistics(
                totalCatches = 0,
                totalWeight = 0.0,
                averageWeight = 0.0,
                heaviestCatch = null,
                totalSpecies = 0,
                mostCaughtSpecies = "",
                catchesByMonth = emptyMap(),
                catchesBySpecies = emptyMap(),
                weightBySpecies = emptyMap(),
                catchesByWeather = emptyMap(),
                catchesByTemperatureRange = emptyMap(),
                catchesByMoonPhase = emptyMap(),
            )
        }

        val totalCatches = catches.size
        val totalWeight = catches.sumOf { it.fishWeight }
        val averageWeight = if (totalCatches > 0) totalWeight / totalCatches else 0.0
        val heaviestCatch = catches.maxByOrNull { it.fishWeight }

        val catchesBySpecies = catches
            .filter { it.fishType.isNotBlank() }
            .groupingBy { it.fishType }
            .eachCount()

        val totalSpecies = catchesBySpecies.size
        val mostCaughtSpecies = catchesBySpecies.maxByOrNull { it.value }?.key ?: ""

        val weightBySpecies = catches
            .filter { it.fishType.isNotBlank() }
            .groupBy { it.fishType }
            .mapValues { (_, catchList) -> catchList.sumOf { it.fishWeight } }

        val catchesByMonth = catches
            .groupingBy {
                val localDate = Instant.fromEpochMilliseconds(it.date)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                "${localDate.year}-${localDate.monthNumber.toString().padStart(2, '0')}"
            }
            .eachCount()
            .toSortedMap()

        val catchesByWeather = catches
            .filter { it.weatherPrimary.isNotBlank() }
            .groupingBy { it.weatherPrimary }
            .eachCount()

        val catchesByTemperatureRange = catches
            .filter { it.weatherTemperature != 0.0f || it.weatherPrimary.isNotBlank() }
            .groupingBy { temperatureRange(it.weatherTemperature) }
            .eachCount()
            .toSortedMap(compareBy { temperatureRangeOrder(it) })

        val catchesByMoonPhase = catches
            .filter { it.weatherMoonPhase != 0.0f || it.weatherPrimary.isNotBlank() }
            .groupingBy { moonPhaseName(it.weatherMoonPhase) }
            .eachCount()

        return CatchStatistics(
            totalCatches = totalCatches,
            totalWeight = totalWeight,
            averageWeight = averageWeight,
            heaviestCatch = heaviestCatch,
            totalSpecies = totalSpecies,
            mostCaughtSpecies = mostCaughtSpecies,
            catchesByMonth = catchesByMonth,
            catchesBySpecies = catchesBySpecies,
            weightBySpecies = weightBySpecies,
            catchesByWeather = catchesByWeather,
            catchesByTemperatureRange = catchesByTemperatureRange,
            catchesByMoonPhase = catchesByMoonPhase,
        )
    }

    private fun temperatureRange(temp: Float): String {
        return when {
            temp < 0 -> "< 0\u00B0C"
            temp < 10 -> "0-10\u00B0C"
            temp < 20 -> "10-20\u00B0C"
            temp < 30 -> "20-30\u00B0C"
            else -> "30+\u00B0C"
        }
    }

    private fun temperatureRangeOrder(range: String): Int {
        return when (range) {
            "< 0\u00B0C" -> 0
            "0-10\u00B0C" -> 1
            "10-20\u00B0C" -> 2
            "20-30\u00B0C" -> 3
            "30+\u00B0C" -> 4
            else -> 5
        }
    }

    private fun moonPhaseName(phase: Float): String {
        return when {
            phase < 0.125 || phase >= 0.875 -> "New Moon"
            phase < 0.375 -> "Waxing"
            phase < 0.625 -> "Full Moon"
            else -> "Waning"
        }
    }
}
