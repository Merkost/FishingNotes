package com.mobileprism.fishing.ui.home.new_catch.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.viewmodels.NewCatchMasterViewModel
import com.mobileprism.fishing.ui.home.new_catch.DateAndTimeItem
import com.mobileprism.fishing.ui.home.new_catch.NewCatchNoPlaceDialog
import com.mobileprism.fishing.ui.home.new_catch.NewCatchPlaceSelectView
import com.mobileprism.fishing.ui.home.new_catch.NewCatchPlacesState
import com.mobileprism.fishing.ui.home.views.DefaultButtonOutlined
import com.mobileprism.fishing.ui.home.views.DefaultDialog
import com.mobileprism.fishing.ui.home.views.SubtitleWithIcon

@Composable
fun NewCatchPlace(viewModel: NewCatchMasterViewModel, navController: NavController) {

    val state by viewModel.placeAndTimeState.collectAsState()

    var mapSelectInfoDialog by remember { mutableStateOf(false) }

    setMarkerListListener(state.placesListState, navController)

    if (mapSelectInfoDialog) CatchOnMapSelectInfoDialog() { mapSelectInfoDialog = false }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SubtitleWithIcon(
            modifier = Modifier.padding(top = 16.dp, start = 16.dp),
            icon = Res.drawable.ic_baseline_location_on_24,
            text = stringResource(Res.string.location)
        )

        NewCatchPlaceSelectView(
            modifier = Modifier.padding(top = 16.dp),
            marker = state.place,
            markersList = state.placesListState,
            isLocationLocked = state.isLocationCocked,
            onNewPlaceSelected = { viewModel.setSelectedPlace(it) },
            onInputError = { viewModel.setPlaceInputError(it) }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultButtonOutlined(
                icon = painterResource(Res.drawable.ic_baseline_map_24),
                text = stringResource(Res.string.select_on_map),
                onClick = { navController.navigate(MainDestinations.Map(isAddingNewPlace = false)) }
            )

            IconButton(onClick = { mapSelectInfoDialog = true }) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = Icons.Default.Info.name,
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        SubtitleWithIcon(
            modifier = Modifier.padding(top = 32.dp, start = 16.dp),
            icon = Icons.Default.AccessTime,
            text = stringResource(Res.string.date_and_time)
        )

        DateAndTimeItem(
            modifier = Modifier.padding(top = 16.dp),
            dateTime = state.date,
            onDateChange = { viewModel.setDate(it) }
        )
    }
}

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

@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class)
@Composable
fun CatchOnMapSelectInfoDialog(onDismiss: () -> Unit) {
    DefaultDialog(
        secondaryText = stringResource(Res.string.new_catch_place_on_map_tutorial),
        onDismiss = onDismiss,
        content = {
            //todo: gif tutorial
                SubcomposeAsyncImage(
                    model = Res.getUri("drawable/add_catch_from_map_tutorial.gif"),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .size(250.dp),
                    contentScale = ContentScale.None,
                    filterQuality = FilterQuality.Low,
                    loading = {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                )
        },
    )
}

