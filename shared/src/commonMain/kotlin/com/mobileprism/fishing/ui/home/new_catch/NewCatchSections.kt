package com.mobileprism.fishing.ui.home.new_catch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.components.AutoSuggestTextField
import com.mobileprism.fishing.ui.components.DateTimePickerField
import com.mobileprism.fishing.ui.components.ExpandableSection
import com.mobileprism.fishing.ui.components.StepperField
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.views.DefaultButtonOutlined
import com.mobileprism.fishing.ui.home.views.MaxCounterView
import com.mobileprism.fishing.ui.home.views.NewCatchPhotoView
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.ui.theme.customColors
import com.mobileprism.fishing.ui.utils.rememberMediaPickerLauncher
import com.mobileprism.fishing.ui.viewmodels.CatchWeatherState
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.utils.network.ConnectionState
import com.mobileprism.fishing.utils.network.rememberConnectionState
import com.mobileprism.fishing.utils.time.toDate
import com.mobileprism.fishing.utils.time.toTime
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun EssentialsSection(
    modifier: Modifier = Modifier,
    placeTitle: String?,
    placeColor: Int?,
    isLocationLocked: Boolean,
    dateTime: Long,
    fishName: String,
    fishSpeciesHistory: List<String>,
    fishAmount: Int,
    fishWeight: Double,
    onPlaceClick: () -> Unit,
    onDateChange: (Long) -> Unit,
    onFishNameChange: (String) -> Unit,
    onFishAmountChange: (Int) -> Unit,
    onFishWeightChange: (Double) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        if (isLocationLocked) {
            PlaceField(
                modifier = Modifier.fillMaxWidth(),
                placeTitle = placeTitle,
                placeColor = placeColor,
                placeholder = stringResource(Res.string.place),
                trailing = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = {
                    SnackbarManager.showMessage(Res.string.another_place_in_new_catch)
                }
            )
        } else {
            PlaceField(
                modifier = Modifier.fillMaxWidth(),
                placeTitle = placeTitle,
                placeColor = placeColor,
                placeholder = stringResource(Res.string.place),
                trailing = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_baseline_event_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = onPlaceClick
            )
        }

        AutoSuggestTextField(
            modifier = Modifier.fillMaxWidth(),
            value = fishName,
            onValueChange = onFishNameChange,
            label = stringResource(Res.string.fish_species),
            suggestions = fishSpeciesHistory,
            isRequired = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            StepperField(
                modifier = Modifier.weight(1f),
                value = fishAmount,
                onValueChange = onFishAmountChange,
                label = stringResource(Res.string.amount),
                suffix = stringResource(Res.string.pc)
            )
            StepperField(
                modifier = Modifier.weight(1f),
                value = fishWeight,
                onValueChange = onFishWeightChange,
                label = stringResource(Res.string.weight),
                suffix = stringResource(Res.string.kg)
            )
        }

        DateTimePickerField(
            dateTime = dateTime,
            onDateTimeChange = onDateChange
        )
    }
}

@Composable
fun EssentialsSummary(
    modifier: Modifier = Modifier,
    placeTitle: String,
    placeColor: Int?,
    dateTime: Long,
    fishName: String,
    fishAmount: Int,
    fishWeight: Double,
    onClick: () -> Unit,
) {
    val separator = stringResource(Res.string.catch_summary_separator)
    val countTemplate = stringResource(Res.string.fish_count_format)
    val kgSuffix = stringResource(Res.string.kg)
    val summaryText = CatchSummaryFormatter.format(
        fishName = fishName,
        amount = fishAmount,
        weight = fishWeight,
        kgSuffix = kgSuffix,
        countTemplate = countTemplate,
        separator = separator,
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                placeColor?.let {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(it)
                    )
                    Spacer(Modifier.width(Spacing.xxs))
                }
                Text(
                    text = placeTitle,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = dateTime.toDate() + " " + dateTime.toTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = summaryText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WeatherSection(
    modifier: Modifier = Modifier,
    state: CatchWeatherState,
    onPrimaryChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onTemperatureChange: (String) -> Unit,
    onPressureChange: (String) -> Unit,
    onWindChange: (String) -> Unit,
    onWindDirChange: (Float) -> Unit,
    onErrorChange: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onClose: () -> Unit,
) {
    var primaryWeatherError by remember { mutableStateOf(false) }
    var temperatureError by remember { mutableStateOf(false) }
    var pressureError by remember { mutableStateOf(false) }
    var windError by remember { mutableStateOf(false) }

    val isError = primaryWeatherError || temperatureError || pressureError || windError
    androidx.compose.runtime.LaunchedEffect(isError) {
        onErrorChange(isError)
    }

    val internetConnectionState by rememberConnectionState()

    ExpandableSection(
        modifier = modifier,
        title = stringResource(Res.string.weather),
        icon = Res.drawable.weather_cloudy,
        onClose = onClose,
        actions = {
            if (internetConnectionState is ConnectionState.Unavailable) {
                Icon(
                    painter = painterResource(Res.drawable.ic_no_internet),
                    contentDescription = null,
                    tint = MaterialTheme.customColors.secondaryIconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (!state.isDownloadAvailable) {
                Text(
                    text = stringResource(Res.string.auto_filled),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(Spacing.xs))
            }

            IconButton(onClick = onRefresh) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        painter = when {
                            state.isDownloadAvailable && internetConnectionState is ConnectionState.Available ->
                                painterResource(Res.drawable.ic_baseline_download_24)
                            else -> painterResource(Res.drawable.ic_baseline_refresh_24)
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    ) {
        NewCatchWeatherPrimary(
            modifier = Modifier.fillMaxWidth(),
            weatherDescription = state.primary,
            weatherIconId = state.icon,
            onDescriptionChange = onPrimaryChange,
            onIconChange = onIconChange,
            onError = { primaryWeatherError = it }
        )

        Spacer(Modifier.height(Spacing.sm))

        Row(modifier = Modifier.fillMaxWidth()) {
            NewCatchTemperatureView(
                modifier = Modifier.weight(1f).padding(end = Spacing.xs),
                temperature = state.temperature,
                onTemperatureChange = onTemperatureChange,
                onError = { temperatureError = it }
            )
            NewCatchPressureView(
                modifier = Modifier.weight(1f).padding(start = Spacing.xs),
                pressure = state.pressure,
                onPressureChange = onPressureChange,
                onError = { pressureError = it }
            )
        }

        Spacer(Modifier.height(Spacing.sm))

        Row(modifier = Modifier.fillMaxWidth()) {
            NewCatchWindView(
                modifier = Modifier.weight(1f).padding(end = Spacing.xs),
                wind = state.windSpeed,
                windDeg = state.windDeg,
                onWindChange = onWindChange,
                onWindDirChange = onWindDirChange,
                onError = { windError = it }
            )
            NewCatchMoonView(
                modifier = Modifier.weight(1f).padding(start = Spacing.xs),
                moonPhase = state.moonPhase
            )
        }
    }
}

@Composable
fun GearSection(
    modifier: Modifier = Modifier,
    rod: String,
    bait: String,
    lure: String,
    onRodChange: (String) -> Unit,
    onBaitChange: (String) -> Unit,
    onLureChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    ExpandableSection(
        modifier = modifier,
        title = stringResource(Res.string.way_of_fishing),
        icon = Res.drawable.ic_fishing_rod,
        onClose = onClose
    ) {
        WayOfFishingView(
            rodState = rod,
            biteState = bait,
            lureState = lure,
            onRodChange = onRodChange,
            onBiteChange = onBaitChange,
            onLureChange = onLureChange
        )
    }
}

@Composable
fun PhotosSection(
    modifier: Modifier = Modifier,
    photos: List<String>,
    onAddPhotos: (List<String>) -> Unit,
    onDeletePhoto: (String) -> Unit,
    onClose: () -> Unit,
) {
    val internetConnectionState by rememberConnectionState()

    val mediaPicker = rememberMediaPickerLauncher(
        maxPhotos = Constants.MAX_PHOTOS,
        onResult = { newPhotos ->
            if ((newPhotos.size + photos.size) > Constants.MAX_PHOTOS) {
                SnackbarManager.showMessage(Res.string.max_photos_allowed)
            } else {
                onAddPhotos(newPhotos)
            }
        }
    )

    ExpandableSection(
        modifier = modifier,
        title = stringResource(Res.string.photos),
        icon = Res.drawable.ic_baseline_photo_24,
        onClose = onClose,
        badge = {
            MaxCounterView(
                count = photos.size,
                maxCount = Constants.MAX_PHOTOS
            )
        }
    ) {
        if (photos.isNotEmpty()) {
            NewCatchPhotoView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = Spacing.sm),
                photos = photos,
                onDelete = onDeletePhoto
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            DefaultButtonOutlined(
                text = stringResource(Res.string.gallery),
                icon = painterResource(Res.drawable.ic_baseline_add_photo_alternate_24),
                enabled = internetConnectionState is ConnectionState.Available,
                onClick = { mediaPicker.launchGallery() }
            )
            Spacer(Modifier.width(Spacing.sm))
            DefaultButtonOutlined(
                text = stringResource(Res.string.camera),
                icon = painterResource(Res.drawable.ic_baseline_photo_camera_24),
                onClick = { mediaPicker.launchCamera() }
            )
        }
    }
}

@Composable
fun NoteSection(
    modifier: Modifier = Modifier,
    note: String,
    onNoteChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    ExpandableSection(
        modifier = modifier,
        title = stringResource(Res.string.note),
        icon = Res.drawable.ic_baseline_edit_note_24,
        onClose = onClose
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 5,
            label = { Text(stringResource(Res.string.note)) },
            value = note,
            onValueChange = onNoteChange
        )
    }
}
