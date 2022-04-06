package com.mobileprism.fishing.ui.home.notes

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.Arguments
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.place.newCatchClicked
import com.mobileprism.fishing.ui.home.views.DefaultAppBar
import com.mobileprism.fishing.ui.navigate
import com.mobileprism.fishing.ui.viewmodels.NotesViewModel
import com.mobileprism.fishing.ui.viewmodels.PlaceNoteItemUiState
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.utils.Constants.modalBottomSheetCorners
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    upPress: () -> Unit,
) {
    val viewModel: NotesViewModel = getViewModel()
    val uiState by viewModel.uiState.collectAsState()

//    val notesPreferences: NotesPreferences = get()

    val bottomState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    var bottomSheetScreen by remember { mutableStateOf(BottomSheetScreen.Sort) }

    val expandedItems by viewModel.expandedItems.collectAsState()


    ModalBottomSheetLayout(
        modifier = modifier,
        sheetState = bottomState,
        sheetShape = modalBottomSheetCorners,
        sheetContent = {
            Text(text = "Hello")
        },
    ) {
        Scaffold(
            topBar = {
                NotesAppBar(onSortClick = { })
            }
        ) {
            uiState.let { state ->
                when (state) {
                    is BaseViewState.Success -> {
                        UserPlacesList(
                            placeNotes = state.data,
                            expandedItems = expandedItems,
                            onItemExpandClick = { viewModel.onPlaceExpandItemClick(it) },
                            onItemClick = { onPlaceItemClick(navController, it) },
                            onCatchClick = { onCatchItemClick(navController, it) },
                            addNewCatch = { newCatchClicked(navController, it) }
                        )
                    }
                    is BaseViewState.Loading -> {
                        UserPlacesLoading()
                    }
                    is BaseViewState.Error -> {
                        UserPlacesError()
                    }
                }
            }
        }


    }

}

@Composable
fun UserPlacesList(
    modifier: Modifier = Modifier,
    placeNotes: List<PlaceNoteItemUiState>,
    expandedItems: List<UserMapMarker>,
    onItemExpandClick: (UserMapMarker) -> Unit,
    onItemClick: (UserMapMarker) -> Unit,
    onCatchClick: (UserCatch) -> Unit,
    addNewCatch: (UserMapMarker) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(count = placeNotes.size) { index ->
            ItemUserPlaceNote(
                placeNote = placeNotes[index],
                isExpanded = expandedItems.contains(placeNotes[index].place),
                onItemClick = { onItemClick(it) },
                onExpandItemClick = { onItemExpandClick(it) },
                onCatchClick = onCatchClick,
                addNewCatch = addNewCatch
            )
        }
    }
}

@Composable
fun UserPlacesLoading() {
    CircularProgressIndicator()
}

@Composable
fun UserPlacesError() {

}

@ExperimentalPagerApi
@Composable
fun NotesAppBar(
    modifier: Modifier = Modifier,
    onSortClick: () -> Unit
) {

    DefaultAppBar(
        modifier = modifier,
        title = stringResource(id = R.string.notes),
        actions = {
            IconButton(
                onClick = { onSortClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = Icons.Default.Sort.name
                )
            }
        }
    )
}

private fun onCatchItemClick(navController: NavController, catch: UserCatch) {
    navController.navigate(MainDestinations.CATCH_ROUTE, Arguments.CATCH to catch)
}

private fun onPlaceItemClick(navController: NavController, place: UserMapMarker) {
    navController.navigate(
        MainDestinations.PLACE_ROUTE,
        Arguments.PLACE to place
    )
}
