package com.mobileprism.fishing.compose.bar_chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileprism.fishing.compose.bar_chart.BarChartUtils.axisAreas
import com.mobileprism.fishing.compose.bar_chart.BarChartUtils.barDrawableArea
import com.mobileprism.fishing.compose.bar_chart.BarChartUtils.forEachWithArea
import com.mobileprism.fishing.compose.bar_chart.BarChartUtils.toLegacyInt
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun BarChart(
    barChartData: BarChartData,
    modifier: Modifier = Modifier,
) {
    val transitionAnimation = remember(barChartData.bars) { Animatable(initialValue = 0f) }

    LaunchedEffect(barChartData.bars) {
        transitionAnimation.animateTo(1f, animationSpec = tween(durationMillis = 500))
    }

    val progress = transitionAnimation.value

    val textBounds = android.graphics.Rect()
    val labelTextSize: TextUnit = 12.sp
    val labelTextColor: Color = Color.Black
    val labelRatio: Int = 3

    Canvas(modifier = modifier
        .fillMaxSize()
        .drawBehind {
            drawIntoCanvas { canvas ->
                val (xAxisArea, yAxisArea) = axisAreas(
                    drawScope = this,
                    totalSize = size,
                )
                val barDrawableArea = barDrawableArea(xAxisArea)

                // Draw yAxis line.
                val lineThickness = 1.dp.toPx()
                val x = yAxisArea.right - (lineThickness / 2f)

                canvas.drawLine(
                    p1 = Offset(
                        x = x,
                        y = yAxisArea.top
                    ),
                    p2 = Offset(
                        x = x,
                        y = yAxisArea.bottom
                    ),
                    paint = Paint().apply {
                        isAntiAlias = true
                        color = Color.Black
                        style = PaintingStyle.Stroke
                        strokeWidth = lineThickness
                    }
                )

                // Draw xAxis line.
                val y = xAxisArea.top + (lineThickness / 2f)

                canvas.drawLine(
                    p1 = Offset(
                        x = xAxisArea.left,
                        y = y
                    ),
                    p2 = Offset(
                        x = xAxisArea.right,
                        y = y
                    ),
                    paint = Paint().apply {
                        isAntiAlias = true
                        color = Color.Black
                        style = PaintingStyle.Stroke
                        strokeWidth = lineThickness
                    }
                )

                // Draw each bar.
                barChartData.forEachWithArea(
                    barDrawableArea,
                    progress,
                ) { barArea, bar ->
                    canvas.drawRect(barArea, Paint().apply { color = bar.color })
                }
            }
        }
    ) {
        /**
         *  Typically we could draw everything here, but because of the lack of canvas.drawText
         *  APIs we have to use Android's `nativeCanvas` which seems to be drawn behind
         *  Compose's canvas.
         */
        drawIntoCanvas { canvas ->
            val (xAxisArea, yAxisArea) = axisAreas(
                drawScope = this,
                totalSize = size,
            )
            val barDrawableArea = barDrawableArea(xAxisArea)

            barChartData.forEachWithArea(
                barDrawableArea = barDrawableArea,
                progress,
            ) { barArea, bar ->
                val xCenter = barArea.left + (barArea.width / 2)

                val yCenter = barArea.bottom + (3f / 2f) * labelTextSize.toPx()

                val paint = android.graphics.Paint().apply {
                    this.textAlign = android.graphics.Paint.Align.CENTER
                    this.color = labelTextColor.toLegacyInt()
                    this.textSize = labelTextSize.toPx()
                }

                canvas.nativeCanvas.drawText(bar.label, xCenter, yCenter, paint)
            }

            val labelPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                color = labelTextColor.toLegacyInt()
                textSize = labelTextSize.toPx()
                textAlign = android.graphics.Paint.Align.RIGHT
            }

            val minLabelHeight = (labelTextSize.toPx() * labelRatio.toFloat())
            val totalHeight = yAxisArea.height
            val labelCount = max((yAxisArea.height / minLabelHeight).roundToInt(), 2)

            for (i in 0..labelCount) {
                val value =
                    barChartData.minYValue + (i * ((barChartData.maxYValue - barChartData.minYValue) / labelCount))

                val label = "%.0f".format(value)
                val x = yAxisArea.right - 1.dp.toPx() - (labelTextSize.toPx() / 2f)

                labelPaint.getTextBounds(label, 0, label.length, textBounds)

                val y =
                    yAxisArea.bottom - (i * (totalHeight / labelCount)) + (textBounds.height() / 2f)

                canvas.nativeCanvas.drawText(label, x, y, labelPaint)
            }
        }
    }
}