package com.mobileprism.fishing.ui.home.catch

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.navigation.NavController
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.common.Progress
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.model.mappers.getMoonIconByPhase
import com.mobileprism.fishing.model.mappers.getWeatherIconByName
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.advertising.AdIds
import com.mobileprism.fishing.ui.home.advertising.BannerAdvertView
import com.mobileprism.fishing.ui.home.notes.ItemUserPlace
import com.mobileprism.fishing.ui.home.views.LottieWarning
import com.mobileprism.fishing.ui.home.views.*
import com.mobileprism.fishing.domain.entity.weather.PressureValues
import com.mobileprism.fishing.domain.entity.weather.TemperatureValues
import com.mobileprism.fishing.domain.entity.weather.WindSpeedValues
import com.mobileprism.fishing.ui.home.weather.stringRes
import com.mobileprism.fishing.ui.utils.rememberMediaPickerLauncher
import com.mobileprism.fishing.ui.viewmodels.UserCatchViewModel
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.utils.time.toDateTextMonth
import com.mobileprism.fishing.utils.time.toTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatchInfoScreen(navController: NavController, catch: UserCatch) {
    val viewModel: UserCatchViewModel = koinViewModel(parameters = { parametersOf(catch) })

    val loadingState by viewModel.loadingState.collectAsState()
    val loadingDialogState = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = loadingState) {
        loadingDialogState.value = loadingState is Progress.Loading
    }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var currentBottomSheet: BottomSheetCatchScreen? by remember { mutableStateOf(null) }

    val closeSheet: () -> Unit = {
        showBottomSheet = false
    }

    val openSheet: (BottomSheetCatchScreen) -> Unit = {
        currentBottomSheet = it
        showBottomSheet = true
    }

    var pickedPhotosHandler by remember { mutableStateOf<(List<String>) -> Unit>({}) }
    val photoPicker = rememberMediaPickerLauncher(
        maxPhotos = Constants.MAX_PHOTOS,
        onResult = { photos -> pickedPhotosHandler(photos) }
    )

    if (!showBottomSheet) {
        currentBottomSheet = null
    }

    var deleteDialogIsShowing by remember { mutableStateOf(false) }

    if (deleteDialogIsShowing) {
        DeleteCatchDialog(catch, onDismiss = { deleteDialogIsShowing = false }) {
            viewModel.deleteCatch()
            deleteDialogIsShowing = false
            navController.popBackStack()
        }
    }

    ModalLoadingDialog(
        visible = loadingDialogState.value,
        text = stringResource(Res.string.saving_photos)
    )

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = bottomSheetState,
            shape = Constants.modalBottomSheetCorners,
        ) {
            currentBottomSheet?.let { currentSheet ->
                CatchModalBottomSheetContent(
                    currentScreen = currentSheet,
                    onCloseBottomSheet = closeSheet,
                    viewModel = viewModel,
                    photoPicker = photoPicker,
                    onPickedPhotosHandlerChange = { pickedPhotosHandler = it }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            CatchTopBar(
                navController = navController,
                catch = catch
            ) { deleteDialogIsShowing = true }
        },
        bottomBar = {
            BannerAdvertView(
                modifier = Modifier.navigationBarsPadding(),
                adId = AdIds.catchBanner
            )
        }
    ) { innerPadding ->
        CatchContent(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            viewModel = viewModel,
            openSheet = openSheet
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatchTopBar(navController: NavController, catch: UserCatch, onDeleteCatch: () -> Unit) {
    val userPreferences: UserPreferences = koinInject()
    val is12hTime by userPreferences.use12hTimeFormat.collectAsState(initial = false)

    AppTopBar(
        title = stringResource(Res.string.user_catch),
        subtitle = catch.date.toDateTextMonth() + " " + catch.date.toTime(is12hTime),
        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
        onNavigationClick = { navController.popBackStack() },
        actions = {
            AppBarOverflowMenu(
                items = listOf(
                    OverflowMenuItem(
                        label = stringResource(Res.string.delete),
                        onClick = onDeleteCatch,
                        leadingIcon = Icons.Outlined.Delete,
                        tint = MaterialTheme.colorScheme.error
                    )
                )
            )
        }
    )
}

@Composable
fun DeleteCatchDialog(
    catch: UserCatch,
    onDismiss: () -> Unit,
    onPositiveClick: () -> Unit
) {
    DefaultDialog(
        primaryText = stringResource(Res.string.delete_catch_dialog, catch.fishType),
        secondaryText = stringResource(Res.string.catch_delete_confirmantion),
        negativeButtonText = stringResource(Res.string.no),
        onNegativeClick = onDismiss,
        positiveButtonText = stringResource(Res.string.yes),
        onPositiveClick = onPositiveClick,
        onDismiss = onDismiss,
        content = {
            LottieWarning(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }
    )
}

@Composable
fun CatchContent(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: UserCatchViewModel,
    openSheet: (BottomSheetCatchScreen) -> Unit
) {
    val catchState by viewModel.catch.collectAsState()

    val placeState by viewModel.mapMarker.collectAsState()

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(horizontal = Spacing.screenH, vertical = Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap)
    ) {

        CatchTitleView(
            catch = catchState,
            onClick = { openSheet(BottomSheetCatchScreen.EditFishTypeAndWeightScreen) }
        )

        PhotosView(
            photos = catchState.downloadPhotoLinks,
            onEditClick = { openSheet(BottomSheetCatchScreen.EditPhotosScreen) }
        )

        placeState?.let { place ->
            ItemUserPlace(
                place = place,
                userPlaceClicked = {
                    navController.navigate(MainDestinations.Place(it))
                },
                navigateToMap = {
                    navController.navigate(
                        MainDestinations.Map(isAddingNewPlace = false, place = place)
                    )
                },
            )
        }

        DefaultNoteView(
            note = catchState.note,
            onClick = { openSheet(BottomSheetCatchScreen.EditNoteScreen) }
        )

        WayOfFishingView(
            catch = catchState,
            onClick = {
                openSheet(BottomSheetCatchScreen.EditWayOfFishingScreen)
            })

        CatchWeatherView(
            catch = catchState
        )
    }

}

@Composable
fun CatchTitleView(
    modifier: Modifier = Modifier,
    catch: UserCatch,
    onClick: () -> Unit
) {
    SectionCard(
        modifier = modifier,
        icon = painterResource(Res.drawable.ic_fish),
        title = catch.fishType,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            MetricItem(
                label = stringResource(Res.string.kg),
                value = catch.fishWeight.toString()
            )
            MetricItem(
                label = stringResource(Res.string.amount),
                value = "${catch.fishAmount} ${stringResource(Res.string.pc)}"
            )
        }
    }
}

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
fun CatchPhotosView(
    modifier: Modifier = Modifier,
    catch: UserCatch,
    onEditClick: () -> Unit
) {
    PhotosView(
        photos = catch.downloadPhotoLinks,
        onEditClick = onEditClick
    )
}


@Composable
fun WayOfFishingView(
    modifier: Modifier = Modifier,
    catch: UserCatch,
    onClick: () -> Unit
) {
    SectionCard(
        modifier = modifier,
        icon = painterResource(Res.drawable.ic_fishing_rod),
        title = stringResource(Res.string.way_of_fishing),
        onClick = onClick
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            LabeledValueRow(
                label = stringResource(Res.string.fish_rod),
                value = catch.fishingRodType.ifBlank { stringResource(Res.string.no_rod) },
                onClick = onClick,
                editContentDescription = stringResource(Res.string.edit)
            )
            LabeledValueRow(
                label = stringResource(Res.string.bait),
                value = catch.fishingBait.ifBlank { stringResource(Res.string.no_bait) },
                onClick = onClick,
                editContentDescription = stringResource(Res.string.edit)
            )
            LabeledValueRow(
                label = stringResource(Res.string.lure),
                value = catch.fishingLure.ifBlank { stringResource(Res.string.no_lure) },
                onClick = onClick,
                editContentDescription = stringResource(Res.string.edit)
            )
        }
    }
}

@Composable
fun CatchWeatherView(
    modifier: Modifier = Modifier,
    catch: UserCatch
) {
    val weatherPrefs: WeatherPreferences = koinInject()
    val pressureUnit by weatherPrefs.getPressureUnit.collectAsState(PressureValues.mmHg)
    val temperatureUnit by weatherPrefs.getTemperatureUnit.collectAsState(TemperatureValues.C)
    val windSpeedUnit by weatherPrefs.getWindSpeedUnit.collectAsState(WindSpeedValues.metersps)

    SectionCard(
        modifier = modifier,
        icon = painterResource(Res.drawable.weather_sunny),
        title = stringResource(Res.string.weather)
    ) {
        if (catch.weatherPrimary.isBlank() && catch.weatherIcon.isBlank()) {
            NoContentView(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.no_description),
                icon = painterResource(Res.drawable.weather_sunny)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier.size(48.dp),
                        painter = painterResource(getWeatherIconByName(catch.weatherIcon)),
                        contentDescription = stringResource(Res.string.weather)
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Column {
                        MetricItem(
                            label = stringResource(Res.string.weather),
                            value = temperatureUnit.getTemperature(catch.weatherTemperature)
                                    + stringResource(temperatureUnit.stringRes)
                        )
                        AppText(
                            text = catch.weatherPrimary.replaceFirstChar { it.uppercase() },
                            style = AppTextStyle.BodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    MetricItem(
                        label = stringResource(Res.string.moon_phase),
                        value = (catch.weatherMoonPhase * 100).toInt().toString()
                                + " " + stringResource(Res.string.percent),
                        icon = painterResource(getMoonIconByPhase(catch.weatherMoonPhase)),
                        vertical = true
                    )
                }

                HorizontalDivider()

                Row(modifier = Modifier.fillMaxWidth()) {
                    MetricItem(
                        modifier = Modifier.weight(1f),
                        label = stringResource(Res.string.pressure),
                        value = pressureUnit.getPressureFromMmhg(catch.weatherPressure)
                                + " " + pressureUnit.name,
                        icon = painterResource(Res.drawable.ic_gauge),
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        vertical = true
                    )
                    MetricItem(
                        modifier = Modifier.weight(1f),
                        label = stringResource(Res.string.wind),
                        value = windSpeedUnit.getWindSpeed(catch.weatherWindSpeed.toDouble())
                                + " " + windSpeedUnit.name,
                        icon = painterResource(Res.drawable.ic_wind),
                        vertical = true
                    )
                }
            }
        }
    }
}
