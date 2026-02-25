package com.mobileprism.fishing.ui.home.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.fade
import com.google.accompanist.placeholder.placeholder
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.model.datastore.NotesPreferences
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.views.DefaultButtonOutlined
import com.mobileprism.fishing.ui.home.views.ErrorView
import com.mobileprism.fishing.ui.home.views.NoContentView
import com.mobileprism.fishing.domain.entity.common.PlacesSortValues
import com.mobileprism.fishing.ui.viewmodels.UserPlacesViewModel
import org.koin.compose.koinInject
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPlacesScreen(
    navController: NavController,
    viewModel: UserPlacesViewModel = koinViewModel(),
    notesPreferences: NotesPreferences = koinInject()
) {
    val placesSortValue by notesPreferences.getPlacesSortValue.collectAsState(PlacesSortValues.Default)
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val lazyPagingItems = viewModel.placesPaged.collectAsLazyPagingItems()

    val userPlaceClicked = remember<(UserMapMarker) -> Unit>(navController) {
        { userMarker -> navController.navigate(MainDestinations.Place(userMarker)) }
    }
    val navigateToMap = remember<(UserMapMarker) -> Unit>(navController) {
        { navController.navigate(MainDestinations.Map(isAddingNewPlace = false, place = it)) }
    }
    val navigateToNewPlace = remember(navController) {
        { navController.navigate(MainDestinations.Map(isAddingNewPlace = true)) }
    }

    // Update sort order when preference changes
    viewModel.setSortOrder(placesSortValue)

    Scaffold(containerColor = Color.Transparent) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                viewModel.refresh()
                lazyPagingItems.refresh()
            }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(4.dp)
            ) {
                when (lazyPagingItems.loadState.refresh) {
                    is LoadState.Loading -> {
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
                    is LoadState.Error -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 128.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                ErrorView()
                                DefaultButtonOutlined(
                                    text = stringResource(R.string.retry),
                                    onClick = { lazyPagingItems.refresh() }
                                )
                            }
                        }
                    }
                    is LoadState.NotLoading -> {
                        if (lazyPagingItems.itemCount == 0) {
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
                        } else {
                            items(
                                count = lazyPagingItems.itemCount,
                                key = { lazyPagingItems.peek(it)?.id ?: "item_$it" }
                            ) { index ->
                                lazyPagingItems[index]?.let { userPlace ->
                                    ItemUserPlace(
                                        place = userPlace,
                                        userPlaceClicked = { userPlaceClicked(userPlace) },
                                        navigateToMap = { navigateToMap(userPlace) },
                                    )
                                }
                            }
                        }
                    }
                }

                // Append loading indicator
                if (lazyPagingItems.loadState.append is LoadState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
