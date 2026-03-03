package com.mobileprism.fishing.ui.home.new_catch

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import androidx.compose.material3.SnackbarDuration
import com.mobileprism.fishing.ui.home.SnackbarAction
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.advertising.AdIds
import com.mobileprism.fishing.ui.home.advertising.BannerAdvertView
import com.mobileprism.fishing.ui.home.advertising.rememberInterstitialAdLauncher
import com.mobileprism.fishing.ui.utils.PlatformBackHandler
import com.mobileprism.fishing.ui.home.new_catch.pages.NewCatchPage
import com.mobileprism.fishing.ui.home.views.LottieWarning
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
import org.koin.compose.viewmodel.koinViewModel
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

    PlatformBackHandler {
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
    val launchAd = rememberInterstitialAdLauncher(onComplete = { upPress() })

    val onFinish = {
        if (viewModel.photos.value.size <= MAX_PHOTOS) {
            viewModel.saveNewCatch()
        } else {
            SnackbarManager.showMessage(Res.string.max_photos_allowed)
        }
    }

    val uiState = viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = uiState.value) {
        uiState.value.let {
            when (it) {
                NewCatchViewState.Editing -> {}
                NewCatchViewState.Complete -> {
                    loadingDialogState.value = false
                    SnackbarManager.showMessage(Res.string.catch_added_successfully)
                    launchAd()
                }

                NewCatchViewState.SavingNewCatch -> {
                    loadingDialogState.value = true
                }

                is NewCatchViewState.Error -> {
                    loadingDialogState.value = false
                    SnackbarManager.showMessage(
                        messageTextId = Res.string.error_occured,
                        snackbarAction = SnackbarAction(
                            textId = Res.string.retry,
                            action = { viewModel.saveNewCatch() }
                        ),
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }

    }

    val uploadProgress by viewModel.uploadProgress.collectAsState()

    val currentProgress = uploadProgress
    val loadingText = when {
        currentProgress != null -> stringResource(
            Res.string.uploading_photo_progress,
            currentProgress.uploaded,
            currentProgress.total
        )
        else -> stringResource(Res.string.saving_new_catch)
    }

    ModalLoadingDialog(
        visible = loadingDialogState.value,
        text = loadingText,
        progress = currentProgress?.takeIf { it.total > 0 }?.let { it.uploaded.toFloat() / it.total }
    )

    val skipAvailable by viewModel.skipAvailable.collectAsState()

    Scaffold(
        topBar = {
            DefaultAppBar(
                title = stringResource(Res.string.new_catch),
                onNavClick = { exitDialogIsShowing = true },
                actions = {
                    IconButton(
                        onClick = {
                            when (skipAvailable) {
                                true -> onFinish()
                                else -> SnackbarManager.showMessage(Res.string.new_catch_skip_tutor)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            NewCatchPager(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                navController = navController,
                viewModel = viewModel,
                pagerState = pagerState,
                pages = pages
            )

            NewCatchButtons(
                modifier = Modifier.fillMaxWidth(),
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
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                adId = AdIds.newCatchBanner,
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
                            change.consume()
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

    Row(
        modifier = modifier
            .padding(vertical = 32.dp, horizontal = 16.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DefaultButton(
            enabled = !isFirstPage,
            text = stringResource(Res.string.previous),
            onClick = onPreviousClick
        )

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            MyHorizontalPagerIndicator(
                activeColor = MaterialTheme.colorScheme.tertiary,
                pagerState = pagerState
            )
        }

        DefaultButtonFilled(
            text = if (isLastPage) {
                stringResource(Res.string.finish)
            } else {
                stringResource(Res.string.next)
            },
            onClick = {
                if (isLastPage) {
                    onFinishClick()
                } else {
                    onNextClick()
                }
            }
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
                    SnackbarManager.showMessage(Res.string.place_select_error)
                }
            }

            1 -> {
                if (viewModel.fishAndWeightState.value.isInputCorrect) {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                } else {
                    SnackbarManager.showMessage(Res.string.fish_error)
                }

            }

            3 -> {
                if (viewModel.catchWeatherState.value.isInputCorrect) {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                } else {
                    SnackbarManager.showMessage(Res.string.weather_error)
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
        primaryText = stringResource(Res.string.cancel_new_catch_dialog),
        secondaryText = stringResource(Res.string.sure_cancel_new_catch_dialog),
        negativeButtonText = stringResource(Res.string.no),
        onNegativeClick = onDismiss,
        positiveButtonText = stringResource(Res.string.yes),
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
