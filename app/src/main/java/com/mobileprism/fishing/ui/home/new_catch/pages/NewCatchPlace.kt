package com.mobileprism.fishing.ui.home.new_catch.pages

import android.graphics.ImageDecoder
import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import coil.ComponentRegistry
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImageContent
import coil.compose.AsyncImagePainter
import coil.compose.LocalImageLoader
import coil.decode.Decoder
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.NewCatchMasterViewModel
import com.mobileprism.fishing.ui.home.HomeSections
import com.mobileprism.fishing.ui.home.new_catch.DateAndTimeItem
import com.mobileprism.fishing.ui.home.new_catch.NewCatchNoPlaceDialog
import com.mobileprism.fishing.ui.home.new_catch.NewCatchPlaceSelectView
import com.mobileprism.fishing.ui.home.new_catch.NewCatchPlacesState
import com.mobileprism.fishing.ui.home.views.DefaultButtonOutlined
import com.mobileprism.fishing.ui.home.views.DefaultDialog
import com.mobileprism.fishing.ui.home.views.SubtitleWithIcon
import com.mobileprism.fishing.domain.NewCatchMasterViewModel
import com.mobileprism.fishing.ui.MainActivity
import com.skydoves.landscapist.coil.CoilImage


@ExperimentalComposeUiApi
@Composable
fun NewCatchPlace(viewModel: NewCatchMasterViewModel, navController: NavController) {

    val state by viewModel.placeAndTimeState.collectAsState()

    var mapSelectInfoDialog by remember { mutableStateOf(false) }

    setMarkerListListener(state.placesListState, navController)

    if (mapSelectInfoDialog) CatchOnMapSelectInfoDialog() { mapSelectInfoDialog = false }

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

@ExperimentalComposeUiApi
@Composable
fun setMarkerListListener(markersList: NewCatchPlacesState, navController: NavController) {
    when (markersList) {
        is NewCatchPlacesState.NotReceived -> {}
        is NewCatchPlacesState.Received -> {
            if (markersList.locations.isEmpty()) {
                NewCatchNoPlaceDialog(navController)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CatchOnMapSelectInfoDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    DefaultDialog(
        secondaryText = stringResource(id = R.string.new_catch_place_on_map_tutorial),
        onDismiss = onDismiss,
        content = {
            //todo: gif tutorial
                AsyncImage(
                    model = R.drawable.add_catch_from_map_tutorial,
                    contentDescription = null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .size(250.dp),
                    contentScale = ContentScale.None,
                    filterQuality = FilterQuality.Low
                ) { state ->
                    if (state is AsyncImagePainter.State.Loading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        AsyncImageContent()
                    }
                }
        },
    )
}

