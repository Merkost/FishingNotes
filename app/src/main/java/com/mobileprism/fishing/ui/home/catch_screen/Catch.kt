package com.mobileprism.fishing.ui.home.catch_screen

import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.mobileprism.fishing.R
import com.mobileprism.fishing.ui.viewmodels.UserCatchViewModel
import com.mobileprism.fishing.model.datastore.UserPreferences
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.model.entity.common.Progress
import com.mobileprism.fishing.model.entity.content.UserCatch
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.mappers.getMoonIconByPhase
import com.mobileprism.fishing.model.mappers.getWeatherIconByName
import com.mobileprism.fishing.ui.Arguments
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.notes.ItemUserPlace
import com.mobileprism.fishing.ui.home.place.LottieWarning
import com.mobileprism.fishing.ui.home.views.*
import com.mobileprism.fishing.ui.home.weather.PressureValues
import com.mobileprism.fishing.ui.home.weather.TemperatureValues
import com.mobileprism.fishing.ui.navigate
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.utils.time.toDateTextMonth
import com.mobileprism.fishing.utils.time.toTime
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@ExperimentalPermissionsApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun UserCatchScreen(navController: NavController, catch: UserCatch) {
    val coroutineScope = rememberCoroutineScope()

    val viewModel = getViewModel<UserCatchViewModel>()

    LaunchedEffect(key1 = catch) { viewModel.setCatch(catch) }

    val loadingState by viewModel.loadingState.collectAsState()
    val loadingDialogState = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = loadingState) {
        loadingDialogState.value = loadingState is Progress.Loading
    }

    val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    var currentBottomSheet: BottomSheetCatchScreen? by remember { mutableStateOf(null) }

    val closeSheet: () -> Unit = {
        coroutineScope.launch { bottomSheetState.hide() }
    }

    val openSheet: (BottomSheetCatchScreen) -> Unit = {
        currentBottomSheet = it
        coroutineScope.launch { bottomSheetState.show() }
    }

    if (!bottomSheetState.isVisible) {
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
        dialogSate = loadingDialogState,
        text = stringResource(R.string.saving_photos)
    )

    ModalBottomSheetLayout(
        modifier = Modifier,
        sheetShape = Constants.modalBottomSheetCorners,
        sheetState = bottomSheetState,
        sheetContent = {
            Spacer(modifier = Modifier.height(1.dp))

            currentBottomSheet?.let { currentSheet ->
                CatchModalBottomSheetContent(
                    currentScreen = currentSheet,
                    onCloseBottomSheet = closeSheet,
                    viewModel = viewModel
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CatchTopBar(
                    navController = navController,
                    catch = catch
                ) { deleteDialogIsShowing = true }
            }
        ) {
            CatchContent(
                navController = navController,
                viewModel = viewModel,
                openSheet = openSheet
            )
        }
    }
}

@Composable
fun CatchTopBar(navController: NavController, catch: UserCatch, onDeleteCatch: () -> Unit) {
    val userPreferences: UserPreferences = get()
    val is12hTime by userPreferences.use12hTimeFormat.collectAsState(initial = false)
    DefaultAppBar(
        title = stringResource(id = R.string.user_catch),
        subtitle = catch.date.toDateTextMonth() + " " + catch.date.toTime(is12hTime),
        onNavClick = { navController.popBackStack() }
    ) {
        IconButton(
            modifier = Modifier.padding(horizontal = 4.dp),
            onClick = onDeleteCatch
        ) {
            Icon(imageVector = Icons.Filled.Delete, contentDescription = "", tint = Color.White)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DeleteCatchDialog(
    catch: UserCatch,
    onDismiss: () -> Unit,
    onPositiveClick: () -> Unit
) {
    DefaultDialog(
        primaryText = String.format(stringResource(R.string.delete_catch_dialog), catch.fishType),
        secondaryText = stringResource(R.string.catch_delete_confirmantion),
        negativeButtonText = stringResource(id = R.string.No),
        onNegativeClick = onDismiss,
        positiveButtonText = stringResource(id = R.string.Yes),
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

@ExperimentalMaterialApi
@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalAnimationApi
@Composable
fun CatchContent(
    navController: NavController,
    viewModel: UserCatchViewModel,
    openSheet: (BottomSheetCatchScreen) -> Unit
) {
    val photosState = remember { mutableStateOf(listOf<Uri>()) }

    val catchState by viewModel.catch.collectAsState()

    catchState?.let { catch ->

        val placeState by viewModel.mapMarker.collectAsState()

        LaunchedEffect(key1 = catch) {
            viewModel.getMapMarker(catch.userMarkerId)
        }

        LaunchedEffect(key1 = photosState.value) {
            if (photosState.value.isNotEmpty()) {
                viewModel.updateCatchPhotos(photosState.value)
            }
        }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            CatchTitleView(
                catch = catch,
                onClick = { openSheet(BottomSheetCatchScreen.EditFishTypeAndWeightScreen) }
            )

            PhotosView(
                photos = catch.downloadPhotoLinks.map { it.toUri() },
                onEditClick = { openSheet(BottomSheetCatchScreen.EditPhotosScreen) }
            )

            placeState?.let { place ->
                ItemUserPlace(
                    place = place,
                    userPlaceClicked = {
                        onPlaceItemClick(place = it, navController = navController)
                    },
                    navigateToMap = {
                        navController.navigate(
                            "${MainDestinations.HOME_ROUTE}/${MainDestinations.MAP_ROUTE}",
                            Arguments.PLACE to place
                        )
                    },
                )
            }

            DefaultNoteView(
                note = catch.note,
                onClick = { openSheet(BottomSheetCatchScreen.EditNoteScreen) }
            )

            WayOfFishingView(
                catch = catch,
                onClick = {
                    openSheet(BottomSheetCatchScreen.EditWayOfFishingScreen)
                })

            CatchWeatherView(
                catch = catch
            )
        }
    }
}

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
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
        ConstraintLayout(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .wrapContentHeight()
                .fillMaxWidth()
        ) {
            val (title, amount, weight) = createRefs()

            HeaderText(
                modifier = Modifier.constrainAs(title) {
                    absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                    absoluteRight.linkTo(weight.absoluteLeft, 8.dp)
                    top.linkTo(parent.top)
                    width = Dimension.fillToConstraints
                },
                text = catch.fishType
            )

            HeaderText(
                modifier = Modifier.constrainAs(weight) {
                    absoluteRight.linkTo(parent.absoluteRight, 8.dp)
                    top.linkTo(parent.top)
                },
                text = "${catch.fishWeight} ${stringResource(id = R.string.kg)}"
            )

            SecondaryText(
                modifier = Modifier.constrainAs(amount) {
                    top.linkTo(title.bottom, 2.dp)
                    absoluteLeft.linkTo(title.absoluteLeft)
                },
                text = "${stringResource(id = R.string.amount)}: ${catch.fishAmount} " +
                        stringResource(id = R.string.pc)
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
        photos = catch.downloadPhotoLinks.map { it.toUri() },
        onEditClick = onEditClick
    )
}


@ExperimentalComposeUiApi
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
        ConstraintLayout(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {

            val (subtitle, rodValue, baitValue, lureValue) = createRefs()

            SubtitleWithIcon(
                modifier = Modifier.constrainAs(subtitle) {
                    top.linkTo(parent.top)
                    absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                },
                icon = R.drawable.ic_fishing_rod,
                text = stringResource(id = R.string.way_of_fishing)
            )

            SimpleUnderlineTextField(
                modifier = Modifier.constrainAs(rodValue) {
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                    top.linkTo(subtitle.bottom, 16.dp)
                    width = Dimension.fillToConstraints
                },
                text = if (catch.fishingRodType.isNotBlank()) {
                    catch.fishingRodType
                } else {
                    stringResource(id = R.string.no_rod)
                },
                label = stringResource(id = R.string.fish_rod),
                onClick = onClick
            )

            SimpleUnderlineTextField(
                modifier = Modifier.constrainAs(baitValue) {
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                    top.linkTo(rodValue.bottom, 16.dp)
                    width = Dimension.fillToConstraints
                },
                text = if (catch.fishingBait.isNotBlank()) {
                    catch.fishingBait
                } else {
                    stringResource(id = R.string.no_bait)
                },
                label = stringResource(id = R.string.bait),
                onClick = onClick
            )

            SimpleUnderlineTextField(
                modifier = Modifier.constrainAs(lureValue) {
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                    top.linkTo(baitValue.bottom, 16.dp)
                    width = Dimension.fillToConstraints
                },
                text = if (catch.fishingLure.isNotBlank()) {
                    catch.fishingLure
                } else {
                    stringResource(id = R.string.no_lure)
                },
                label = stringResource(id = R.string.lure),
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

    val weatherPrefs: WeatherPreferences = get()
    val pressureUnit by weatherPrefs.getPressureUnit.collectAsState(PressureValues.mmHg)
    val temperatureUnit by weatherPrefs.getTemperatureUnit.collectAsState(TemperatureValues.C)

    DefaultCard(
        modifier = modifier
    ) {
        ConstraintLayout(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            val (subtitle, pressText, windText, pressIcon, pressValue, windIcon, windValue, windDeg,
                primary, description, moonTile, moonIcon, moonValue) = createRefs()

            val guideline = createGuidelineFromAbsoluteLeft(0.5f)

            SubtitleWithIcon(
                modifier = Modifier.constrainAs(subtitle) {
                    top.linkTo(parent.top)
                    absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                },
                icon = R.drawable.weather_sunny,
                text = stringResource(id = R.string.weather)
            )

            Row(
                modifier = Modifier
                    .wrapContentSize()
                    .constrainAs(primary) {
                        top.linkTo(subtitle.bottom, 8.dp)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(guideline)
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(horizontal = 2.dp),
                    painter = painterResource(id = getWeatherIconByName(catch.weatherIcon)),
                    contentDescription = stringResource(id = R.string.weather)
                )

                HeaderText(
                    modifier = Modifier.padding(horizontal = 2.dp),
                    text = temperatureUnit.getTemperature(catch.weatherTemperature)
                            + stringResource(temperatureUnit.stringRes)
                )
            }

            SecondaryTextSmall(
                modifier = Modifier.constrainAs(description) {
                    top.linkTo(primary.bottom, 4.dp)
                    absoluteLeft.linkTo(primary.absoluteLeft)
                    absoluteRight.linkTo((primary.absoluteRight))
                },
                text = catch.weatherPrimary.replaceFirstChar { it.uppercase() }
            )

            SecondaryText(
                modifier = Modifier.constrainAs(moonTile) {
                    top.linkTo(primary.top)
                    absoluteLeft.linkTo(guideline)
                    absoluteRight.linkTo(parent.absoluteRight)
                },
                text = stringResource(id = R.string.moon_phase)
            )

            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .constrainAs(moonIcon) {
                        top.linkTo(moonTile.bottom, 4.dp)
                        absoluteLeft.linkTo(moonTile.absoluteLeft)
                        absoluteRight.linkTo(moonValue.absoluteLeft)
                    },
                painter = painterResource(id = getMoonIconByPhase(catch.weatherMoonPhase)),
                contentDescription = stringResource(id = R.string.moon_phase)
            )

            PrimaryText(
                modifier.constrainAs(moonValue) {
                    top.linkTo(moonIcon.top)
                    bottom.linkTo(moonIcon.bottom)
                    absoluteRight.linkTo(moonTile.absoluteRight)
                },
                text = (catch.weatherMoonPhase * 100).toInt().toString()
                        + " " + stringResource(id = R.string.percent)
            )

            SecondaryText(
                modifier = Modifier.constrainAs(pressText) {
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(guideline)
                    top.linkTo(description.bottom, 16.dp)
                },
                text = stringResource(id = R.string.pressure)
            )

            SecondaryText(
                modifier = Modifier.constrainAs(windText) {
                    absoluteLeft.linkTo(guideline)
                    absoluteRight.linkTo(parent.absoluteRight)
                    top.linkTo(pressText.top)
                },
                text = stringResource(id = R.string.wind)
            )

            Image(
                modifier = Modifier
                    .size(24.dp)
                    .constrainAs(pressIcon) {
                        top.linkTo(pressText.bottom, 4.dp)
                        absoluteLeft.linkTo(pressText.absoluteLeft)
                        absoluteRight.linkTo(pressValue.absoluteLeft, 2.dp)
                    },
                painter = painterResource(id = R.drawable.ic_gauge),
                contentDescription = stringResource(id = R.string.pressure),
                colorFilter = ColorFilter.tint(color = MaterialTheme.colors.primaryVariant)
            )

            PrimaryText(
                modifier = Modifier.constrainAs(pressValue) {
                    top.linkTo(pressIcon.top)
                    bottom.linkTo(pressIcon.bottom)
                    absoluteLeft.linkTo(pressIcon.absoluteRight, 2.dp)
                    absoluteRight.linkTo(pressText.absoluteRight)
                },
                text = pressureUnit.getPressure(
                    catch.weatherPressure
                ) + " " + pressureUnit.name,
            )

            Image(
                modifier = Modifier
                    .size(24.dp)
                    .constrainAs(windIcon) {
                        top.linkTo(pressText.bottom, 4.dp)
                        absoluteLeft.linkTo(windText.absoluteLeft)
                        absoluteRight.linkTo(windValue.absoluteLeft, 2.dp)
                    },
                painter = painterResource(id = R.drawable.ic_wind),
                contentDescription = stringResource(id = R.string.wind),
            )

            PrimaryText(
                modifier = Modifier.constrainAs(windValue) {
                    top.linkTo(windIcon.top)
                    bottom.linkTo(windIcon.bottom)
                    absoluteLeft.linkTo(windIcon.absoluteRight, 2.dp)
                    absoluteRight.linkTo(windDeg.absoluteLeft, 2.dp)
                },
                text = catch.weatherWindSpeed.toInt()
                    .toString() + " " + stringResource(id = R.string.wind_speed_units)
            )

            Icon(
                modifier = Modifier
                    .constrainAs(windDeg) {
                        top.linkTo(windIcon.top)
                        bottom.linkTo(windIcon.bottom)
                        absoluteLeft.linkTo(windValue.absoluteRight, 4.dp)
                        absoluteRight.linkTo(windText.absoluteRight)
                    }
                    .rotate(catch.weatherWindDeg.toFloat()),
                painter = painterResource(id = R.drawable.ic_baseline_navigation_24),
                contentDescription = stringResource(id = R.string.wind),
            )
        }
    }
}

private fun onPlaceItemClick(place: UserMapMarker, navController: NavController) {
    navController.navigate(
        MainDestinations.PLACE_ROUTE,
        Arguments.PLACE to place
    )
}







