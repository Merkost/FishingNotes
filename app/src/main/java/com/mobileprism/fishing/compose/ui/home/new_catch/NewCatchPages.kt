package com.mobileprism.fishing.compose.ui.home.new_catch

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.mobileprism.fishing.R
import com.mobileprism.fishing.compose.ui.home.views.DefaultButtonOutlined
import com.mobileprism.fishing.compose.ui.home.views.DefaultIconButton
import com.mobileprism.fishing.compose.ui.home.views.PhotosView
import com.mobileprism.fishing.compose.ui.home.views.SubtitleWithIcon
import com.mobileprism.fishing.domain.NewCatchMasterViewModel

typealias NewCatchScreenItem = @Composable (viewModel: NewCatchMasterViewModel, navController: NavController) -> Unit

sealed class NewCatchPage(var screen: NewCatchScreenItem) {
    @ExperimentalComposeUiApi
    class NewCatchPlacePage() : NewCatchPage(screen = { viewModel, navController ->
        NewCatchPlace(viewModel, navController)
    })

    class NewCatchFishInfoPage() : NewCatchPage(screen = { viewModel, navController ->
        NewCatchFishInfo(viewModel, navController)
    })

    class NewCatchWayOfFishingPage() : NewCatchPage(screen = { viewModel, navController ->
        NewCatchNote(viewModel, navController)
    })

    @ExperimentalComposeUiApi
    class NewCatchWeatherPage() : NewCatchPage(screen = { viewModel, navController ->
        NewCatchWeather(viewModel, navController)
    })

    @ExperimentalAnimationApi
    @ExperimentalComposeUiApi
    class NewCatchPhotosPage() : NewCatchPage(screen = { viewModel, navController ->
        NewCatchPhotos(viewModel, navController)
    })
}

@ExperimentalComposeUiApi
@Composable
fun NewCatchPlace(viewModel: NewCatchMasterViewModel, navController: NavController) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val (titleLocation, titleDate, field, date, button) = createRefs()

        SubtitleWithIcon(
            modifier = Modifier.constrainAs(titleLocation) {
                top.linkTo(parent.top, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
            },
            icon = R.drawable.ic_baseline_location_on_24,
            text = stringResource(R.string.location)
        )

        NewCatchPlaceSelectView(
            modifier = Modifier.constrainAs(field) {
                top.linkTo(titleLocation.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            marker = viewModel.currentPlace.collectAsState(),
            markersList = viewModel.markersListState.collectAsState(),
            isLocationLocked = viewModel.isLocationLocked.collectAsState().value,
            onNewPlaceSelected = { viewModel.setSelectedPlace(it) },
            onInputError = { viewModel.setPlaceInputError(it) }
        )

        DefaultButtonOutlined(
            modifier = Modifier.constrainAs(button) {
                top.linkTo(field.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            icon = painterResource(id = R.drawable.ic_baseline_map_24),
            text = stringResource(R.string.select_on_map),
            onClick = {
                // TODO: navigate to map
            }
        )

        SubtitleWithIcon(
            modifier = Modifier.constrainAs(titleDate) {
                top.linkTo(button.bottom, 32.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 32.dp)
            },
            icon = R.drawable.ic_baseline_access_time_24,
            text = "Date and time"
        )

        DateAndTimeItem(
            modifier = Modifier.constrainAs(date) {
                top.linkTo(titleDate.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            date = viewModel.catchDate.collectAsState(),
            onDateChange = { viewModel.setDate(it) }
        )
    }
}

@Composable
fun NewCatchFishInfo(viewModel: NewCatchMasterViewModel, navController: NavController) {
    ConstraintLayout(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        val (subtitleFish, subtitleRod, fish, amountAndWeight, rod) = createRefs()

        SubtitleWithIcon(
            modifier = Modifier.constrainAs(subtitleFish) {
                top.linkTo(parent.top, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
            },
            icon = R.drawable.ic_fish,
            text = stringResource(R.string.fish_catch)
        )

        FishSpecies(
            modifier = Modifier.constrainAs(fish) {
                top.linkTo(subtitleFish.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            name = viewModel.fishType.collectAsState(),
            onNameChange = { viewModel.setFishType(it) }
        )

        FishAmountAndWeightViewItem(
            modifier = Modifier.constrainAs(amountAndWeight) {
                top.linkTo(fish.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            amountState = viewModel.fishAmount.collectAsState(),
            weightState = viewModel.fishWeight.collectAsState(),
            onAmountChange = { viewModel.setFishAmount(it) },
            onWeightChange = { viewModel.setFishWeight(it) }
        )

        SubtitleWithIcon(
            modifier = Modifier.constrainAs(subtitleRod) {
                top.linkTo(amountAndWeight.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
            },
            icon = R.drawable.ic_fishing_rod,
            text = stringResource(R.string.way_of_fishing)
        )

        WayOfFishingView(
            modifier = Modifier.constrainAs(rod) {
                top.linkTo(subtitleRod.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            rodState = viewModel.rod.collectAsState(),
            biteState = viewModel.bait.collectAsState(),
            lureState = viewModel.lure.collectAsState(),
            onRodChange = { viewModel.setRod(it) },
            onBiteChange = { viewModel.setBait(it) },
            onLureChange = { viewModel.setLure(it) }
        )

    }
}

@Composable
fun NewCatchNote(viewModel: NewCatchMasterViewModel, navController: NavController) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        val (subtitle, note) = createRefs()

        SubtitleWithIcon(
            modifier = Modifier.constrainAs(subtitle) {
                top.linkTo(parent.top, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
            },
            icon = R.drawable.ic_baseline_edit_note_24,
            text = stringResource(id = R.string.note)
        )

        OutlinedTextField(
            modifier = Modifier
                .defaultMinSize(minHeight = 200.dp)
                .constrainAs(note) {
                    top.linkTo(subtitle.bottom, 16.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                    width = Dimension.fillToConstraints
                },
            singleLine = false,
            maxLines = 10,
            label = { Text(text = stringResource(id = R.string.note)) },
            value = viewModel.description.collectAsState().value,
            onValueChange = { viewModel.setNote(it) }
        )

    }
}

@ExperimentalComposeUiApi
@Composable
fun NewCatchWeather(viewModel: NewCatchMasterViewModel, navController: NavController) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        val (subtitle, description, temp, press, wind, moon, refreshButton) = createRefs()

        val guideline = createGuidelineFromAbsoluteLeft(0.5f)

        SubtitleWithIcon(
            modifier = Modifier.constrainAs(subtitle) {
                top.linkTo(parent.top, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
            },
            icon = R.drawable.weather_cloudy,
            text = stringResource(R.string.weather)
        )

        DefaultIconButton(
            modifier = Modifier.constrainAs(refreshButton) {
                absoluteRight.linkTo(parent.absoluteRight)
                top.linkTo(subtitle.top)
                bottom.linkTo(subtitle.bottom)
            },
            icon = painterResource(id = R.drawable.ic_baseline_refresh_24),
            tint = MaterialTheme.colors.primaryVariant,
            onClick = { viewModel.refreshWeatherState() }
        )

        NewCatchWeatherPrimary(
            modifier = Modifier.constrainAs(description) {
                top.linkTo(subtitle.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            weatherDescription = viewModel.weatherPrimary.collectAsState(),
            weatherIconId = viewModel.weatherIconId.collectAsState(),
            onDescriptionChange = { viewModel.setWeatherPrimary(it) },
            onIconChange = { viewModel.setWeatherIconId(it) },
            onError = { viewModel.setWeatherIsError(it) }
        )

        NewCatchTemperatureView(
            modifier = Modifier.constrainAs(temp) {
                top.linkTo(description.bottom, 8.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(guideline, 4.dp)
                width = Dimension.fillToConstraints
            },
            temperature = viewModel.weatherTemperature.collectAsState(),
            onTemperatureChange = { viewModel.setWeatherTemperature(it) },
            onError = { viewModel.setWeatherIsError(it) }
        )

        NewCatchPressureView(
            modifier = Modifier.constrainAs(press) {
                top.linkTo(temp.top)
                absoluteLeft.linkTo(guideline, 4.dp)
                absoluteRight.linkTo(parent.absoluteRight)
                width = Dimension.fillToConstraints
            },
            pressure = viewModel.weatherPressure.collectAsState(),
            onPressureChange = { viewModel.setWeatherPressure(it) },
            onError = { viewModel.setWeatherIsError(it) }
        )

        NewCatchWindView(
            modifier = Modifier.constrainAs(wind) {
                top.linkTo(press.bottom, 8.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(guideline, 4.dp)
                width = Dimension.fillToConstraints
            },
            wind = viewModel.weatherWindSpeed.collectAsState(),
            windDeg = viewModel.weatherWindDeg.collectAsState(),
            onWindChange = { viewModel.setWeatherWindSpeed(it) },
            onError = { viewModel.setWeatherIsError(it) }
        )

        NewCatchMoonView(
            modifier = Modifier.constrainAs(moon) {
                top.linkTo(wind.top)
                absoluteLeft.linkTo(guideline, 4.dp)
                absoluteRight.linkTo(parent.absoluteRight)
                width = Dimension.fillToConstraints
            },
            moonPhase = viewModel.weatherMoonPhase.collectAsState()
        )
    }
}

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
fun NewCatchPhotos(viewModel: NewCatchMasterViewModel, navController: NavController) {

    ConstraintLayout(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
    ) {
        val (subtitle, photosView) = createRefs()

        val photos = viewModel.photos.collectAsState()

        SubtitleWithIcon(
            modifier = Modifier.constrainAs(subtitle) {
                top.linkTo(parent.top, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
            },
            icon = R.drawable.ic_baseline_photo_24,
            text = stringResource(R.string.photos)
        )

        PhotosView(
            modifier = Modifier.constrainAs(photosView) {
                top.linkTo(subtitle.bottom, 32.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            photos = photos.value,
            onEditClick = { viewModel.addPhotoState.value = true }
        )
    }

}
