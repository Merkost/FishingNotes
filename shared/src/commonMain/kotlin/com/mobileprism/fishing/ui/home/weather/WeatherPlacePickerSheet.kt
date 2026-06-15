package com.mobileprism.fishing.ui.home.weather

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.utils.Constants.CURRENT_PLACE_ITEM_ID

@Composable
fun WeatherPlacePickerSheetContent(
    places: List<UserMapMarker>,
    selectedPlace: UserMapMarker?,
    onPlaceSelected: (UserMapMarker) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

    val currentLocationPlace = remember(places) {
        places.firstOrNull { it.id == CURRENT_PLACE_ITEM_ID }
    }
    val regularPlaces = remember(places) {
        places.filter { it.id != CURRENT_PLACE_ITEM_ID }
    }
    val filteredPlaces = remember(regularPlaces, searchQuery) {
        if (searchQuery.isBlank()) regularPlaces
        else regularPlaces.filter {
            it.title.contains(searchQuery, ignoreCase = true)
        }
    }
    val showCurrentLocation = remember(currentLocationPlace, searchQuery) {
        currentLocationPlace != null && (searchQuery.isBlank() ||
                currentLocationPlace.title.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            text = stringResource(Res.string.select_place),
            style = FishingTheme.typography.titleLarge,
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(stringResource(Res.string.search_places)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = FishingTheme.colorScheme.outline,
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(Res.string.close))
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (!showCurrentLocation && filteredPlaces.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.no_places_found),
                    style = FishingTheme.typography.bodyLarge,
                    color = FishingTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .padding(horizontal = 12.dp),
            ) {
                if (showCurrentLocation && currentLocationPlace != null) {
                    item(key = CURRENT_PLACE_ITEM_ID) {
                        CurrentLocationItem(
                            place = currentLocationPlace,
                            isSelected = selectedPlace?.id == currentLocationPlace.id,
                            onClick = { onPlaceSelected(currentLocationPlace) },
                        )
                    }
                }

                if (filteredPlaces.isNotEmpty()) {
                    item(key = "section_saved") {
                        SectionLabel(text = stringResource(Res.string.saved_places))
                    }

                    items(
                        items = filteredPlaces,
                        key = { it.id }
                    ) { place ->
                        PlaceItem(
                            place = place,
                            isSelected = selectedPlace?.id == place.id,
                            onClick = { onPlaceSelected(place) },
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun CurrentLocationItem(
    place: UserMapMarker,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (isSelected) {
        FishingTheme.colorScheme.primary.copy(alpha = 0.08f)
    } else {
        Color.Transparent
    }
    val dotColor = FishingTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .drawBehind {
                    drawCircle(color = dotColor)
                    drawCircle(
                        color = dotColor.copy(alpha = 0.3f),
                        radius = size.minDimension / 2f + 4.dp.toPx(),
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
        )
        Text(
            modifier = Modifier.weight(1f),
            text = place.title,
            style = FishingTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = FishingTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
        text = text,
        style = FishingTheme.typography.labelSmall,
        color = FishingTheme.colorScheme.outline,
        letterSpacing = 1.sp,
    )
}

@Composable
private fun PlaceItem(
    place: UserMapMarker,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (isSelected) {
        FishingTheme.colorScheme.primary.copy(alpha = 0.08f)
    } else {
        Color.Transparent
    }
    val markerColor = Color(place.markerColor)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .drawBehind {
                    drawCircle(color = markerColor)
                }
        )
        Text(
            modifier = Modifier.weight(1f),
            text = place.title,
            style = FishingTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = FishingTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
