package com.mobileprism.fishing.ui.home.notes

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.placeholder
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.model.datastore.NotesPreferences
import com.mobileprism.fishing.ui.Arguments
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.ui.home.views.DefaultButtonOutlinedOld
import com.mobileprism.fishing.ui.home.views.NoContentView
import com.mobileprism.fishing.ui.home.weather.navigateToAddNewPlace
import com.mobileprism.fishing.ui.navigate
import com.mobileprism.fishing.ui.utils.enums.PlacesSortValues
import com.mobileprism.fishing.ui.viewmodels.UserPlacesViewModel
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@ExperimentalAnimationApi
@Composable
fun UserPlacesScreen(
    navController: NavController,
    viewModel: UserPlacesViewModel = getViewModel(),
    notesPreferences: NotesPreferences = get()
) {
    val uiState = viewModel.uiState.collectAsState()
    val placesSortValue by notesPreferences.getPlacesSortValue.collectAsState(PlacesSortValues.Default)

    Scaffold(backgroundColor = Color.Transparent) {
        val places: List<UserMapMarker> by viewModel.currentContent.collectAsState()

        UserPlaces(
            placesState = uiState,
            places = placesSortValue.sort(places),
            userPlaceClicked = { userMarker ->
                onPlaceItemClick(userMarker, navController)
            },
            navigateToMap = {
                navController.navigate(
                    "${MainDestinations.HOME_ROUTE}/${MainDestinations.MAP_ROUTE}",
                    Arguments.PLACE to it
                )
            },
        navigateToNewPlace = { navigateToAddNewPlace(navController) })
    }
}


@ExperimentalAnimationApi
@Composable
fun UserPlaces(
    modifier: Modifier = Modifier,
    placesState: State<UiState>,
    userPlaceClicked: (UserMapMarker) -> Unit,
    navigateToMap: (UserMapMarker) -> Unit,
    places: List<UserMapMarker>,
    navigateToNewPlace: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(4.dp)
    ) {
        when (placesState.value) {
            UiState.InProgress -> {
                items(3) {
                    ItemUserPlace(
                        childModifier = Modifier.placeholder(
                            true,
                            color = Color.Gray,
                            shape = CircleShape,
                            highlight = PlaceholderHighlight.fade()
                        ),
                        place = UserMapMarker(),
                        userPlaceClicked = {},
                        navigateToMap = {}
                    )
                }
            }
            else -> {
                when {
                    places.isNotEmpty() -> {
                        items(items = places) { userPlace ->
                            ItemUserPlace(
                                place = userPlace,
                                userPlaceClicked = { userPlaceClicked(userPlace) },
                                navigateToMap = {
                                    navigateToMap(userPlace)
                                },
                            )
                        }
                    }
                    places.isEmpty() -> {
                        item {
                            NoContentView(
                                modifier = Modifier.padding(top = 128.dp),
                                text = stringResource(id = R.string.no_places_added),
                                icon = painterResource(id = R.drawable.ic_no_place_on_map)
                            )
                            Spacer(modifier = Modifier.size(16.dp))
                            DefaultButtonOutlinedOld(
                                text = stringResource(id = R.string.new_place_text),
                                onClick = navigateToNewPlace
                            )
                        }

                    }
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
