package com.joesemper.fishing.compose.ui.home.place

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.datastore.UserPreferences
import com.joesemper.fishing.compose.ui.home.*
import com.joesemper.fishing.compose.ui.home.notes.*
import com.joesemper.fishing.compose.ui.theme.primaryTextColor
import com.joesemper.fishing.compose.ui.theme.secondaryTextColor
import com.joesemper.fishing.compose.ui.theme.supportTextColor
import com.joesemper.fishing.domain.UserPlaceViewModel
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.utils.time.toDateTextMonth
import com.joesemper.fishing.utils.time.toTime
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

@Composable
fun PlaceTitleView(
    modifier: Modifier = Modifier,
    place: UserMapMarker,
    catchesAmount: Int,
) {

    ConstraintLayout(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        val (icon, title, fishIcon, navigateButton, date) = createRefs()

        Icon(
            modifier = Modifier
                .size(32.dp)
                .constrainAs(icon) {
                    top.linkTo(title.top)
                    bottom.linkTo(date.bottom)
                    absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                },
            painter = painterResource(R.drawable.ic_baseline_location_on_24),
            contentDescription = stringResource(R.string.place),
            tint = Color(place.markerColor)
        )

        HeaderText(
            modifier = Modifier.constrainAs(title) {
                absoluteLeft.linkTo(icon.absoluteRight, 8.dp)
                absoluteRight.linkTo(navigateButton.absoluteLeft, 8.dp)
                top.linkTo(parent.top, 16.dp)
                width = Dimension.fillToConstraints
            },
            text = place.title
        )

        DefaultIconButton(
            modifier = Modifier
                .size(32.dp)
                .constrainAs(navigateButton) {
                    top.linkTo(title.top)
                    bottom.linkTo(date.bottom)
                    absoluteRight.linkTo(parent.absoluteRight, 16.dp)
                },
            icon = painterResource(id = R.drawable.ic_place_on_map),
            onClick = { }
        )

        SupportText(
            modifier = Modifier.constrainAs(date) {
                top.linkTo(title.bottom, 4.dp)
                bottom.linkTo(parent.bottom, 8.dp)
                absoluteLeft.linkTo(title.absoluteLeft)
            },
            text = place.dateOfCreation.toDateTextMonth()
        )

        ItemCounter(
            modifier = Modifier.constrainAs(fishIcon) {
                top.linkTo(date.top)
                bottom.linkTo(date.bottom)
                absoluteLeft.linkTo(date.absoluteRight, 8.dp)
            },
            count = catchesAmount,
            icon = R.drawable.ic_fishing
        )
    }
}

@ExperimentalPagerApi
@Composable
fun PlaceTabsView(
    modifier: Modifier = Modifier,
    tabs: List<TabItem>,
    pagerState: PagerState
) {
    val scope = rememberCoroutineScope()
    TabRow(
        modifier = modifier,
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = MaterialTheme.colors.surface,
        contentColor = primaryTextColor,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
            )
        }) {
        tabs.forEachIndexed { index, tab ->
            LeadingIconTab(
                icon = {
                    Icon(
                        painter = painterResource(id = tab.icon), contentDescription = "",
                        tint = MaterialTheme.colors.primaryVariant
                    )
                },
                text = {
                    Text(
                        stringResource(tab.titleRes),
                        color = MaterialTheme.colors.onSurface
                    )
                },
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

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalPagerApi
@Composable
fun PlaceTabsContentView(
    tabs: List<TabItem>,
    pagerState: PagerState,
    navController: NavController,
    catches: List<UserCatch>,
    note: String
) {
    HorizontalPager(
        modifier = Modifier.fillMaxSize(),
        state = pagerState,
        count = tabs.size,
        verticalAlignment = Alignment.Top
    ) { page ->
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            when (page) {
                0 -> PlaceCatchesView(
                    catches = catches,
                    userCatchClicked = { onCatchItemClick(it, navController) }
                )
                1 -> DefaultNoteView(
                    modifier = Modifier.padding(8.dp),
                    note = note,
                    onSaveNoteChange = {}
                )
            }
        }


    }
}

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun PlaceCatchesView(
    modifier: Modifier = Modifier,
    catches: List<UserCatch>,
    userCatchClicked: (UserCatch) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 4.dp),
    ) {
        when {
            catches.isNotEmpty() -> {
                getDatesList(catches).forEach { catchDate ->
                    stickyHeader {
                        ItemDate(text = catchDate)
                    }
                    items(items = catches
                        .filter { userCatch ->
                            userCatch.date.toDateTextMonth() == catchDate
                        }
                        .sortedByDescending { it.date },
                        key = {
                            it
                        }
                    ) {
                        PlaceCatchItemView(
                            catch = it,
                            onClick = { userCatch -> userCatchClicked(userCatch) }
                        )
                    }
                }
            }
            catches.isEmpty() -> {
                item {
                    NoElementsView(
                        mainText = stringResource(R.string.no_cathces_added),
                        secondaryText = stringResource(R.string.add_catch_text),
                        onClickAction = { }
                    )
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun PlaceCatchItemView(
    modifier: Modifier = Modifier,
    catch: UserCatch,
    showPlace: Boolean = true,
    onClick: (UserCatch) -> Unit
) {
    val preferences: UserPreferences = get()
    val is12hTimeFormat by preferences.use12hTimeFormat.collectAsState(initial = false)

    DefaultCardClickable(
        modifier = modifier,
        onClick = { onClick(catch) }
    ) {
        ConstraintLayout(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            val (fishType, amount, weight, placeIcon, place, time, photosCount) = createRefs()

            PrimaryText(
                modifier = Modifier.constrainAs(fishType) {
                    top.linkTo(parent.top)
                    absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                    absoluteRight.linkTo(weight.absoluteLeft, 16.dp)
                    width = Dimension.fillToConstraints
                },
                text = catch.fishType,
                maxLines = 1
            )

            SecondaryTextSmall(
                modifier = Modifier.constrainAs(amount) {
                    top.linkTo(fishType.bottom)
                    absoluteLeft.linkTo(fishType.absoluteLeft)
                },
                text = "${stringResource(id = R.string.amount)}: ${catch.fishAmount}" +
                        " ${stringResource(id = R.string.pc)}"
            )

            PrimaryTextBold(
                modifier = Modifier.constrainAs(weight) {
                    top.linkTo(fishType.top)
                    absoluteRight.linkTo(parent.absoluteRight, 8.dp)
                },
                text = "${catch.fishWeight} ${stringResource(id = R.string.kg)}"
            )

            if (showPlace) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .constrainAs(placeIcon) {
                            absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                            top.linkTo(time.top)
                            bottom.linkTo(time.bottom)
                        },
                    painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                    contentDescription = stringResource(id = R.string.location),
                    tint = secondaryTextColor
                )

                SecondaryText(
                    modifier = Modifier.constrainAs(place) {
                        absoluteLeft.linkTo(placeIcon.absoluteRight, 8.dp)
                        absoluteRight.linkTo(photosCount.absoluteLeft, 8.dp)
                        top.linkTo(placeIcon.top)
                        bottom.linkTo(placeIcon.bottom)
                        width = Dimension.fillToConstraints
                    },
                    text = catch.placeTitle,
                    textAlign = TextAlign.Start,
                    maxLines = 1
                )
            }

            SupportText(
                modifier = Modifier.constrainAs(time) {
                    absoluteRight.linkTo(parent.absoluteRight, 8.dp)
                    top.linkTo(amount.bottom, 16.dp)
                },
                text = catch.date.toTime(is12hTimeFormat)
            )

            ItemCounter(
                modifier = Modifier.constrainAs(photosCount) {
                    top.linkTo(time.top)
                    bottom.linkTo(time.bottom)
                    absoluteRight.linkTo(time.absoluteLeft, 12.dp)
                },
                count = catch.downloadPhotoLinks.size,
                icon = R.drawable.ic_baseline_photo_24
            )

        }
    }
}

@Composable
fun PlaceButtonsView(
    modifier: Modifier = Modifier,
    place: UserMapMarker,
    navController: NavController,
    viewModel: UserPlaceViewModel
) {
    val context = LocalContext.current

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.padding(4.dp))

        ButtonWithIcon(
            text = stringResource(id = R.string.new_catch),
            icon = painterResource(id = R.drawable.ic_add_catch),
            onClick = { newCatchClicked(navController, viewModel) }
        )

        ButtonWithIcon(
            text = stringResource(id = R.string.navigate),
            icon = painterResource(id = R.drawable.ic_baseline_navigation_24),
            onClick = { onRouteClicked(context, place) }
        )

        ButtonWithIcon(
            text = stringResource(id = R.string.share),
            icon = painterResource(id = R.drawable.ic_baseline_share_24),
            onClick = { onShareClicked(context, place) }
        )

        ButtonWithIcon(
            text = stringResource(id = R.string.edit),
            icon = painterResource(id = R.drawable.ic_baseline_edit_24),
            onClick = { }
        )

        ButtonWithIcon(
            text = stringResource(id = R.string.delete),
            icon = painterResource(id = R.drawable.ic_baseline_delete_24),
            onClick = { }
        )

        Spacer(modifier = Modifier.padding(4.dp))
    }
}

@Composable
fun PlaceTopBar(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    var toggleChecked by remember {
        mutableStateOf(false)
    }

    val color = animateColorAsState(
        targetValue = if (toggleChecked) {
            supportTextColor
        } else {
            MaterialTheme.colors.onPrimary
        }
    )

    DefaultAppBar(
        modifier = modifier,
        title = stringResource(id = R.string.place),
        onNavClick = { navController.popBackStack() },
        actions = {
            IconToggleButton(checked = toggleChecked, onCheckedChange = {
                toggleChecked = it
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_remove_red_eye_24),
                    contentDescription = null,
                    tint = color.value
                )
            }
        }
    )
}