package com.joesemper.fishing.compose.ui.home.notes

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.joesemper.fishing.R
import com.joesemper.fishing.model.datastore.NotesPreferences
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.navigate
import com.joesemper.fishing.compose.ui.utils.PlacesSortValues
import com.joesemper.fishing.domain.UserPlacesViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@ExperimentalAnimationApi
@Composable
fun UserPlacesScreen(
    navController: NavController,
    viewModel: UserPlacesViewModel = getViewModel()
) {
    val notesPreferences: NotesPreferences = get()
    val placesSortValue by notesPreferences.placesSortValue
        .collectAsState(PlacesSortValues.Default)

    Scaffold(backgroundColor = Color.Transparent) {
        val places: List<UserMapMarker>? by viewModel.currentContent.collectAsState()
        Crossfade(places) { animatedUiState ->
            if (animatedUiState != null) {
                UserPlaces(
                    places = placesSortValue.sort(animatedUiState),
                    userPlaceClicked = { userMarker ->
                        onPlaceItemClick(userMarker, navController)
                    },
                    navigateToMap = {
                        navController.navigate("${MainDestinations.HOME_ROUTE}/${MainDestinations.MAP_ROUTE}",
                            Arguments.PLACE to it)
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
    navigateToMap: (UserMapMarker) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        when {
            places.isNotEmpty() -> {
                items(items = places) { userPlace ->
                    ItemUserPlace(
                        place = userPlace,
                        userPlaceClicked = { userPlaceClicked(userPlace) }
                    ) {
                        navigateToMap(userPlace)
                    }

                }
            }
            places.isEmpty() -> {
                item {
                    NoElementsView(
                        mainText = stringResource(R.string.no_places_added),
                        secondaryText = stringResource(R.string.new_place_text),
                        onClickAction = {  }
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
