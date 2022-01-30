package com.mobileprism.fishing.compose.bar_chart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

class BarChartDataModel {
    private var colors = mutableListOf(
        Color(0XFFF44336),
        Color(0XFFE91E63),
        Color(0XFF9C27B0),
        Color(0XFF673AB7),
        Color(0XFF3F51B5),
        Color(0XFF03A9F4),
        Color(0XFF009688),
        Color(0XFFCDDC39),
        Color(0XFFFFC107),
        Color(0XFFFF5722),
        Color(0XFF795548),
        Color(0XFF9E9E9E),
        Color(0XFF607D8B)
    )

    var barChartData by mutableStateOf(
        BarChartData(
            bars = listOf(
                BarChartData.Bar(
                    label = "Jul",
                    value = randomValue(),
                    color = randomColor()
                ),
                BarChartData.Bar(
                    label = "Aug",
                    value = randomValue(),
                    color = randomColor()
                ),
                BarChartData.Bar(
                    label = "Sep",
                    value = randomValue(),
                    color = randomColor()
                ),
                BarChartData.Bar(
                    label = "Oct",
                    value = randomValue(),
                    color = randomColor()
                ),
                BarChartData.Bar(
                    label = "Nov",
                    value = randomValue(),
                    color = randomColor()
                ),
                BarChartData.Bar(
                    label = "Dec",
                    value = randomValue(),
                    color = randomColor()
                )

            )
        )
    )

    val bars: List<BarChartData.Bar>
        get() = barChartData.bars

    private fun randomValue(): Float = (10 * Math.random() + 1).toFloat()

    private fun randomColor(): Color {
        val randomIndex = (Math.random() * colors.size).toInt()
        val color = colors[randomIndex]
        colors.removeAt(randomIndex)

        return color
    }
}