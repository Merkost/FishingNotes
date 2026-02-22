package com.mobileprism.fishing.domain.entity.statistics

import androidx.compose.runtime.Immutable
import com.mobileprism.fishing.domain.entity.content.UserCatch

@Immutable
data class CatchStatistics(
    val totalCatches: Int,
    val totalWeight: Double,
    val averageWeight: Double,
    val heaviestCatch: UserCatch?,
    val totalSpecies: Int,
    val mostCaughtSpecies: String,
    val catchesByMonth: Map<String, Int>,
    val catchesBySpecies: Map<String, Int>,
    val weightBySpecies: Map<String, Double>,
    val catchesByWeather: Map<String, Int>,
    val catchesByTemperatureRange: Map<String, Int>,
    val catchesByMoonPhase: Map<String, Int>,
)
