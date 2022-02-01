package com.mobileprism.fishing.compose.ui.home.new_catch

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.mobileprism.fishing.R
import com.mobileprism.fishing.compose.ui.home.views.DefaultAppBar
import com.mobileprism.fishing.compose.ui.home.views.DefaultButton
import com.mobileprism.fishing.compose.ui.home.views.DefaultButtonFilled
import com.mobileprism.fishing.compose.ui.home.views.DefaultButtonOutlined
import com.mobileprism.fishing.domain.NewCatchMasterViewModel
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import kotlinx.coroutines.launch
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf

@ExperimentalPagerApi
@Composable
fun NewCatchMasterScreen(
    upPress: () -> Unit,
    receivedPlace: UserMapMarker?,
    navController: NavController
) {
    val viewModel: NewCatchMasterViewModel by viewModel {
        parametersOf(
            if (receivedPlace != null) {
                ReceivedPlaceState.Received(receivedPlace)
            } else {
                ReceivedPlaceState.NotReceived
            }
        )
    }

    val pagerState = rememberPagerState(0)
    val pages = remember {
        listOf(
            NewCatchPage.NewCatchPlacePage(),
            NewCatchPage.NewCatchFishInfoPage(),
            NewCatchPage.NewCatchWayOfFishingPage(),
            NewCatchPage.NewCatchWeatherPage(),
            NewCatchPage.NewCatchPhotosPage()
        )
    }

    Scaffold(
        topBar = {
            DefaultAppBar(title = stringResource(id = R.string.new_catch))
        }
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (pager, buttons) = createRefs()

            NewCatchPager(
                modifier = Modifier.constrainAs(pager) {
                    top.linkTo(parent.top)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                    bottom.linkTo(buttons.top)
                    height = Dimension.fillToConstraints
                    width = Dimension.fillToConstraints
                },
                navController = navController,
                viewModel = viewModel,
                pagerState = pagerState,
                pages = pages
            )

            NewCatchButtons(
                modifier = Modifier.constrainAs(buttons) {
                    bottom.linkTo(parent.bottom)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                },
                pagerState = pagerState,
                onFinishClick = { },
                onCloseClick = upPress
            )
        }
    }
}

@ExperimentalPagerApi
@Composable
fun NewCatchPager(
    modifier: Modifier = Modifier,
    navController: NavController,
    pages: List<NewCatchPage>,
    viewModel: NewCatchMasterViewModel,
    pagerState: PagerState
) {

    HorizontalPager(
        modifier = modifier,
        count = pages.size,
        state = pagerState
    ) { page ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, _ ->
                            change.consumeAllChanges()
                        }
                    )
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            pages[page].screen(viewModel, navController)
        }

    }
}

@ExperimentalPagerApi
@Composable
fun NewCatchButtons(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    onFinishClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = remember(pagerState.currentPage) {
        pagerState.currentPage == (pagerState.pageCount - 1)
    }
    val isFirstPage = remember(pagerState.currentPage) {
        pagerState.currentPage == 0
    }

    ConstraintLayout(
        modifier = modifier
            .padding(vertical = 32.dp, horizontal = 16.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        val (next, previous, close) = createRefs()

        DefaultButtonFilled(
            modifier = Modifier.constrainAs(next) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            text = if (isLastPage) {
                stringResource(R.string.finish)
            } else {
                stringResource(R.string.next)
            },
            onClick = {
                coroutineScope.launch {
                    if (isLastPage) {
                        onFinishClick()
                    } else {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            }
        )

        DefaultButtonOutlined(
            modifier = Modifier.constrainAs(previous) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                absoluteRight.linkTo(next.absoluteLeft, 16.dp)
            },
            enabled = !isFirstPage,
            text = stringResource(R.string.previous),
            onClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            }
        )

        DefaultButton(
            modifier = Modifier.constrainAs(close) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                absoluteLeft.linkTo(parent.absoluteLeft)
            },
            text = stringResource(R.string.close),
            onClick = onCloseClick
        )

    }
}
