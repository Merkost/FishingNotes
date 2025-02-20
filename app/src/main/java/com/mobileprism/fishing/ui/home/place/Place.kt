package com.mobileprism.fishing.ui.home.place

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.mobileprism.fishing.ui.home.advertising.BannerAdvertView
import com.mobileprism.fishing.ui.home.notes.TabItem
import com.mobileprism.fishing.ui.navigate
import com.mobileprism.fishing.ui.viewmodels.UserPlaceViewModel
import com.mobileprism.fishing.utils.Constants
import com.mobileprism.fishing.utils.Constants.bottomBannerPadding
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterialApi::class,
    ExperimentalAnimationApi::class, ExperimentalPagerApi::class, ExperimentalComposeUiApi::class
)
@Composable
fun UserPlaceScreen(backPress: () -> Unit, navController: NavController, place: UserMapMarker) {

    val viewModel: UserPlaceViewModel = koinViewModel()
    LaunchedEffect(place) {
        viewModel.setMarker(place)
    }
    DisposableEffect(Unit) {
        viewModel.markerVisibility.value = place.visible
        onDispose { }
    }
    val scaffoldState =
        rememberBottomSheetScaffoldState(BottomSheetState(BottomSheetValue.Expanded))
    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    val coroutineScope = rememberCoroutineScope()
    val marker by viewModel.marker.collectAsState()
    val notes by viewModel.markerNotes.collectAsState()


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
            NoteModalBottomSheet(viewModel = viewModel) {
                coroutineScope.launch {
                    modalBottomSheetState.hide()
                }
            }
        }) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            topBar = {
                PlaceTopBar(backPress, viewModel) { deleteDialogIsShowing = true }
            },
            sheetContent = {
                BannerAdvertView(
                    modifier = Modifier.navigationBarsPadding(),
                    adId = stringResource(R.string.place_admob_banner_id)
                )
            },
            sheetShape = RectangleShape,
            sheetGesturesEnabled = false,
        ) {
            marker?.let { userPlace ->

                val userCatches by viewModel.getCatchesByMarkerId(userPlace.id)
                    .collectAsState(listOf())

                val tabs = listOf(TabItem.PlaceCatches, TabItem.Note)
                val pagerState = rememberPagerState(0)

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {

                    PlaceTitleView(
                        place = userPlace,
                        catchesAmount = userCatches.size,
                    ) {
                        navController.navigate(
                            "${MainDestinations.HOME_ROUTE}/${MainDestinations.MAP_ROUTE}",
                            Arguments.PLACE to userPlace
                        )
                    }

                    PlaceButtonsView(
                        modifier = Modifier.padding(vertical = 16.dp),
                        place = userPlace,
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
                        viewModel.currentNote.value = note
                        coroutineScope.launch {
                            modalBottomSheetState.show()
                        }
                    }

                    Spacer(modifier = Modifier.size(bottomBannerPadding))
                }
            }
        }
    }
}


