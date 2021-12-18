package com.joesemper.fishing.compose.ui.home.place

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.joesemper.fishing.compose.ui.home.notes.TabItem
import com.joesemper.fishing.domain.UserPlaceViewModel
import com.joesemper.fishing.model.entity.content.UserMapMarker
import org.koin.androidx.compose.getViewModel

@ExperimentalPagerApi
@OptIn(ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun UserPlaceScreen(navController: NavController, place: UserMapMarker?) {

    val viewModel = getViewModel<UserPlaceViewModel>()
    place?.let { viewModel.marker.value = it }

    Scaffold(
        topBar = {
            PlaceTopBar(navController = navController)
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
