package com.joesemper.fishing.compose.ui.home.place

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
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.advertising.BannerAdvertView
import com.joesemper.fishing.compose.ui.home.notes.TabItem
import com.joesemper.fishing.compose.ui.navigate
import com.joesemper.fishing.domain.UserPlaceViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.utils.Constants
import com.joesemper.fishing.utils.Constants.bottomBannerPadding
import kotlinx.coroutines.launch
import org.koin.androidx.compose.viewModel

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterialApi::class,
    ExperimentalAnimationApi::class, ExperimentalPagerApi::class, ExperimentalComposeUiApi::class
)
@Composable
fun UserPlaceScreen(backPress: () -> Unit, navController: NavController, place: UserMapMarker) {

    val viewModel: UserPlaceViewModel by viewModel()
    LaunchedEffect(place) {
        viewModel.setMarker(place)
    }
    DisposableEffect(Unit) {
        viewModel.markerVisibility.value = place.visible
        onDispose { }
    }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    val coroutineScope = rememberCoroutineScope()
    val marker by viewModel.marker.collectAsState()
    val notes by viewModel.markerNotes.collectAsState()

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
                PlaceTopBar(backPress, viewModel)
            },
            sheetContent = {
                BannerAdvertView(adId = stringResource(R.string.place_admob_banner_id))
            },
            sheetShape = RectangleShape,
            sheetGesturesEnabled = false,
            sheetPeekHeight = 0.dp
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
                        navController.navigate("${MainDestinations.HOME_ROUTE}/${MainDestinations.MAP_ROUTE}",
                            Arguments.PLACE to userPlace)
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
                        onNewCatchClick = { newCatchClicked(navController, viewModel) }
                    ) { note ->
                        viewModel.currentNote.value = note
                        coroutineScope.launch {
                            modalBottomSheetState.animateTo(ModalBottomSheetValue.Expanded)
                        }
                    }

                    Spacer(modifier = Modifier.size(bottomBannerPadding))

                }
            }
        }
    }
}


