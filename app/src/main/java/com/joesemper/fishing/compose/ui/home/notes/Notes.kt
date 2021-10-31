package com.joesemper.fishing.compose.ui.home.notes

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.*
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.DefaultAppBar
import com.joesemper.fishing.compose.ui.home.FabMenuItem
import com.joesemper.fishing.compose.ui.home.FabWithMenu
import com.joesemper.fishing.compose.ui.home.HomeSections
import com.joesemper.fishing.compose.ui.navigate
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.theme.primaryFigmaLightColor
import com.joesemper.fishing.ui.theme.primaryFigmaTextColor
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@Composable
fun Notes(
    onSnackClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController,
//    state: SearchState = rememberSearchState()
) {
    //val navController = rememberNavController()
    val tabs = listOf(TabItem.Places, TabItem.Catches)
    val pagerState = rememberPagerState(pageCount = tabs.size)

    Scaffold(
        topBar = {
            DefaultAppBar(
                onNavClick = { navController.popBackStack() },
                title = stringResource(id = R.string.notes)
            )
        },
        floatingActionButton = {
            FabWithMenu(
                items = listOf(
                    FabMenuItem(
                        icon = R.drawable.ic_add_catch,
                        onClick = { onAddNewCatchClick(navController) }
                    ),
                    FabMenuItem(
                        icon = R.drawable.ic_baseline_add_location_24,
                        onClick = { onAddNewPlaceClick(navController) }
                    )
                )
            )
        },
        modifier = Modifier.background(color = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(color = Color.Transparent)
        ) {
            Tabs(tabs = tabs, pagerState = pagerState)
            Box(modifier = Modifier.fillMaxSize()) {
                BackgroundImage()
                TabsContent(tabs = tabs, pagerState = pagerState, navController)
            }

        }

    }
}

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun Tabs(tabs: List<TabItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    // OR ScrollableTabRow()
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = primaryFigmaLightColor,
        contentColor = primaryFigmaTextColor,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
            )
        }) {
        tabs.forEachIndexed { index, tab ->
            // OR Tab()
            LeadingIconTab(
                icon = { Icon(painter = painterResource(id = tab.icon), contentDescription = "") },
                text = { Text(tab.title) },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
            )
        }
    }
}

@ExperimentalPagerApi
@Composable
fun TabsContent(tabs: List<TabItem>, pagerState: PagerState, navController: NavController) {
    HorizontalPager(
        state = pagerState
    ) { page ->
        tabs[page].screen(navController)
    }
}


private fun onAddNewCatchClick(navController: NavController) {
    navController.navigate(
        MainDestinations.NEW_CATCH_ROUTE,
        Arguments.PLACE to UserMapMarker()
    )
}

private fun onAddNewPlaceClick(navController: NavController) {
    val addNewPlace = true
    navController.navigate("${MainDestinations.HOME_ROUTE}/${MainDestinations.MAP_ROUTE}?${Arguments.MAP_NEW_PLACE}=${addNewPlace}")
}


@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Preview(showBackground = true)
@Composable
fun TabsContentPreview() {
    val tabs = listOf(
        TabItem.Places,
        TabItem.Catches,
    )
    val pagerState = rememberPagerState(pageCount = tabs.size)
    TabsContent(tabs = tabs, pagerState = pagerState, rememberNavController())
}




