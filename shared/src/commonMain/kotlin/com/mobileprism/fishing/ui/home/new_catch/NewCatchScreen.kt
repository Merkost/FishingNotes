package com.mobileprism.fishing.ui.home.new_catch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.home.SnackbarAction
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.advertising.AdIds
import com.mobileprism.fishing.ui.home.advertising.BannerAdvertView
import com.mobileprism.fishing.ui.home.advertising.rememberInterstitialAdLauncher
import com.mobileprism.fishing.ui.home.views.BottomActionBar
import com.mobileprism.fishing.ui.home.views.DefaultAppBar
import com.mobileprism.fishing.ui.home.views.ModalLoadingDialog
import com.mobileprism.fishing.ui.home.weather.WeatherPlacePickerSheetContent
import com.mobileprism.fishing.ui.theme.Motion
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.ui.utils.PlatformBackHandler
import com.mobileprism.fishing.ui.viewmodels.DetailSection
import com.mobileprism.fishing.ui.viewmodels.NewCatchMasterViewModel
import com.mobileprism.fishing.ui.viewstates.NewCatchViewState
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.utils.Constants.MAX_PHOTOS
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewCatchMasterScreen(
    receivedPlace: UserMapMarker?,
    navController: NavController,
    upPress: () -> Unit,
) {
    val viewModel: NewCatchMasterViewModel = koinViewModel(parameters = {
        parametersOf(
            if (receivedPlace != null) {
                ReceivedPlaceState.Received(receivedPlace)
            } else {
                ReceivedPlaceState.NotReceived
            }
        )
    })

    var exitDialogIsShowing by remember { mutableStateOf(false) }
    var showPlacePicker by remember { mutableStateOf(false) }

    if (exitDialogIsShowing) {
        CancelNewCatchDialog(
            onDismiss = { exitDialogIsShowing = false },
            onPositiveClick = { exitDialogIsShowing = false; upPress() }
        )
    }

    PlatformBackHandler {
        exitDialogIsShowing = true
    }

    val loadingDialogState = remember { mutableStateOf(false) }
    val launchAd = rememberInterstitialAdLauncher(onComplete = { upPress() })

    val onSave = {
        if (viewModel.photos.value.size <= MAX_PHOTOS) {
            viewModel.saveNewCatch()
        } else {
            SnackbarManager.showMessage(Res.string.max_photos_allowed)
        }
    }

    val uiState = viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = uiState.value) {
        when (val state = uiState.value) {
            NewCatchViewState.Editing -> {}
            NewCatchViewState.Complete -> {
                loadingDialogState.value = false
                SnackbarManager.showMessage(Res.string.catch_added_successfully)
                launchAd()
            }
            NewCatchViewState.SavingNewCatch -> {
                loadingDialogState.value = true
            }
            is NewCatchViewState.Error -> {
                loadingDialogState.value = false
                SnackbarManager.showMessage(
                    messageTextId = Res.string.error_occured,
                    snackbarAction = SnackbarAction(
                        textId = Res.string.retry,
                        action = { viewModel.saveNewCatch() }
                    ),
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val currentProgress = uploadProgress
    val loadingText = when {
        currentProgress != null -> stringResource(
            Res.string.uploading_photo_progress,
            currentProgress.uploaded,
            currentProgress.total
        )
        else -> stringResource(Res.string.saving_new_catch)
    }

    ModalLoadingDialog(
        visible = loadingDialogState.value,
        text = loadingText,
        progress = currentProgress?.takeIf { it.total > 0 }?.let { it.uploaded.toFloat() / it.total }
    )

    val placeAndTime by viewModel.placeAndTimeState.collectAsState()
    val fishState by viewModel.fishAndWeightState.collectAsState()
    val catchInfo by viewModel.catchInfoState.collectAsState()
    val weatherState by viewModel.catchWeatherState.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val fishSpeciesHistory by viewModel.fishSpeciesHistory.collectAsState()
    val expandedSections by viewModel.expandedSections.collectAsState()
    val essentialsCollapsed by viewModel.essentialsCollapsed.collectAsState()
    val skipAvailable by viewModel.skipAvailable.collectAsState()

    LaunchedEffect(placeAndTime.place, placeAndTime.date) {
        if (placeAndTime.place != null && weatherState.isDownloadAvailable) {
            viewModel.loadWeather()
        }
    }

    val placesList = when (val state = placeAndTime.placesListState) {
        is NewCatchPlacesState.Received -> state.locations
        NewCatchPlacesState.NotReceived -> emptyList()
    }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showPlacePicker) {
        ModalBottomSheet(
            onDismissRequest = { showPlacePicker = false },
            sheetState = bottomSheetState,
            shape = Constants.modalBottomSheetCorners,
        ) {
            WeatherPlacePickerSheetContent(
                places = placesList,
                selectedPlace = placeAndTime.place,
                onPlaceSelected = { place ->
                    viewModel.setSelectedPlace(place)
                    showPlacePicker = false
                }
            )
        }
    }

    if (placesList.isEmpty() && placeAndTime.placesListState is NewCatchPlacesState.Received && !placeAndTime.isLocationCocked) {
        NewCatchNoPlaceDialog(navController)
    }

    Scaffold(
        topBar = {
            DefaultAppBar(
                title = stringResource(Res.string.new_catch),
                onNavClick = { exitDialogIsShowing = true }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Spacer(Modifier.height(Spacing.xs))

                AnimatedVisibility(
                    visible = !essentialsCollapsed,
                    enter = expandVertically(Motion.enterContent()),
                    exit = shrinkVertically(Motion.screenExit())
                ) {
                    EssentialsSection(
                        placeTitle = placeAndTime.place?.title,
                        placeColor = placeAndTime.place?.markerColor,
                        isLocationLocked = placeAndTime.isLocationCocked,
                        dateTime = placeAndTime.date,
                        fishName = fishState.fish,
                        fishSpeciesHistory = fishSpeciesHistory,
                        fishAmount = fishState.fishAmount,
                        fishWeight = fishState.fishWeight,
                        onPlaceClick = { showPlacePicker = true },
                        onDateChange = { viewModel.setDate(it) },
                        onFishNameChange = { viewModel.setFishType(it) },
                        onFishAmountChange = { viewModel.setFishAmount(it) },
                        onFishWeightChange = { viewModel.setFishWeight(it) }
                    )
                }

                AnimatedVisibility(
                    visible = essentialsCollapsed,
                    enter = expandVertically(Motion.enterContent()),
                    exit = shrinkVertically(Motion.screenExit())
                ) {
                    placeAndTime.place?.let { place ->
                        EssentialsSummary(
                            placeTitle = place.title,
                            placeColor = place.markerColor,
                            dateTime = placeAndTime.date,
                            fishName = fishState.fish,
                            fishAmount = fishState.fishAmount,
                            fishWeight = fishState.fishWeight,
                            onClick = { viewModel.collapseAllSections() }
                        )
                    }
                }

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    val weatherDone = !weatherState.isDownloadAvailable && weatherState.primary.isNotBlank()
                    StatusFilterChip(
                        label = stringResource(Res.string.weather),
                        selected = DetailSection.WEATHER in expandedSections,
                        badge = when {
                            weatherState.isLoading -> ChipBadge.Loading
                            weatherDone -> ChipBadge.Done
                            else -> ChipBadge.None
                        },
                        onClick = { viewModel.toggleSection(DetailSection.WEATHER) }
                    )
                    StatusFilterChip(
                        label = stringResource(Res.string.way_of_fishing),
                        selected = DetailSection.GEAR in expandedSections,
                        badge = if (catchInfo.rod.isNotBlank() || catchInfo.bait.isNotBlank() || catchInfo.lure.isNotBlank()) {
                            ChipBadge.Done
                        } else {
                            ChipBadge.None
                        },
                        onClick = { viewModel.toggleSection(DetailSection.GEAR) }
                    )
                    StatusFilterChip(
                        label = stringResource(Res.string.photos),
                        selected = DetailSection.PHOTOS in expandedSections,
                        badge = ChipBadge.Count(photos.size),
                        onClick = { viewModel.toggleSection(DetailSection.PHOTOS) }
                    )
                    StatusFilterChip(
                        label = stringResource(Res.string.note),
                        selected = DetailSection.NOTE in expandedSections,
                        badge = if (catchInfo.note.isNotBlank()) ChipBadge.Done else ChipBadge.None,
                        onClick = { viewModel.toggleSection(DetailSection.NOTE) }
                    )
                }

                AnimatedVisibility(
                    visible = DetailSection.WEATHER in expandedSections,
                    enter = expandVertically(Motion.enterContent()),
                    exit = shrinkVertically(Motion.screenExit())
                ) {
                    WeatherSection(
                        state = weatherState,
                        onPrimaryChange = { viewModel.setWeatherPrimary(it) },
                        onIconChange = { viewModel.setWeatherIconId(it) },
                        onTemperatureChange = { viewModel.setWeatherTemperature(it) },
                        onPressureChange = { viewModel.setWeatherPressure(it) },
                        onWindChange = { viewModel.setWeatherWindSpeed(it) },
                        onWindDirChange = { viewModel.setWeatherWindDeg(it.toInt()) },
                        onErrorChange = { viewModel.setWeatherIsError(it) },
                        onRefresh = { viewModel.loadWeather() },
                        onClose = { viewModel.toggleSection(DetailSection.WEATHER) }
                    )
                }

                AnimatedVisibility(
                    visible = DetailSection.GEAR in expandedSections,
                    enter = expandVertically(Motion.enterContent()),
                    exit = shrinkVertically(Motion.screenExit())
                ) {
                    GearSection(
                        rod = catchInfo.rod,
                        bait = catchInfo.bait,
                        lure = catchInfo.lure,
                        onRodChange = { viewModel.setRod(it) },
                        onBaitChange = { viewModel.setBait(it) },
                        onLureChange = { viewModel.setLure(it) },
                        onClose = { viewModel.toggleSection(DetailSection.GEAR) }
                    )
                }

                AnimatedVisibility(
                    visible = DetailSection.PHOTOS in expandedSections,
                    enter = expandVertically(Motion.enterContent()),
                    exit = shrinkVertically(Motion.screenExit())
                ) {
                    PhotosSection(
                        photos = photos,
                        onAddPhotos = { viewModel.addPhotos(it) },
                        onDeletePhoto = { viewModel.deletePhoto(it) },
                        onClose = { viewModel.toggleSection(DetailSection.PHOTOS) }
                    )
                }

                AnimatedVisibility(
                    visible = DetailSection.NOTE in expandedSections,
                    enter = expandVertically(Motion.enterContent()),
                    exit = shrinkVertically(Motion.screenExit())
                ) {
                    NoteSection(
                        note = catchInfo.note,
                        onNoteChange = { viewModel.setNote(it) },
                        onClose = { viewModel.toggleSection(DetailSection.NOTE) }
                    )
                }

                Spacer(Modifier.height(Spacing.sm))
            }

            BottomActionBar(
                primaryText = stringResource(Res.string.save),
                loading = loadingDialogState.value,
                onClick = {
                    if (skipAvailable) {
                        onSave()
                    } else {
                        SnackbarManager.showMessage(Res.string.new_catch_skip_tutor)
                    }
                }
            )

            BannerAdvertView(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                adId = AdIds.newCatchBanner,
            )
        }
    }
}
