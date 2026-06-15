package com.mobileprism.fishing.ui.components

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.decrease
import fishing.shared.generated.resources.ic_baseline_minus
import fishing.shared.generated.resources.ic_baseline_plus
import fishing.shared.generated.resources.increase
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun StepperField(
    modifier: Modifier = Modifier,
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    suffix: String = "",
    range: IntRange = 0..9999,
) {
    Surface(
        modifier = modifier,
        color = FishingTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilledIconButton(
                onClick = { if (value > range.first) onValueChange(value - 1) },
                enabled = value > range.first,
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = FishingTheme.colorScheme.primary.copy(alpha = 0.12f),
                    contentColor = FishingTheme.colorScheme.primary
                )
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_baseline_minus),
                    contentDescription = stringResource(Res.string.decrease)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (suffix.isNotEmpty()) "$value $suffix" else value.toString(),
                    style = FishingTheme.typography.titleLarge,
                    color = FishingTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = FishingTheme.typography.bodySmall,
                    color = FishingTheme.colorScheme.onSurfaceVariant
                )
            }

            FilledIconButton(
                onClick = { if (value < range.last) onValueChange(value + 1) },
                enabled = value < range.last,
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = FishingTheme.colorScheme.primary.copy(alpha = 0.12f),
                    contentColor = FishingTheme.colorScheme.primary
                )
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_baseline_plus),
                    contentDescription = stringResource(Res.string.increase)
                )
            }
        }
    }
}

@Composable
fun StepperField(
    modifier: Modifier = Modifier,
    value: Double,
    onValueChange: (Double) -> Unit,
    label: String,
    suffix: String = "",
    step: Double = 0.1,
    range: ClosedFloatingPointRange<Double> = 0.0..500.0,
) {
    val factor = 1.0 / step
    fun roundStep(v: Double): Double = (v * factor).roundToInt() / factor

    Surface(
        modifier = modifier,
        color = FishingTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilledIconButton(
                onClick = {
                    val next = roundStep(value - step)
                    if (next >= range.start) onValueChange(next)
                },
                enabled = value > range.start,
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = FishingTheme.colorScheme.primary.copy(alpha = 0.12f),
                    contentColor = FishingTheme.colorScheme.primary
                )
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_baseline_minus),
                    contentDescription = stringResource(Res.string.decrease)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (suffix.isNotEmpty()) "$value $suffix" else value.toString(),
                    style = FishingTheme.typography.titleLarge,
                    color = FishingTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = FishingTheme.typography.bodySmall,
                    color = FishingTheme.colorScheme.onSurfaceVariant
                )
            }

            FilledIconButton(
                onClick = {
                    val next = roundStep(value + step)
                    if (next <= range.endInclusive) onValueChange(next)
                },
                enabled = value < range.endInclusive,
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = FishingTheme.colorScheme.primary.copy(alpha = 0.12f),
                    contentColor = FishingTheme.colorScheme.primary
                )
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_baseline_plus),
                    contentDescription = stringResource(Res.string.increase)
                )
            }
        }
    }
}
