package com.mobileprism.fishing.compose.ui.home.new_catch.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.mobileprism.fishing.R
import com.mobileprism.fishing.compose.ui.home.HomeSections
import com.mobileprism.fishing.compose.ui.home.new_catch.DateAndTimeItem
import com.mobileprism.fishing.compose.ui.home.new_catch.NewCatchPlaceSelectView
import com.mobileprism.fishing.compose.ui.home.views.DefaultButtonOutlined
import com.mobileprism.fishing.compose.ui.home.views.DefaultDialog
import com.mobileprism.fishing.compose.ui.home.views.SubtitleWithIcon
import com.mobileprism.fishing.domain.NewCatchMasterViewModel


@ExperimentalComposeUiApi
@Composable
fun NewCatchPlace(viewModel: NewCatchMasterViewModel, navController: NavController) {

    var mapSelectInfoDialog by remember { mutableStateOf(false) }
    if (mapSelectInfoDialog) CatchOnMapSelectInfoDialog() { mapSelectInfoDialog = false }

    val state by viewModel.placeAndTimeState.collectAsState()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val (titleLocation, titleDate, field, date, button, buttonInfo) = createRefs()

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
            marker = state.place,
            markersList = state.placesListState,
            isLocationLocked = state.isLocationCocked,
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
            onClick = { navController.navigate(HomeSections.MAP.route) }
        )

        IconButton(modifier = Modifier.constrainAs(buttonInfo) {
            linkTo(button.top, button.bottom)
            absoluteLeft.linkTo(button.absoluteRight, 8.dp)
        }, onClick = { mapSelectInfoDialog = true }) {
            Icon(
                Icons.Default.Info,
                contentDescription = Icons.Default.Info.name,
                tint = MaterialTheme.colors.primaryVariant
            )
        }

        SubtitleWithIcon(
            modifier = Modifier.constrainAs(titleDate) {
                top.linkTo(button.bottom, 32.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, 16.dp)
            },
            icon = Icons.Default.AccessTime,
            text = stringResource(id = R.string.date_and_time)
        )

        DateAndTimeItem(
            modifier = Modifier.constrainAs(date) {
                top.linkTo(titleDate.bottom, 16.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            dateTime = state.date,
            onDateChange = { viewModel.setDate(it) }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CatchOnMapSelectInfoDialog(onDismiss: () -> Unit) {
    DefaultDialog(secondaryText = "Выберите место на карте и нажмите кнопку добавления нового улова",
        onDismiss = onDismiss, content = {
            //todo: gif tutorial
        }
    )
}

