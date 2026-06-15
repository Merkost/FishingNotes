package com.mobileprism.fishing.ui.home.views

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.theme.Spacing
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent

@Composable
fun ChartCard(
    title: String,
    data: Map<String, Int>,
    modifier: Modifier = Modifier,
    formatLabel: (String) -> String = { it },
    emptyLabel: String? = null,
) {
    AppCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.md)) {
            Text(
                text = title,
                style = FishingTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = FishingTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(Spacing.md))
            if (data.isEmpty()) {
                if (emptyLabel != null) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = emptyLabel,
                            style = FishingTheme.typography.bodyMedium,
                            color = FishingTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                BrandColumnChart(data = data, formatLabel = formatLabel)
            }
        }
    }
}

@Composable
private fun BrandColumnChart(
    data: Map<String, Int>,
    formatLabel: (String) -> String,
) {
    val keys = data.keys.toList()
    val values = data.values.toList().map { it.toDouble() }
    val brandColor = FishingTheme.colorScheme.primary

    val modelProducer = remember(data) { CartesianChartModelProducer() }
    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnSeries { series(values) }
        }
    }

    val brandFill = Fill(brandColor)
    val brandColumn = rememberLineComponent(fill = brandFill, thickness = 16.dp)
    val columnProvider = ColumnCartesianLayer.ColumnProvider.series(brandColumn)

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(columnProvider = columnProvider),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    keys.getOrElse(value.toInt()) { "" }.let(formatLabel)
                },
            ),
        ),
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxWidth().height(200.dp),
    )
}
