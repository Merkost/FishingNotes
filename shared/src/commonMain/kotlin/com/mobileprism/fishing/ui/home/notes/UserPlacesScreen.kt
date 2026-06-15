package com.mobileprism.fishing.ui.home.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.mobileprism.fishing.domain.entity.common.PlacesSortValues
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.model.datastore.NotesPreferences
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.components.SearchField
import com.mobileprism.fishing.ui.components.state.EmptyStateNoPlaces
import com.mobileprism.fishing.ui.components.state.PagedListScaffold
import com.mobileprism.fishing.ui.home.views.AppButton
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.ui.viewmodels.UserPlacesViewModel
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.new_place
import fishing.shared.generated.resources.search_places_hint
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserPlacesScreen(
    navController: NavController,
    viewModel: UserPlacesViewModel = koinViewModel(),
    notesPreferences: NotesPreferences = koinInject(),
) {
    val placesSortValue by notesPreferences.getPlacesSortValue
        .collectAsState(PlacesSortValues.Default)
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val lazyPagingItems = viewModel.placesPaged.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val onPlaceClick = remember<(UserMapMarker) -> Unit>(navController) {
        { marker -> navController.navigate(MainDestinations.Place(marker)) }
    }
    val onOpenOnMap = remember<(UserMapMarker) -> Unit>(navController) {
        { marker -> navController.navigate(MainDestinations.Map(isAddingNewPlace = false, place = marker)) }
    }
    val onAddPlace = remember(navController) {
        { navController.navigate(MainDestinations.Map(isAddingNewPlace = true)) }
    }

    LaunchedEffect(placesSortValue) {
        viewModel.setSortOrder(placesSortValue)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchField(
            query = searchQuery,
            onQueryChange = viewModel::onSearchQueryChanged,
            placeholder = stringResource(Res.string.search_places_hint),
            modifier = Modifier.padding(
                start = Spacing.screenH,
                end = Spacing.screenH,
                top = Spacing.sm,
            ),
        )
        PagedListScaffold(
            items = lazyPagingItems,
            isRefreshing = isRefreshing,
            onRefresh = {
                viewModel.refresh()
                lazyPagingItems.refresh()
            },
            skeleton = { PlaceItemSkeleton() },
            emptyState = {
                EmptyStateNoPlaces(
                    action = {
                        AppButton(
                            text = stringResource(Res.string.new_place),
                            onClick = onAddPlace,
                        )
                    },
                )
            },
            key = { place -> place.id },
            onDelete = { place -> viewModel.deletePlace(place) },
            itemContent = { place ->
                ItemUserPlace(
                    place = place,
                    userPlaceClicked = { onPlaceClick(place) },
                    navigateToMap = { onOpenOnMap(place) },
                )
            },
        )
    }
}
