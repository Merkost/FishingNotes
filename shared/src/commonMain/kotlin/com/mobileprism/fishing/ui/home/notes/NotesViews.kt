package com.mobileprism.fishing.ui.home.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.mobileprism.fishing.ui.theme.LocalColors
import com.mobileprism.fishing.ui.theme.cardColor
import com.mobileprism.fishing.ui.theme.customColors
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

    DefaultCardClickable(
        modifier = modifier.padding(bottom = 4.dp),
        onClick = { userPlaceClicked(place) }
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
                PrimaryText(
                    modifier = childModifier,
                    text = place.title,
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SupportText(
                        modifier = childModifier,
                        text = place.dateOfCreation.toDateTextMonth()
                    )

                    ItemCounter(
                        modifier = Modifier.padding(start = 8.dp),
                        count = place.catchesCount,
                        icon = Res.drawable.ic_fishing,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    )
                }
            }

            DefaultIconButton(
                childModifier = childModifier,
                icon = painterResource(Res.drawable.ic_place_on_map),
                tint = if (!place.visible) LocalColors.current.secondaryTextColor
                        else MaterialTheme.colorScheme.onSurface,
                onClick = { navigateToMap() }
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
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            shape = RoundedCornerShape(24.dp), color = cardColor
        ) {
            SecondaryTextColored(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                text = text,
                color = MaterialTheme.colorScheme.inverseOnSurface
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

    DefaultCardClickable(
        modifier = modifier.padding(bottom = 4.dp),
        onClick = { onClick(catch) }
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
                PrimaryText(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp, end = 16.dp)
                        .then(childModifier),
                    text = catch.fishType,
                    maxLines = 1
                )

                PrimaryText(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .then(childModifier),
                    text = "${catch.fishWeight} ${stringResource(Res.string.kg)}"
                )
            }

            SecondaryTextSmall(
                modifier = Modifier
                    .padding(top = 4.dp, start = 8.dp)
                    .then(childModifier),
                text = "${stringResource(Res.string.amount)}: ${catch.fishAmount}" +
                        " ${stringResource(Res.string.pc)}"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showPlace) {
                    Icon(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(start = 8.dp),
                        painter = painterResource(Res.drawable.ic_baseline_location_on_24),
                        contentDescription = stringResource(Res.string.location),
                        tint = MaterialTheme.colorScheme.outline
                    )

                    SecondaryText(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp, end = 8.dp)
                            .then(childModifier),
                        text = catch.placeTitle,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        textColor = MaterialTheme.colorScheme.outline
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                ItemCounter(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .then(childModifier),
                    count = catch.downloadPhotoLinks.size,
                    icon = Res.drawable.ic_baseline_photo_24,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                )

                SupportText(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .then(childModifier),
                    text = catch.date.toTime(is12hTimeFormat)
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
    tint: Color = LocalColors.current.secondaryIconColor
) {
    Row(modifier = modifier) {
        Icon(
            modifier = Modifier.size(24.dp),
            tint = tint,
            painter = painterResource(icon),
            contentDescription = null,
        )
        SupportText(text = stringResource(Res.string.item_count_format, count.toInt()))
    }

}