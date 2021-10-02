package com.joesemper.fishing.compose.ui.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.*
import com.joesemper.fishing.compose.ui.TabItem
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
        topBar = { AppBar(navController) },
    ) {
        Column {
            Tabs(tabs = tabs, pagerState = pagerState)
            TabsContent(tabs = tabs, pagerState = pagerState, navController)
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
        backgroundColor = Color.White,
        contentColor = Color.Black,
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
    HorizontalPager(state = pagerState) { page ->
        tabs[page].screen(navController)
    }
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


