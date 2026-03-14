package com.mobileprism.fishing.ui.home.catch

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
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
import com.mobileprism.fishing.ui.viewmodels.UserCatchViewModel
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
            Spacer(modifier = Modifier.height(1.dp))

            currentBottomSheet?.let { currentSheet ->
                CatchModalBottomSheetContent(
                    currentScreen = currentSheet,
                    onCloseBottomSheet = closeSheet,
                    viewModel = viewModel
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

@Composable
fun CatchTopBar(navController: NavController, catch: UserCatch, onDeleteCatch: () -> Unit) {
    val userPreferences: UserPreferences = koinInject()
    val is12hTime by userPreferences.use12hTimeFormat.collectAsState(initial = false)

    var menuOpened by remember { mutableStateOf(false) }

    DefaultAppBar(
        title = stringResource(Res.string.user_catch),
        subtitle = catch.date.toDateTextMonth() + " " + catch.date.toTime(is12hTime),
        onNavClick = { navController.popBackStack() }
    ) {
        IconButton(
            modifier = Modifier.padding(horizontal = 4.dp),
            onClick = { menuOpened = true }
        ) {
            Icon(imageVector = Icons.Outlined.MoreVert, Icons.Outlined.MoreVert.name)
        }
        DropdownMenu(expanded = menuOpened, onDismissRequest = { menuOpened = false }) {
            DropdownMenuItem(
                text = { Text(text = stringResource(Res.string.delete), maxLines = 1) },
                onClick = { menuOpened = false; onDeleteCatch() }
            )
        }
    }
}

@Composable
fun DeleteCatchDialog(
    catch: UserCatch,
    onDismiss: () -> Unit,
    onPositiveClick: () -> Unit
) {
    DefaultDialog(
        primaryText = stringResource(Res.string.delete_catch_dialog).replace("%s", catch.fishType).replace("%1\$s", catch.fishType),
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
    val photosState = remember { mutableStateOf(listOf<String>()) }

    val catchState by viewModel.catch.collectAsState()

    val placeState by viewModel.mapMarker.collectAsState()

    LaunchedEffect(key1 = photosState.value) {
        if (photosState.value.isNotEmpty()) {
            viewModel.updateCatchPhotos(photosState.value)
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
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

    DefaultCardClickable(
        modifier = modifier,
        onClick = onClick
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
                HeaderText(
                    modifier = Modifier.weight(1f),
                    text = catch.fishType
                )
                HeaderText(
                    text = "${catch.fishWeight} ${stringResource(Res.string.kg)}"
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            SecondaryText(
                text = "${stringResource(Res.string.amount)}: ${catch.fishAmount} " +
                        stringResource(Res.string.pc)
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

    DefaultCardClickable(
        modifier = modifier,
        onClick = { onClick() }
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SubtitleWithIcon(
                icon = Res.drawable.ic_fishing_rod,
                text = stringResource(Res.string.way_of_fishing)
            )

            SimpleUnderlineTextField(
                modifier = Modifier.fillMaxWidth(),
                text = if (catch.fishingRodType.isNotBlank()) {
                    catch.fishingRodType
                } else {
                    stringResource(Res.string.no_rod)
                },
                label = stringResource(Res.string.fish_rod),
                onClick = onClick
            )

            SimpleUnderlineTextField(
                modifier = Modifier.fillMaxWidth(),
                text = if (catch.fishingBait.isNotBlank()) {
                    catch.fishingBait
                } else {
                    stringResource(Res.string.no_bait)
                },
                label = stringResource(Res.string.bait),
                onClick = onClick
            )

            SimpleUnderlineTextField(
                modifier = Modifier.fillMaxWidth(),
                text = if (catch.fishingLure.isNotBlank()) {
                    catch.fishingLure
                } else {
                    stringResource(Res.string.no_lure)
                },
                label = stringResource(Res.string.lure),
                onClick = onClick
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

    DefaultCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SubtitleWithIcon(
                icon = Res.drawable.weather_sunny,
                text = stringResource(Res.string.weather)
            )

            // Primary weather row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(horizontal = 2.dp),
                    painter = painterResource(getWeatherIconByName(catch.weatherIcon)),
                    contentDescription = stringResource(Res.string.weather)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    HeaderText(
                        text = temperatureUnit.getTemperature(catch.weatherTemperature)
                                + stringResource(temperatureUnit.stringRes)
                    )
                    SecondaryTextSmall(
                        text = catch.weatherPrimary.replaceFirstChar { it.uppercase() }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    SecondaryText(
                        text = stringResource(Res.string.moon_phase)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(getMoonIconByPhase(catch.weatherMoonPhase)),
                            contentDescription = stringResource(Res.string.moon_phase)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        PrimaryText(
                            text = (catch.weatherMoonPhase * 100).toInt().toString()
                                    + " " + stringResource(Res.string.percent)
                        )
                    }
                }
            }

            HorizontalDivider()

            // Pressure + Wind row
            Row(modifier = Modifier.fillMaxWidth()) {
                // Pressure column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SecondaryText(text = stringResource(Res.string.pressure))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(Res.drawable.ic_gauge),
                            contentDescription = stringResource(Res.string.pressure),
                            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.tertiary)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        PrimaryText(
                            text = pressureUnit.getPressureFromMmhg(
                                catch.weatherPressure
                            ) + " " + pressureUnit.name,
                        )
                    }
                }
                // Wind column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SecondaryText(text = stringResource(Res.string.wind))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(Res.drawable.ic_wind),
                            contentDescription = stringResource(Res.string.wind),
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        PrimaryText(
                            text = windSpeedUnit.getWindSpeed(catch.weatherWindSpeed.toDouble())
                                    + " " + windSpeedUnit.name
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            modifier = Modifier.rotate(catch.weatherWindDeg.toFloat()),
                            painter = painterResource(Res.drawable.ic_baseline_navigation_24),
                            contentDescription = stringResource(Res.string.wind),
                        )
                    }
                }
            }
        }
    }
}