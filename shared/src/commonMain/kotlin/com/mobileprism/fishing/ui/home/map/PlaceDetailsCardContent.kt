package com.mobileprism.fishing.ui.home.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mobileprism.fishing.model.mappers.getWeatherIconByName
import com.mobileprism.fishing.ui.utils.placeholder
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
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .zIndex(1.0f)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp)
            .animateContentSize(animationSpec = tween(300, easing = LinearOutSlowInEasing)),
        onClick = onCardClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
            Spacer(modifier = Modifier.height(12.dp))
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitleLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp)
                        .padding(top = 3.dp)
                        .placeholder(
                            visible = true,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(4.dp),
                        ),
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = address.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (!address.isNullOrBlank() && distance != null) {
                        Text(
                            text = " \u00B7 ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (distance != null) {
                        Text(
                            text = distance,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TemperatureAccent,
                )
            }
        }

        if (temperatureCelsius == null) {
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 28.dp)
                    .padding(horizontal = 10.dp)
                    .placeholder(
                        visible = true,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(6.dp),
                    ),
            )
        }

        if (showAddCatch) {
            IconButton(
                onClick = onAddCatchClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (statsLoading) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .placeholder(
                            visible = true,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(12.dp),
                        ),
                )
            }
        } else {
            StatChip(
                iconPainter = painterResource(Res.drawable.fish),
                iconRotationDeg = 0f,
                label = fishActivityPercent?.let { "$it%" } ?: "\u2014",
                modifier = Modifier.weight(1f),
            )
            StatChip(
                iconPainter = painterResource(Res.drawable.ic_baseline_navigation_24),
                iconRotationDeg = windRotationDeg,
                label = windSpeedText ?: "\u2014",
                modifier = Modifier.weight(1f),
            )
            StatChip(
                iconPainter = painterResource(Res.drawable.ic_add_catch),
                iconRotationDeg = 0f,
                label = stringResource(Res.string.catches_plural, catchesCount),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatChip(
    iconPainter: androidx.compose.ui.graphics.painter.Painter,
    iconRotationDeg: Float,
    label: String,
    modifier: Modifier = Modifier,
) {
    val chipColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(chipColor)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = iconPainter,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier
                .size(16.dp)
                .rotate(iconRotationDeg),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
