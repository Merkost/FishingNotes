package com.mobileprism.fishing.ui.home.place

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.Arguments
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.advertising.AdaptiveBannerAdvertView
import com.mobileprism.fishing.ui.home.notes.TabItem
import com.mobileprism.fishing.ui.navigate
import com.mobileprism.fishing.ui.viewmodels.UserPlaceViewModel
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.utils.Constants.bottomBannerPadding
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterialApi::class,
    ExperimentalAnimationApi::class, ExperimentalPagerApi::class, ExperimentalComposeUiApi::class
)
@Composable
fun UserPlaceScreen(modifier: Modifier, backPress: () -> Unit, navController: NavController, place: UserMapMarker) {

    val viewModel: UserPlaceViewModel = getViewModel(parameters = { parametersOf(place) })
    val marker by viewModel.marker.collectAsState()
    val notes by viewModel.markerNotes.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    var deleteDialogIsShowing by remember { mutableStateOf(false) }

    if (deleteDialogIsShowing) {
        DeletePlaceDialog(place, onDismiss = { deleteDialogIsShowing = false }) {
            viewModel.deletePlace()
            deleteDialogIsShowing = false
            navController.popBackStack()
        }
    }

    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetShape = Constants.modalBottomSheetCorners,
        sheetContent = {
            NoteModalBottomSheet(
                currentNote = viewModel.currentNote.collectAsState().value,
                onNoteUpdate = viewModel::updateMarkerNotes,
                onNoteDelete = viewModel::deleteMarkerNote
            ) {
                coroutineScope.launch {
                    modalBottomSheetState.hide()
                }
            }
        }) {
        BottomSheetScaffold(
            modifier = modifier,
            scaffoldState = scaffoldState,
            topBar = {
                PlaceTopBar(
                    backPress = backPress,
                    placeVisibility = viewModel.markerVisibility.collectAsState().value,
                    onDelete = { deleteDialogIsShowing = true },
                    onVisibilityChanged = viewModel::changeVisibility
                )
            },
            sheetContent = {
                AdaptiveBannerAdvertView(adId = stringResource(R.string.place_admob_banner_id))
            },
            sheetShape = RectangleShape,
            sheetGesturesEnabled = false,
            sheetPeekHeight = 0.dp
        ) {

            val userCatches by viewModel.catchesList.collectAsState(listOf())
            val tabs = listOf(TabItem.PlaceCatches, TabItem.Note)
            val pagerState = rememberPagerState(0)

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {

                PlaceTitleView(
                    place = marker,
                    catchesAmount = userCatches.size,
                ) {
                    navController.navigate(
                        "${MainDestinations.HOME_ROUTE}/${MainDestinations.MAP_ROUTE}",
                        Arguments.PLACE to marker
                    )
                }

                PlaceButtonsView(
                    modifier = Modifier.padding(vertical = 16.dp),
                    place = place,
                    navController = navController,
                    viewModel = viewModel
                )

                PlaceTabsView(
                    tabs = tabs,
                    pagerState = pagerState
                )

                PlaceTabsContentView(
                    tabs = tabs,
                    pagerState = pagerState,
                    navController = navController,
                    catches = userCatches,
                    notes = notes,
                    onNewCatchClick = { newCatchClicked(navController, place) }
                ) { note ->
                    viewModel.setCurrentNote(note)
                    coroutineScope.launch { modalBottomSheetState.show() }
                }

                Spacer(modifier = Modifier.size(bottomBannerPadding))

            }
        }
    }
}


