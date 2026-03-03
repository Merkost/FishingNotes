package com.mobileprism.fishing.ui.home.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
        // Drag handle
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .align(Alignment.CenterHorizontally)
                .width(32.dp)
                .height(4.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                )
        )

        // Header
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            text = stringResource(Res.string.select_place),
            style = MaterialTheme.typography.titleLarge,
        )

        // Search field
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(stringResource(Res.string.search_places)) },
            singleLine = true,
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(Res.string.close))
                    }
                } else {
                    Icon(Icons.Filled.Search, contentDescription = null)
                }
            },
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (!showCurrentLocation && filteredPlaces.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.no_places_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                if (showCurrentLocation && currentLocationPlace != null) {
                    item(key = CURRENT_PLACE_ITEM_ID) {
                        PlaceItem(
                            place = currentLocationPlace,
                            isSelected = selectedPlace?.id == currentLocationPlace.id,
                            isCurrentLocation = true,
                            onClick = { onPlaceSelected(currentLocationPlace) },
                        )
                    }
                }
                items(
                    items = filteredPlaces,
                    key = { it.id }
                ) { place ->
                    PlaceItem(
                        place = place,
                        isSelected = selectedPlace?.id == place.id,
                        isCurrentLocation = false,
                        onClick = { onPlaceSelected(place) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceItem(
    place: UserMapMarker,
    isSelected: Boolean,
    isCurrentLocation: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            painter = painterResource(
                if (isCurrentLocation) {
                    Res.drawable.ic_baseline_my_location_24
                } else {
                    Res.drawable.ic_baseline_location_on_24
                }
            ),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(24.dp),
        )
        Text(
            modifier = Modifier.weight(1f),
            text = place.title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
