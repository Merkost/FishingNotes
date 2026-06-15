package com.mobileprism.fishing.ui.home.notes

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.SubcomposeAsyncImage
import com.mobileprism.fishing.ui.theme.Spacing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.ui.home.views.*
import com.mobileprism.fishing.ui.theme.IconSize
import com.mobileprism.fishing.ui.theme.LocalColors
import com.mobileprism.fishing.ui.utils.placeholder
import com.mobileprism.fishing.utils.time.toDateTextMonth
import com.mobileprism.fishing.utils.time.toTime
import org.koin.compose.koinInject

/**
 * @param[childModifier] This is a modifier which is used in all child views
 * in order to show placeholder loading
 */
@Composable
fun ItemUserPlace(
    modifier: Modifier = Modifier,
    childModifier: Modifier = Modifier,
    place: UserMapMarker,
    userPlaceClicked: (UserMapMarker) -> Unit,
    navigateToMap: () -> Unit,
) {

    AppCard(
        modifier = modifier.padding(bottom = 4.dp),
        onClick = { userPlaceClicked(place) },
        contentPadding = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(36.dp),
                painter = painterResource(Res.drawable.ic_baseline_location_on_24),
                contentDescription = stringResource(Res.string.place),
                tint = Color(place.markerColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 8.dp)
            ) {
                AppText(
                    modifier = childModifier,
                    text = place.title,
                    style = AppTextStyle.Title,
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppText(
                        modifier = childModifier,
                        text = place.dateOfCreation.toDateTextMonth(),
                        style = AppTextStyle.BodySmall,
                        color = FishingTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )

                    ItemCounter(
                        modifier = Modifier.padding(start = 8.dp),
                        count = place.catchesCount,
                        icon = Res.drawable.ic_fishing,
                    )
                }
            }

            AppIconButton(
                modifier = childModifier,
                icon = painterResource(Res.drawable.ic_place_on_map),
                contentDescription = stringResource(Res.string.show_on_map),
                tint = if (!place.visible) LocalColors.current.secondaryTextColor
                        else FishingTheme.colorScheme.onSurface,
                onClick = { navigateToMap() },
            )
        }
    }
}

@Composable
fun ItemDate(text: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .zIndex(1f)
    ) {
        Surface(
            modifier = Modifier.padding(top = Spacing.sm, bottom = Spacing.xs),
            shape = RoundedCornerShape(percent = 50),
            color = FishingTheme.colorScheme.surfaceContainerHigh,
        ) {
            AppText(
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
                text = text,
                style = AppTextStyle.BodySmall,
                color = FishingTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * @param[childModifier] This is a modifier which is used in all child views
 * in order to show placeholder loading
 */
@Composable
fun CatchItemView(
    modifier: Modifier = Modifier,
    childModifier: Modifier = Modifier,
    catch: UserCatch,
    showPlace: Boolean = true,
    onClick: (UserCatch) -> Unit,
) {
    val preferences: UserPreferences = koinInject()
    val is12hTimeFormat by preferences.use12hTimeFormat.collectAsState(initial = false)

    CatchItemContent(
        modifier = modifier,
        fishType = catch.fishType,
        fishWeight = catch.fishWeight,
        fishAmount = catch.fishAmount,
        placeTitle = catch.placeTitle,
        photoUrl = catch.downloadPhotoLinks.firstOrNull(),
        photoCount = catch.downloadPhotoLinks.size,
        timeText = catch.date.toTime(is12hTimeFormat),
        showPlace = showPlace,
        onClick = { onClick(catch) },
    )
}

@Composable
fun CatchItemContent(
    modifier: Modifier = Modifier,
    fishType: String,
    fishWeight: Double,
    fishAmount: Int,
    placeTitle: String,
    photoUrl: String?,
    photoCount: Int,
    timeText: String,
    showPlace: Boolean = true,
    onClick: () -> Unit,
) {
    AppCard(
        modifier = modifier.padding(bottom = Spacing.sm),
        onClick = onClick,
        contentPadding = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CatchThumbnail(photoUrl = photoUrl, photoCount = photoCount)

            Spacer(modifier = Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppText(
                        modifier = Modifier.weight(1f).padding(end = Spacing.sm),
                        text = fishType,
                        style = AppTextStyle.Title,
                        maxLines = 1,
                    )
                    AppText(
                        text = "$fishWeight ${stringResource(Res.string.kg)}",
                        style = AppTextStyle.Title,
                        color = FishingTheme.colorScheme.primary,
                        maxLines = 1,
                    )
                }

                if (fishAmount > 1) {
                    AppText(
                        modifier = Modifier.padding(top = Spacing.xxs),
                        text = "${stringResource(Res.string.amount)}: $fishAmount" +
                                " ${stringResource(Res.string.pc)}",
                        style = AppTextStyle.Caption,
                        color = FishingTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (showPlace) {
                        Icon(
                            modifier = Modifier.size(IconSize.sm),
                            painter = painterResource(Res.drawable.ic_baseline_location_on_24),
                            contentDescription = null,
                            tint = FishingTheme.colorScheme.onSurfaceVariant,
                        )
                        AppText(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = Spacing.xs, end = Spacing.sm),
                            text = placeTitle,
                            style = AppTextStyle.BodySmall,
                            color = FishingTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    AppText(
                        text = timeText,
                        style = AppTextStyle.BodySmall,
                        color = FishingTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun CatchThumbnail(
    photoUrl: String?,
    photoCount: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(FishingTheme.shapes.medium)
            .background(FishingTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        if (photoUrl != null) {
            SubcomposeAsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { CatchThumbnailFallback() },
                error = { CatchThumbnailFallback() },
            )
        } else {
            CatchThumbnailFallback()
        }

        if (photoCount > 1) {
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd).padding(Spacing.xxs),
                shape = FishingTheme.shapes.small,
                color = FishingTheme.colorScheme.scrim.copy(alpha = 0.6f),
            ) {
                AppText(
                    modifier = Modifier.padding(horizontal = Spacing.xs),
                    text = photoCount.toString(),
                    style = AppTextStyle.Caption,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun CatchThumbnailFallback() {
    Icon(
        modifier = Modifier.size(IconSize.lg),
        painter = painterResource(Res.drawable.ic_fish),
        contentDescription = null,
        tint = FishingTheme.colorScheme.onSurfaceVariant.copy(alpha = FishingTheme.emphasis.medium),
    )
}

@Composable
fun PlaceItemSkeleton(modifier: Modifier = Modifier) {
    val shimmerModifier = Modifier.placeholder(
        visible = true,
        color = FishingTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(8.dp),
    )

    AppCard(
        modifier = modifier.padding(bottom = 4.dp),
        onClick = {},
        contentPadding = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .placeholder(
                        visible = true,
                        color = FishingTheme.colorScheme.surfaceContainerHighest,
                        shape = CircleShape,
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(18.dp)
                        .then(shimmerModifier)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(14.dp)
                            .then(shimmerModifier)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(14.dp)
                            .then(shimmerModifier)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .placeholder(
                        visible = true,
                        color = FishingTheme.colorScheme.surfaceContainerHighest,
                        shape = CircleShape,
                    )
            )
        }
    }
}

@Composable
fun CatchItemSkeleton(modifier: Modifier = Modifier) {
    val shimmerModifier = Modifier.placeholder(
        visible = true,
        color = FishingTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(8.dp),
    )

    AppCard(
        modifier = modifier.padding(bottom = 4.dp),
        onClick = {},
        contentPadding = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp, end = 16.dp)
                        .height(18.dp)
                        .then(shimmerModifier)
                )
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(18.dp)
                        .padding(end = 8.dp)
                        .then(shimmerModifier)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .width(100.dp)
                    .height(14.dp)
                    .then(shimmerModifier)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp)
                        .placeholder(
                            visible = true,
                            color = FishingTheme.colorScheme.surfaceContainerHighest,
                            shape = CircleShape,
                        )
                )
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .width(120.dp)
                        .height(14.dp)
                        .then(shimmerModifier)
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .width(48.dp)
                        .height(14.dp)
                        .then(shimmerModifier)
                )
            }
        }
    }
}

@Composable
fun ItemCounter(
    modifier: Modifier = Modifier,
    count: Number,
    icon: DrawableResource,
    tint: Color = LocalColors.current.secondaryIconColor,
) {
    if (count.toInt() == 0) return
    Row(modifier = modifier) {
        Icon(
            modifier = Modifier.size(IconSize.md),
            tint = tint,
            painter = painterResource(icon),
            contentDescription = null,
        )
        AppText(
            text = stringResource(Res.string.item_count_format, count.toInt()),
            style = AppTextStyle.BodySmall,
            color = FishingTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}
