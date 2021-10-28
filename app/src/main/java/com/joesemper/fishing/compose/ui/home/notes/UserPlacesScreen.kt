package com.joesemper.fishing.compose.ui.home.notes

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.navigate
import com.joesemper.fishing.domain.UserPlacesViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.theme.backgroundGreenColor
import org.koin.androidx.compose.getViewModel

@ExperimentalAnimationApi
@Composable
fun UserPlacesScreen(
    navController: NavController,
    viewModel: UserPlacesViewModel = getViewModel()
) {
    Scaffold(modifier = Modifier.background(color = Color.Transparent)) {
        Image(
            modifier = Modifier
                .zIndex(-1.0f)
                .fillMaxSize(),
            colorFilter = ColorFilter.tint(
                backgroundGreenColor,
                BlendMode.ColorDodge
            ),
            painter = painterResource(id = R.drawable.ic_pattern_background),
            contentDescription = "",
            alpha = 0.1f,
            contentScale = ContentScale.FillWidth
        )
        val places: List<UserMapMarker>? by viewModel.currentContent.collectAsState()
        Crossfade(places) { animatedUiState ->
            if (animatedUiState != null) {
                UserPlaces(
                    places = animatedUiState,
                    userPlaceClicked = { userMarker ->
                        onPlaceItemClick(userMarker, navController)
                    }
                )
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun UserPlaces(
    places: List<UserMapMarker>,
    userPlaceClicked: (UserMapMarker) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        when {
            places.isNotEmpty() -> {
                items(items = places) { userPlace ->
                    ItemUserPlace(
                        place = userPlace
                    ) { userPlaceClicked(userPlace) }
                }
            }
            places.isEmpty() -> {
                item {
                    NoElementsView(
                        mainText = stringResource(R.string.no_places_added),
                        secondaryText = stringResource(R.string.new_place_text),
                        onClickAction = { }
                    )
                }

            }
        }

    }
}

private fun onPlaceItemClick(place: UserMapMarker, navController: NavController) {
    navController.navigate(
        MainDestinations.PLACE_ROUTE,
        Arguments.PLACE to place
    )
}
