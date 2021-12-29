package com.joesemper.fishing.compose.ui.home.place

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.home.advertising.BannerAdvertView
import com.joesemper.fishing.compose.ui.home.notes.TabItem
import com.joesemper.fishing.domain.UserPlaceViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.utils.Constants.bottomBannerPadding
import org.koin.androidx.compose.viewModel

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterialApi::class,
    ExperimentalAnimationApi::class, ExperimentalPagerApi::class, ExperimentalComposeUiApi::class
)
@Composable
fun UserPlaceScreen(backPress: () -> Unit, navController: NavController, place: UserMapMarker?) {

    val viewModel: UserPlaceViewModel by viewModel()
    place?.let { marker ->
        viewModel.marker.value = marker

    }
    DisposableEffect(Unit) {
        viewModel.markerVisibility.value = place?.visible
        onDispose {  }
    }

    BottomSheetScaffold(
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
        viewModel.marker.value?.let { userPlace ->

            val userCatches by viewModel.getCatchesByMarkerId(userPlace.id)
                .collectAsState(listOf())

            val tabs = listOf(TabItem.PlaceCatches, TabItem.Note)
            val pagerState = rememberPagerState(0)

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {

                PlaceTitleView(
                    place = userPlace,
                    catchesAmount = userCatches.size,
                )

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
                    note = userPlace.description
                )

                Spacer(modifier = Modifier.size(bottomBannerPadding))

            }
        }
    }
}
