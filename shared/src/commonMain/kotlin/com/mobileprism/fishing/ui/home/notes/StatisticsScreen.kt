package com.mobileprism.fishing.ui.home.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Phishing
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.SetMeal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mobileprism.fishing.domain.entity.statistics.CatchStatistics
import com.mobileprism.fishing.ui.components.state.EmptyState
import com.mobileprism.fishing.ui.components.state.ErrorStateGeneric
import com.mobileprism.fishing.ui.components.state.ScreenStateContent
import com.mobileprism.fishing.ui.home.views.ChartCard
import com.mobileprism.fishing.ui.home.views.StatTile
import com.mobileprism.fishing.ui.home.views.StatTileSkeleton
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.ui.utils.monthShortLabelResource
import com.mobileprism.fishing.ui.viewmodels.StatisticsViewModel
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = koinViewModel(),
) {
    val state by viewModel.statisticsState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
    ) {
        ScreenStateContent(
            state = state,
            loading = { StatisticsLoadingContent() },
            error = { _ ->
                ErrorStateGeneric(
                    modifier = Modifier.fillMaxSize(),
                    onRetry = { viewModel.retry() },
                )
            },
            isEmpty = { stats -> stats.totalCatches == 0 },
            empty = {
                EmptyState(
                    illustration = painterResource(Res.drawable.ic_statistics),
                    title = stringResource(Res.string.add_places_catches_for_stats),
                    modifier = Modifier.fillMaxSize(),
                )
            },
            content = { stats ->
                StatisticsContent(stats)
            },
        )
    }
}

@Composable
private fun StatisticsLoadingContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        repeat(2) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    StatTileSkeleton(modifier = Modifier.weight(1f))
                    StatTileSkeleton(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatisticsContent(stats: CatchStatistics) {
    val monthLabelMap: Map<String, String> = stats.catchesByMonth.keys.associateWith { key ->
        val res = monthShortLabelResource(key)
        if (res != null) stringResource(res) else key
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Phishing,
                    title = stringResource(Res.string.total_catches),
                    value = stats.totalCatches.toString(),
                )
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Scale,
                    title = stringResource(Res.string.total_weight),
                    value = "${formatStatWeight(stats.totalWeight)} ${stringResource(Res.string.kg)}",
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.EmojiEvents,
                    title = stringResource(Res.string.heaviest_catch),
                    value = stats.heaviestCatch?.let {
                        "${formatStatWeight(it.fishWeight)} ${stringResource(Res.string.kg)}"
                    } ?: stringResource(Res.string.no_data),
                )
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.SetMeal,
                    title = stringResource(Res.string.species_count),
                    value = stats.totalSpecies.toString(),
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Scale,
                    title = stringResource(Res.string.average_weight),
                    value = "${formatStatWeight(stats.averageWeight)} ${stringResource(Res.string.kg)}",
                )
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.SetMeal,
                    title = stringResource(Res.string.most_caught),
                    value = stats.mostCaughtSpecies.ifBlank { stringResource(Res.string.no_data) },
                )
            }
        }

        if (stats.catchesByMonth.isNotEmpty()) {
            item {
                ChartCard(
                    title = stringResource(Res.string.catches_over_time),
                    data = stats.catchesByMonth,
                    formatLabel = { key -> monthLabelMap[key] ?: key },
                )
            }
        }
        if (stats.catchesBySpecies.isNotEmpty()) {
            item {
                val topSpecies = stats.catchesBySpecies.entries
                    .sortedByDescending { it.value }
                    .take(8)
                    .associate { it.key to it.value }
                ChartCard(
                    title = stringResource(Res.string.top_species),
                    data = topSpecies,
                )
            }
        }
        if (stats.catchesByWeather.isNotEmpty()) {
            item {
                ChartCard(
                    title = stringResource(Res.string.weather_conditions),
                    data = stats.catchesByWeather,
                )
            }
        }
        if (stats.catchesByTemperatureRange.isNotEmpty()) {
            item {
                ChartCard(
                    title = stringResource(Res.string.temperature_range),
                    data = stats.catchesByTemperatureRange,
                )
            }
        }
        if (stats.catchesByMoonPhase.isNotEmpty()) {
            item {
                ChartCard(
                    title = stringResource(Res.string.moon_phase_stats),
                    data = stats.catchesByMoonPhase,
                )
            }
        }

        item { Spacer(modifier = Modifier.height(Spacing.fabClearance)) }
    }
}

private fun formatStatWeight(value: Double): String {
    val rounded = (value * 100).toLong() / 100.0
    return if (rounded == rounded.toLong().toDouble()) "${rounded.toLong()}"
    else rounded.toString().trimEnd('0').trimEnd('.')
}
