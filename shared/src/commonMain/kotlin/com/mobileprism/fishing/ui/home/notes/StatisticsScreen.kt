package com.mobileprism.fishing.ui.home.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Phishing
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.SetMeal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.utils.placeholder
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.statistics.CatchStatistics
import com.mobileprism.fishing.ui.home.views.DefaultButtonOutlined
import com.mobileprism.fishing.ui.home.views.DefaultCard
import com.mobileprism.fishing.ui.home.views.ErrorView
import com.mobileprism.fishing.ui.home.views.NoContentView
import com.mobileprism.fishing.ui.viewmodels.StatisticsViewModel
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = koinViewModel()
) {
    val state by viewModel.statisticsState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() }
    ) {
        when (val currentState = state) {
            is BaseViewState.Loading -> {
                StatisticsLoadingContent()
            }

            is BaseViewState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 128.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ErrorView()
                    DefaultButtonOutlined(
                        text = stringResource(Res.string.retry),
                        onClick = { viewModel.retry() }
                    )
                }
            }

            is BaseViewState.Success -> {
                val stats = currentState.data
                if (stats.totalCatches == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 128.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        NoContentView(
                            text = stringResource(Res.string.add_places_catches_for_stats),
                            icon = painterResource(Res.drawable.ic_statistics)
                        )
                    }
                } else {
                    StatisticsContent(stats)
                }
            }
        }
    }
}

@Composable
private fun StatisticsLoadingContent() {
    val placeholderModifier = Modifier.placeholder(
        visible = true,
        color = MaterialTheme.colorScheme.outlineVariant,
        shape = CircleShape,
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(2) {
                    DefaultCard(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = placeholderModifier
                                .height(80.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(2) {
                    DefaultCard(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = placeholderModifier
                                .height(80.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
        items(3) {
            DefaultCard {
                Box(
                    modifier = placeholderModifier
                        .height(200.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StatisticsContent(stats: CatchStatistics) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Summary cards row 1
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Phishing,
                    title = stringResource(Res.string.total_catches),
                    value = stats.totalCatches.toString()
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Scale,
                    title = stringResource(Res.string.total_weight),
                    value = "${formatWeight(stats.totalWeight)} ${stringResource(Res.string.kg)}"
                )
            }
        }

        // Summary cards row 2
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.EmojiEvents,
                    title = stringResource(Res.string.heaviest_catch),
                    value = if (stats.heaviestCatch != null) {
                        "${formatWeight(stats.heaviestCatch.fishWeight)} ${stringResource(Res.string.kg)}"
                    } else {
                        stringResource(Res.string.no_data)
                    }
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.SetMeal,
                    title = stringResource(Res.string.species_count),
                    value = stats.totalSpecies.toString()
                )
            }
        }

        // Catches over time chart
        if (stats.catchesByMonth.isNotEmpty()) {
            item {
                ChartSection(title = stringResource(Res.string.catches_over_time)) {
                    BarChartView(
                        data = stats.catchesByMonth,
                        formatLabel = { key ->
                            // "2025-01" -> "Jan"
                            val parts = key.split("-")
                            if (parts.size == 2) {
                                val monthNames = arrayOf(
                                    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
                                )
                                val monthIndex = (parts[1].toIntOrNull() ?: 1) - 1
                                monthNames.getOrElse(monthIndex) { key }
                            } else key
                        }
                    )
                }
            }
        }

        // Top species chart
        if (stats.catchesBySpecies.isNotEmpty()) {
            item {
                ChartSection(title = stringResource(Res.string.top_species)) {
                    val topSpecies = stats.catchesBySpecies.entries
                        .sortedByDescending { it.value }
                        .take(8)
                        .associate { it.key to it.value }
                    BarChartView(data = topSpecies)
                }
            }
        }

        // Weather conditions chart
        if (stats.catchesByWeather.isNotEmpty()) {
            item {
                ChartSection(title = stringResource(Res.string.weather_conditions)) {
                    BarChartView(data = stats.catchesByWeather)
                }
            }
        }

        // Temperature range chart
        if (stats.catchesByTemperatureRange.isNotEmpty()) {
            item {
                ChartSection(title = stringResource(Res.string.temperature_range)) {
                    BarChartView(data = stats.catchesByTemperatureRange)
                }
            }
        }

        // Moon phase chart
        if (stats.catchesByMoonPhase.isNotEmpty()) {
            item {
                ChartSection(title = stringResource(Res.string.moon_phase_stats)) {
                    BarChartView(data = stats.catchesByMoonPhase)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String
) {
    DefaultCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ChartSection(
    title: String,
    content: @Composable () -> Unit
) {
    DefaultCard {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun BarChartView(
    data: Map<String, Int>,
    formatLabel: (String) -> String = { it }
) {
    if (data.isEmpty()) return

    val keys = data.keys.toList()
    val values = data.values.toList().map { it.toDouble() }

    val modelProducer = remember(data) { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnSeries {
                series(values)
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    keys.getOrElse(value.toInt()) { "" }.let(formatLabel)
                }
            ),
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
    )
}

private fun formatWeight(value: Double): String {
    val rounded = (value * 100).toLong() / 100.0
    return if (rounded == rounded.toLong().toDouble()) "${rounded.toLong()}"
    else rounded.toString().trimEnd('0').trimEnd('.')
}
