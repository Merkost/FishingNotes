package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill

data class WeatherChartPoint(
    val label: String,
    val value: Double,
)

@Composable
fun WeatherTrendChart(
    points: List<WeatherChartPoint>,
    modifier: Modifier = Modifier,
    height: Dp = 160.dp,
    formatValue: (Double) -> String = { it.toString() },
) {
    if (points.isEmpty()) return

    val labels = points.map { it.label }
    val values = points.map { it.value }
    val brandColor = MaterialTheme.colorScheme.secondary

    val modelProducer = remember(points) { CartesianChartModelProducer() }
    LaunchedEffect(points) {
        modelProducer.runTransaction {
            lineModel { series(values) }
        }
    }

    val brandFill = LineCartesianLayer.LineFill.single(Fill(brandColor))
    val brandLine = remember(brandFill) {
        LineCartesianLayer.Line(fill = brandFill)
    }
    val lineProvider = LineCartesianLayer.LineProvider.series(brandLine)

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(lineProvider = lineProvider),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = CartesianValueFormatter { _, value, _ -> formatValue(value) },
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    labels.getOrElse(value.toInt()) { "" }
                },
            ),
        ),
        modelProducer = modelProducer,
        modifier = modifier.fillMaxWidth().height(height),
    )
}
