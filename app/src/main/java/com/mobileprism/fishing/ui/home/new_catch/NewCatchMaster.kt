package com.mobileprism.fishing.ui.home.new_catch

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.advertising.BannerAdvertView
import com.mobileprism.fishing.ui.home.advertising.showInterstitialAd
import com.mobileprism.fishing.ui.home.new_catch.pages.NewCatchPage
import com.mobileprism.fishing.ui.home.place.LottieWarning
import com.mobileprism.fishing.ui.home.views.DefaultAppBar
import com.mobileprism.fishing.ui.home.views.DefaultButton
import com.mobileprism.fishing.ui.home.views.DefaultButtonFilled
import com.mobileprism.fishing.ui.home.views.DefaultDialog
import com.mobileprism.fishing.ui.home.views.ModalLoadingDialog
import com.mobileprism.fishing.ui.viewmodels.NewCatchMasterViewModel
import com.mobileprism.fishing.ui.viewstates.NewCatchViewState
import com.mobileprism.fishing.utils.Constants.MAX_PHOTOS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.absoluteValue
import kotlin.math.sign

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewCatchMasterScreen(
    receivedPlace: UserMapMarker?,
    navController: NavController,
    upPress: () -> Unit,
) {
    val viewModel: NewCatchMasterViewModel = koinViewModel(parameters = {
        parametersOf(
            if (receivedPlace != null) {
                ReceivedPlaceState.Received(receivedPlace)
            } else {
                ReceivedPlaceState.NotReceived
            }
        )
    })

    val coroutineScope = rememberCoroutineScope()

    val pages = remember {
        listOf(
            NewCatchPage.NewCatchPlacePage(),
            NewCatchPage.NewCatchFishInfoPage(),
            NewCatchPage.NewCatchWayOfFishingPage(),
            NewCatchPage.NewCatchWeatherPage(),
            NewCatchPage.NewCatchPhotosPage()
        )
    }

    val pagerState = androidx.compose.foundation.pager.rememberPagerState { pages.size }

    var exitDialogIsShowing by remember { mutableStateOf(false) }

    if (exitDialogIsShowing) {
        CancelNewCatchDialog(onDismiss = { exitDialogIsShowing = false }) {
            exitDialogIsShowing = false; upPress()
        }
    }

    BackHandler {
        val currentPage = pagerState.currentPage
        if (currentPage != 0) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(currentPage - 1)
            }
        } else {
            exitDialogIsShowing = true
        }
    }

    val loadingDialogState = remember { mutableStateOf(false) }
    val isAdLoaded = remember { mutableStateOf(false) }

    val context = LocalContext.current

    val onFinish = {
        if (viewModel.photos.value.size <= MAX_PHOTOS) {
            viewModel.saveNewCatch()
        } else {
            SnackbarManager.showMessage(R.string.max_photos_allowed)
        }
    }

    val uiState = viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = uiState.value) {
        uiState.value.let {
            when (it) {
                NewCatchViewState.Editing -> {}
                NewCatchViewState.Complete -> {
                    loadingDialogState.value = false
                    SnackbarManager.showMessage(R.string.catch_added_successfully)
                    showInterstitialAd(
                        context = context,
                        onAdLoaded = {
                            upPress()
                        }
                    )
                }

                NewCatchViewState.SavingNewCatch -> {
                    loadingDialogState.value = true
                }

                is NewCatchViewState.Error -> {
                    loadingDialogState.value = false
                    SnackbarManager.showMessage(R.string.error_occured)
                    upPress()
                }
            }
        }

    }

    ModalLoadingDialog(
        visible = loadingDialogState.value,
        text = stringResource(id = R.string.saving_new_catch)
    )

    val skipAvailable by viewModel.skipAvailable.collectAsState()

    Scaffold(
        topBar = {
            DefaultAppBar(
                title = stringResource(id = R.string.new_catch),
                onNavClick = { exitDialogIsShowing = true },
                actions = {
                    IconButton(
                        onClick = {
                            when (skipAvailable) {
                                true -> onFinish()
                                else -> SnackbarManager.showMessage(R.string.new_catch_skip_tutor)
                            }
                        },
                        enabled = true
                    ) {
                        Icon(Icons.Default.Check, Icons.Default.Check.name)
                    }
                }
            )
        }
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            val (pager, buttons, advertisement) = createRefs()

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
                    bottom.linkTo(advertisement.top)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                },
                pagerState = pagerState,
                onFinishClick = {
                    onFinish()
                },
                onNextClick = {
                    handlePagerNextClick(
                        coroutineScope = coroutineScope,
                        viewModel = viewModel,
                        pagerState = pagerState
                    )
                },
                onPreviousClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
            )

            BannerAdvertView(
                modifier = Modifier
                    .constrainAs(advertisement) {
                        bottom.linkTo(parent.bottom)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(parent.absoluteRight)
                    }
                    .navigationBarsPadding(),
                adId = stringResource(R.string.new_catch_admob_banner_id),
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewCatchPager(
    modifier: Modifier = Modifier,
    navController: NavController,
    pages: List<NewCatchPage>,
    viewModel: NewCatchMasterViewModel,
    pagerState: androidx.compose.foundation.pager.PagerState
) {

    androidx.compose.foundation.pager.HorizontalPager(
        modifier = modifier,
        state = pagerState
    ) { page ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, _ ->
                            change.changedToDownIgnoreConsumed()
                            change.changedToUpIgnoreConsumed()
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewCatchButtons(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    onFinishClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
) {
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
        val (next, previous, skip, indicator) = createRefs()

        MyHorizontalPagerIndicator(
            modifier = Modifier.constrainAs(indicator) {
                top.linkTo(next.top)
                bottom.linkTo(next.bottom)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            activeColor = MaterialTheme.colorScheme.tertiary,
            pagerState = pagerState
        )

        DefaultButtonFilled(
            modifier = Modifier.constrainAs(next) {
                top.linkTo(parent.top, 8.dp)
                bottom.linkTo(parent.bottom)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            text = if (isLastPage) {
                stringResource(R.string.finish)
            } else {
                stringResource(R.string.next)
            },
            onClick = {
                if (isLastPage) {
                    onFinishClick()
                } else {
                    onNextClick()
                }
            }
        )

        DefaultButton(
            modifier = Modifier.constrainAs(previous) {
                top.linkTo(next.top)
                bottom.linkTo(next.bottom)
                absoluteLeft.linkTo(parent.absoluteLeft)
            },
            enabled = !isFirstPage,
            text = stringResource(R.string.previous),
            onClick = onPreviousClick
        )

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MyHorizontalPagerIndicator(
    pagerState: PagerState,
    pageCount: Int = pagerState.pageCount,
    modifier: Modifier = Modifier,
    pageIndexMapping: (Int) -> Int = { it },
    activeColor: Color = MaterialTheme.colorScheme.onSurface,
    inactiveColor: Color = activeColor.copy(alpha = 0.38f),
    indicatorWidth: Dp = 8.dp,
    indicatorHeight: Dp = indicatorWidth,
    spacing: Dp = indicatorWidth,
    indicatorShape: Shape = CircleShape,
) {

    val indicatorWidthPx = LocalDensity.current.run { indicatorWidth.roundToPx() }
    val spacingPx = LocalDensity.current.run { spacing.roundToPx() }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val indicatorModifier = Modifier
                .size(width = indicatorWidth, height = indicatorHeight)
                .background(color = inactiveColor, shape = indicatorShape)

            repeat(pageCount) {
                Box(indicatorModifier)
            }
        }

        Box(
            Modifier
                .offset {
                    val position = pageIndexMapping(pagerState.currentPage)
                    val offset = pagerState.currentPageOffsetFraction
                    val next = pageIndexMapping(pagerState.currentPage + offset.sign.toInt())
                    val scrollPosition = ((next - position) * offset.absoluteValue + position)
                        .coerceIn(
                            0f,
                            (pageCount - 1)
                                .coerceAtLeast(0)
                                .toFloat()
                        )

                    IntOffset(
                        x = ((spacingPx + indicatorWidthPx) * scrollPosition).toInt(),
                        y = 0
                    )
                }
                .size(width = indicatorWidth, height = indicatorHeight)
                .then(
                    if (pageCount > 0) Modifier.background(
                        color = activeColor,
                        shape = indicatorShape,
                    )
                    else Modifier
                )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun handlePagerNextClick(
    coroutineScope: CoroutineScope,
    pagerState: androidx.compose.foundation.pager.PagerState,
    viewModel: NewCatchMasterViewModel
) {
    coroutineScope.launch {
        when (pagerState.currentPage) {
            0 -> {
                if (viewModel.placeAndTimeState.value.isInputCorrect) {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                } else {
                    SnackbarManager.showMessage(R.string.place_select_error)
                }
            }

            1 -> {
                if (viewModel.fishAndWeightSate.value.isInputCorrect) {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                } else {
                    SnackbarManager.showMessage(R.string.fish_error)
                }

            }

            3 -> {
                if (viewModel.catchWeatherState.value.isInputCorrect) {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                } else {
                    SnackbarManager.showMessage(R.string.weather_error)
                }
            }

            else -> {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        }
    }

}

@Composable
fun CancelNewCatchDialog(
    onDismiss: () -> Unit,
    onPositiveClick: () -> Unit
) {
    DefaultDialog(
        primaryText = stringResource(R.string.cancel_new_catch_dialog),
        secondaryText = stringResource(R.string.sure_cancel_new_catch_dialog),
        negativeButtonText = stringResource(id = R.string.no),
        onNegativeClick = onDismiss,
        positiveButtonText = stringResource(id = R.string.yes),
        onPositiveClick = onPositiveClick,
        onDismiss = onDismiss,
        content = {
            LottieWarning(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }
    )
}
