package com.mobileprism.fishing.ui.home.new_catch

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.google.accompanist.pager.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.custom.DefaultDialog
import com.mobileprism.fishing.ui.custom.ModalLoadingDialog
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.new_catch.pages.NewCatchPage
import com.mobileprism.fishing.ui.home.place.LottieWarning
import com.mobileprism.fishing.ui.home.views.*
import com.mobileprism.fishing.ui.viewmodels.NewCatchMasterViewModel
import com.mobileprism.fishing.ui.viewstates.NewCatchViewState
import com.mobileprism.fishing.utils.Constants.MAX_PHOTOS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf

@ExperimentalPermissionsApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@ExperimentalPagerApi
@Composable
fun NewCatchMasterScreen(
    receivedPlace: UserMapMarker?,
    navController: NavController,
    upPress: () -> Unit,
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

    val coroutineScope = rememberCoroutineScope()

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

    val onFinish = {
        if (viewModel.photos.value.size <= MAX_PHOTOS) {
            viewModel.saveNewCatch()
            /*showInterstitialAd(
                context = context,
                onAdLoaded = { isAdLoaded.value = true }
            )*/
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
                    upPress()
                }
                NewCatchViewState.SavingNewCatch -> {
                    loadingDialogState.value = true
                    // TODO: Insert fullscreen AD after new catch
//                    showInterstitialAd(
//                        context = context,
//                        onAdLoaded = { isAdLoaded.value = true }
//                    )
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
        isLoading = loadingDialogState.value,
        text = stringResource(id = R.string.saving_new_catch)
    )

    val skipAvailable by viewModel.skipAvailable.collectAsState()

    /*ModalBottomSheetLayout(sheetState = modalBottomSheetState,
        sheetContent = {
            WeatherTypesSheet()
        }) {*/
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

@ExperimentalPagerApi
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

        HorizontalPagerIndicator(
            modifier = Modifier.constrainAs(indicator) {
                top.linkTo(next.top)
                bottom.linkTo(next.bottom)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            },
            activeColor = MaterialTheme.colors.primaryVariant,
            pagerState = pagerState
        )

        FishingButtonFilled(
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

@OptIn(ExperimentalPagerApi::class)
private fun handlePagerNextClick(
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
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

@ExperimentalComposeUiApi
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
