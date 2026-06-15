package com.mobileprism.fishing.ui.home.map

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mobileprism.fishing.model.mappers.getWeatherIconByName
import com.mobileprism.fishing.ui.components.SkeletonBox
import com.mobileprism.fishing.ui.components.SkeletonLine
import com.mobileprism.fishing.ui.home.views.IconStatChip
import com.mobileprism.fishing.ui.theme.Elevation
import com.mobileprism.fishing.ui.theme.Spacing
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.add_new_catch
import fishing.shared.generated.resources.catches_plural
import fishing.shared.generated.resources.fish
import fishing.shared.generated.resources.ic_add_catch
import fishing.shared.generated.resources.ic_baseline_navigation_24
import fishing.shared.generated.resources.no_name_place
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val TemperatureAccent = Color(0xFFE65100)

@Composable
fun PlaceDetailsCardContent(
    title: String,
    address: String?,
    distance: String?,
    subtitleLoading: Boolean,
    temperatureCelsius: Int?,
    weatherIconName: String?,
    fishActivityPercent: Int?,
    windSpeedText: String?,
    windRotationDeg: Float,
    catchesCount: Int,
    showAddCatch: Boolean,
    onCardClick: () -> Unit,
    onAddCatchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statsLoading = fishActivityPercent == null && windSpeedText == null

    Card(
        shape = FishingTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.bottomSheet),
        colors = CardDefaults.cardColors(containerColor = FishingTheme.colorScheme.surface),
        modifier = modifier
            .zIndex(1.0f)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(Spacing.sm)
            .animateContentSize(animationSpec = tween(300, easing = LinearOutSlowInEasing)),
        onClick = onCardClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
        ) {
            PlaceDetailsHeader(
                title = title,
                address = address,
                distance = distance,
                subtitleLoading = subtitleLoading,
                temperatureCelsius = temperatureCelsius,
                weatherIconName = weatherIconName,
                showAddCatch = showAddCatch,
                onAddCatchClick = onAddCatchClick,
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            PlaceDetailsStatsRow(
                statsLoading = statsLoading,
                fishActivityPercent = fishActivityPercent,
                windSpeedText = windSpeedText,
                windRotationDeg = windRotationDeg,
                catchesCount = catchesCount,
            )
        }
    }
}

@Composable
private fun PlaceDetailsHeader(
    title: String,
    address: String?,
    distance: String?,
    subtitleLoading: Boolean,
    temperatureCelsius: Int?,
    weatherIconName: String?,
    showAddCatch: Boolean,
    onAddCatchClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title.ifEmpty { stringResource(Res.string.no_name_place) },
                style = FishingTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FishingTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitleLoading) {
                SkeletonLine(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(top = Spacing.xxs),
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = address.orEmpty(),
                        style = FishingTheme.typography.bodySmall,
                        color = FishingTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (!address.isNullOrBlank() && distance != null) {
                        Text(
                            text = " \u00B7 ",
                            style = FishingTheme.typography.bodySmall,
                            color = FishingTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (distance != null) {
                        Text(
                            text = distance,
                            style = FishingTheme.typography.bodySmall,
                            color = FishingTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = temperatureCelsius != null,
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(200)),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp),
            ) {
                if (weatherIconName != null) {
                    Icon(
                        painter = painterResource(getWeatherIconByName(weatherIconName)),
                        contentDescription = null,
                        tint = TemperatureAccent,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = "${temperatureCelsius ?: 0}\u00B0",
                    style = FishingTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TemperatureAccent,
                )
            }
        }

        if (temperatureCelsius == null) {
            SkeletonBox(
                width = 40.dp,
                height = 28.dp,
                modifier = Modifier.padding(horizontal = Spacing.md),
            )
        }

        if (showAddCatch) {
            IconButton(
                onClick = onAddCatchClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = FishingTheme.colorScheme.primary,
                    contentColor = FishingTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier.size(34.dp),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add_catch),
                    contentDescription = stringResource(Res.string.add_new_catch),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun PlaceDetailsStatsRow(
    statsLoading: Boolean,
    fishActivityPercent: Int?,
    windSpeedText: String?,
    windRotationDeg: Float,
    catchesCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (statsLoading) {
            repeat(3) {
                SkeletonBox(
                    height = 32.dp,
                    shape = FishingTheme.shapes.medium,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            IconStatChip(
                iconPainter = painterResource(Res.drawable.fish),
                label = fishActivityPercent?.let { "$it%" } ?: "\u2014",
                modifier = Modifier.weight(1f),
            )
            IconStatChip(
                iconPainter = painterResource(Res.drawable.ic_baseline_navigation_24),
                iconRotationDeg = windRotationDeg,
                label = windSpeedText ?: "\u2014",
                modifier = Modifier.weight(1f),
            )
            IconStatChip(
                iconPainter = painterResource(Res.drawable.ic_add_catch),
                label = stringResource(Res.string.catches_plural, catchesCount),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

