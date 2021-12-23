package com.joesemper.fishing.compose.ui.home.place

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.joesemper.fishing.compose.ui.home.notes.TabItem
import com.joesemper.fishing.domain.UserPlaceViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import org.koin.androidx.compose.getViewModel
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
        viewModel.markerVisibility.value = place?.isVisible
        onDispose {  }
    }

    Scaffold(
        topBar = {
            PlaceTopBar(backPress, viewModel)
        }
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

            }
        }
    }
}
