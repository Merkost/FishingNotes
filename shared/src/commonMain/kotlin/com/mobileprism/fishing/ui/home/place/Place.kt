package com.mobileprism.fishing.ui.home.place

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import org.jetbrains.compose.resources.stringResource
import androidx.navigation.NavController
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.advertising.AdIds
import com.mobileprism.fishing.ui.home.advertising.BannerAdvertView
import com.mobileprism.fishing.ui.home.notes.TabItem
import com.mobileprism.fishing.ui.home.views.AppTab
import com.mobileprism.fishing.ui.home.views.TabbedPager
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.ui.viewmodels.UserPlaceViewModel
import com.mobileprism.fishing.utils.Constants
import org.koin.compose.viewmodel.koinViewModel

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class
)
@Composable
fun UserPlaceScreen(backPress: () -> Unit, navController: NavController, place: UserMapMarker) {

    val viewModel: UserPlaceViewModel = koinViewModel()
    LaunchedEffect(place) {
        viewModel.setMarker(place)
    }
    LaunchedEffect(place.visible) {
        viewModel.setMarkerVisibility(place.visible)
    }
    val scaffoldState =
        rememberBottomSheetScaffoldState(
            rememberStandardBottomSheetState(
                SheetValue.Expanded
            )
        )
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showModalSheet by remember { mutableStateOf(false) }

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

    if (showModalSheet) {
        ModalBottomSheet(
            onDismissRequest = { showModalSheet = false },
            sheetState = modalBottomSheetState,
            shape = Constants.modalBottomSheetCorners,
        ) {
            NoteModalBottomSheet(viewModel = viewModel) {
                showModalSheet = false
            }
        }
    }

    BottomSheetScaffold(
            scaffoldState = scaffoldState,
            topBar = {
                PlaceTopBar(backPress, viewModel) { deleteDialogIsShowing = true }
            },
            sheetContent = {
                BannerAdvertView(
                    modifier = Modifier.navigationBarsPadding(),
                    adId = AdIds.placeBanner
                )
            },
            sheetShape = RectangleShape,
            sheetSwipeEnabled = false,
        ) {
            val userPlace = marker

            if (userPlace == null) {
                PlaceDetailSkeleton(modifier = Modifier.fillMaxSize())
            } else {
                val userCatches by viewModel.getCatchesByMarkerId(userPlace.id)
                    .collectAsState(listOf())

                val tabItems = listOf(TabItem.PlaceCatches, TabItem.Note)
                val tabs = tabItems.map { AppTab(title = stringResource(it.titleRes)) }
                val pagerState = rememberPagerState(initialPage = 0) { tabs.size }

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
                            MainDestinations.Map(isAddingNewPlace = false, place = userPlace)
                        )
                    }

                    PlaceButtonsView(
                        modifier = Modifier.padding(vertical = Spacing.lg),
                        place = userPlace,
                        navController = navController,
                        viewModel = viewModel
                    )

                    TabbedPager(
                        modifier = Modifier.fillMaxSize(),
                        tabs = tabs,
                        pagerState = pagerState
                    ) { page ->
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Top
                        ) {
                            when (page) {
                                0 -> PlaceCatchesView(
                                    catches = userCatches,
                                    onNewCatchClick = { newCatchClicked(navController, place) }
                                ) {
                                    navController.navigate(MainDestinations.Catch(it))
                                }
                                1 -> PlaceNotes(notes) { note ->
                                    viewModel.setCurrentNote(note)
                                    showModalSheet = true
                                }
                            }
                        }
                    }
                }
            }
        }
}
