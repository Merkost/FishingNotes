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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.weather.WindSpeedValues
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.utils.placeholder
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.viewmodels.MapViewModel
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.ui.home.weather.stringRes
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun PlaceDetailsCard(
    viewModel: MapViewModel,
    navController: NavController,
    onAddCatch: (UserMapMarker) -> Unit = {},
    onSaveCurrentPlace: () -> Unit = {},
) {
    val receivedMarker by viewModel.currentMarker.collectAsState()
    val windRotation by viewModel.windIconRotation.collectAsStateWithLifecycle()
    val weatherPreferences: WeatherPreferences = koinInject()
    val windUnit by weatherPreferences.getWindSpeedUnit.collectAsState(WindSpeedValues.metersps)
    var address by remember { mutableStateOf("") }
    val addressState by viewModel.currentMarkerAddressState.collectAsState()
    val rawDistance by viewModel.currentMarkerRawDistance.collectAsState()
    val lastKnownLocation by viewModel.lastKnownLocation.collectAsState()
    val fishActivity by viewModel.fishActivity.collectAsStateWithLifecycle()
    val currentWeather by viewModel.currentWeather.collectAsStateWithLifecycle()

    val mLabel = stringResource(Res.string.m)
    val kmLabel = stringResource(Res.string.km)
    val distance: String? by remember(rawDistance) {
        mutableStateOf(rawDistance?.let { convertDistance(it, mLabel, kmLabel) })
    }

    SetPlaceNameResultListener(addressState) { newPlaceName ->
        address = newPlaceName
    }

    receivedMarker?.let { notNullMarker ->
        LaunchedEffect(receivedMarker, lastKnownLocation) {
            viewModel.setNewMarkerInfo(notNullMarker.latitude, notNullMarker.longitude)
        }
    }

    val subtitleLoading = address.isBlank() && distance == null
    val statsLoading = fishActivity == null && currentWeather == null

    receivedMarker?.let { marker ->
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .zIndex(1.0f)
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp)
                .animateContentSize(animationSpec = tween(300, easing = LinearOutSlowInEasing)),
            onClick = {
                if (marker.id != Constants.CURRENT_PLACE_ITEM_ID) {
                    navController.navigate(MainDestinations.Place(marker))
                } else {
                    onSaveCurrentPlace()
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = marker.title.ifEmpty { stringResource(Res.string.no_name_place) },
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
                            Text(
                                text = buildString {
                                    if (address.isNotBlank()) append(address)
                                    if (address.isNotBlank() && distance != null) append(" \u00B7 ")
                                    distance?.let { append(it) }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = currentWeather != null,
                        enter = fadeIn(tween(400)),
                        exit = fadeOut(tween(200)),
                    ) {
                        currentWeather?.let { weather ->
                            Text(
                                text = "${weather.temp}\u00B0",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFE65100),
                                modifier = Modifier.padding(horizontal = 10.dp),
                            )
                        }
                    }

                    if (currentWeather == null) {
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

                    if (marker.id != Constants.CURRENT_PLACE_ITEM_ID) {
                        IconButton(
                            onClick = { onAddCatch(marker) },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color(0xFF43A047),
                                contentColor = Color.White,
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

                Spacer(modifier = Modifier.height(10.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(8.dp))

                if (statsLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(18.dp)
                                .padding(horizontal = 8.dp)
                                .placeholder(
                                    visible = true,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(4.dp),
                                ),
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(16.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(18.dp)
                                .padding(horizontal = 8.dp)
                                .placeholder(
                                    visible = true,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(4.dp),
                                ),
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(16.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(18.dp)
                                .padding(horizontal = 8.dp)
                                .placeholder(
                                    visible = true,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(4.dp),
                                ),
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(400)),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.fish),
                                    contentDescription = null,
                                    tint = if (fishActivity != null) Color(0xFF2E7D32)
                                    else MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = fishActivity?.let { "$it%" } ?: "\u2014",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (fishActivity != null) Color(0xFF2E7D32)
                                    else MaterialTheme.colorScheme.outlineVariant,
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(16.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        )

                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(400, delayMillis = 100)),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_baseline_navigation_24),
                                    contentDescription = null,
                                    tint = if (currentWeather != null) Color(0xFF1565C0)
                                    else MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .rotate(windRotation),
                                )
                                Text(
                                    text = currentWeather?.let {
                                        "${windUnit.getWindSpeed(it.wind_speed)} ${stringResource(windUnit.stringRes)}"
                                    } ?: "\u2014",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (currentWeather != null) Color(0xFF1565C0)
                                    else MaterialTheme.colorScheme.outlineVariant,
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(16.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        )

                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(400, delayMillis = 200)),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_add_catch),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = "${marker.catchesCount} ${stringResource(Res.string.catches).lowercase()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
