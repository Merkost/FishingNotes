package com.mobileprism.fishing.ui.home.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
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
import com.google.accompanist.placeholder.material3.fade
import com.google.accompanist.placeholder.placeholder
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.model.datastore.NotesPreferences
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.ui.home.views.DefaultButtonOutlined
import com.mobileprism.fishing.ui.home.views.NoContentView
import com.mobileprism.fishing.ui.utils.enums.PlacesSortValues
import com.mobileprism.fishing.ui.viewmodels.UserPlacesViewModel
import org.koin.compose.koinInject
import org.koin.androidx.compose.koinViewModel

@Composable
fun UserPlacesScreen(
    navController: NavController,
    viewModel: UserPlacesViewModel = koinViewModel(),
    notesPreferences: NotesPreferences = koinInject()
) {
    val uiState = viewModel.uiState.collectAsState()
    val placesSortValue by notesPreferences.getPlacesSortValue.collectAsState(PlacesSortValues.Default)

    Scaffold(containerColor = Color.Transparent) {
        val places: List<UserMapMarker> by viewModel.currentContent.collectAsState()

        UserPlaces(
            placesState = uiState,
            places = placesSortValue.sort(places),
            userPlaceClicked = { userMarker ->
                navController.navigate(MainDestinations.Place(userMarker))
            },
            navigateToMap = {
                navController.navigate(
                    MainDestinations.Map(isAddingNewPlace = false, place = it)
                )
            },
        navigateToNewPlace = {
            navController.navigate(MainDestinations.Map(isAddingNewPlace = true))
        })
    }
}


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
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(4.dp)
    ) {
        when (placesState.value) {
            UiState.InProgress -> {
                items(3) {
                    ItemUserPlace(
                        childModifier = Modifier.placeholder(
                            true,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant,
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
                            DefaultButtonOutlined(
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
