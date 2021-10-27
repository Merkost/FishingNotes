package com.joesemper.fishing.compose.ui.home.notes.user_places

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.notes.ItemAdd
import com.joesemper.fishing.compose.ui.home.notes.ItemUserPlace
import com.joesemper.fishing.compose.ui.home.notes.NoElementsView
import com.joesemper.fishing.compose.ui.navigate
import com.joesemper.fishing.domain.UserPlacesViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import org.koin.androidx.compose.getViewModel

@ExperimentalAnimationApi
@Composable
fun UserPlacesScreen(
    navController: NavController,
    viewModel: UserPlacesViewModel = getViewModel<UserPlacesViewModel>()
) {
    Scaffold() {
        val places by viewModel.currentContent.collectAsState()
        Crossfade(places) { animatedUiState ->
            UserPlaces(
                places = animatedUiState,
                addNewPlaceClicked = { onAddNewPlaceClick(navController) },
                userPlaceClicked = { userMarker ->
                    onPlaceItemClick(userMarker, navController)
                }
            )
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun UserPlaces(
    places: List<UserMapMarker>,
    addNewPlaceClicked: () -> Unit,
    userPlaceClicked: (UserMapMarker) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            ItemAdd(
                icon = painterResource(R.drawable.ic_baseline_add_location_24),
                text = stringResource(R.string.add_new_place),
                onClickAction = addNewPlaceClicked
            )
        }
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
                        onClickAction = addNewPlaceClicked
                    )
                }

            }
        }

    }
}


private fun onAddNewPlaceClick(navController: NavController) {
    val addNewPlace = true
    navController.navigate("${MainDestinations.HOME_ROUTE}/${MainDestinations.MAP_ROUTE}?${Arguments.MAP_NEW_PLACE}=${addNewPlace}")
}

private fun onPlaceItemClick(place: UserMapMarker, navController: NavController) {
    navController.navigate(
        MainDestinations.PLACE_ROUTE,
        Arguments.PLACE to place
    )


    /*val action =
        NotesFragmentDirections.actionNotesFragmentToUserPlaceFragment(place)
    findNavController().navigate(action)*/
}
