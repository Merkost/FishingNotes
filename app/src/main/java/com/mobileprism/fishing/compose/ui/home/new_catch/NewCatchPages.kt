package com.mobileprism.fishing.compose.ui.home.new_catch

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
        NewCatchWayOfFishing(viewModel, navController)
    })

    class NewCatchWeatherPage() : NewCatchPage(screen = { viewModel, navController ->
        NewCatchWeather(viewModel, navController)
    })

    class NewCatchPhotosPage() : NewCatchPage(screen = { viewModel, navController ->
        NewCatchPhotos(viewModel, navController)
    })
}

@ExperimentalComposeUiApi
@Composable
fun NewCatchPlace(viewModel: NewCatchMasterViewModel, navController: NavController) {
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (titleLocation, titleDate, field, date, button) = createRefs()

        SubtitleWithIcon(
            modifier = Modifier.constrainAs(titleLocation) {
                top.linkTo(parent.top, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 32.dp)
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
                top.linkTo(button.bottom, 48.dp)
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
            .fillMaxSize()
            .padding(horizontal = 32.dp)
    ) {
        val (subtitleFish, subtitleNote, fish, amountAndWeight, note) = createRefs()

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
            modifier = Modifier.constrainAs(subtitleNote) {
                top.linkTo(amountAndWeight.bottom, 48.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
            },
            icon = R.drawable.ic_baseline_edit_note_24,
            text = stringResource(id = R.string.note)
        )

        OutlinedTextField(
            modifier = Modifier.constrainAs(note) {
                top.linkTo(subtitleNote.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
                width = Dimension.fillToConstraints
            },
            singleLine = false,
            maxLines = 7,
            label = { Text(text = stringResource(id = R.string.note)) },
            value = viewModel.description.collectAsState().value,
            onValueChange = { viewModel.setNote(it) }
        )

    }
}

@Composable
fun NewCatchWayOfFishing(viewModel: NewCatchMasterViewModel, navController: NavController) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
    ) {
        val (subtitle, fields) = createRefs()
        SubtitleWithIcon(
            modifier = Modifier.constrainAs(subtitle) {
                top.linkTo(parent.top, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
            },
            icon = R.drawable.ic_fishing_rod,
            text = stringResource(R.string.way_of_fishing)
        )

        WayOfFishingView(
            modifier = Modifier.constrainAs(fields) {
                top.linkTo(subtitle.bottom, 16.dp)
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
fun NewCatchWeather(viewModel: NewCatchMasterViewModel, navController: NavController) {
    Text(text = "Weather")
}

@Composable
fun NewCatchPhotos(viewModel: NewCatchMasterViewModel, navController: NavController) {
    Text(text = "Photos")
}