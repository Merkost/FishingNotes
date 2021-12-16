package com.joesemper.fishing.compose.ui.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.google.accompanist.pager.*
import com.joesemper.fishing.R
import com.joesemper.fishing.compose.datastore.UserPreferences
import com.joesemper.fishing.compose.ui.Arguments
import com.joesemper.fishing.compose.ui.MainDestinations
import com.joesemper.fishing.compose.ui.home.notes.ItemDate
import com.joesemper.fishing.compose.ui.home.notes.NoElementsView
import com.joesemper.fishing.compose.ui.home.notes.TabItem
import com.joesemper.fishing.compose.ui.home.notes.getDatesList
import com.joesemper.fishing.compose.ui.navigate
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
import org.koin.androidx.compose.getViewModel
import java.util.*

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
            DefaultAppBar(
                title = stringResource(id = R.string.place),
                onNavClick = { navController.popBackStack() }
            )
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
                    navController = navController,
                    viewModel = viewModel
                )
                Spacer(modifier = Modifier.size(8.dp))
                PlaceTabsView(tabs = tabs, pagerState = pagerState)
                PlaceTabsContentView(
                    tabs = tabs,
                    pagerState = pagerState,
                    navController = navController,
                    catches = userCatches,
                    note = place?.description ?: ""
                )
            }
        }
    }
}

@Composable
fun PlaceTitleView(
    modifier: Modifier = Modifier,
    place: UserMapMarker,
    catchesAmount: Int,
    navController: NavController,
    viewModel: UserPlaceViewModel
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (title, icon, amountTitle, amountValue, fishIcon, navigateButton, buttons) = createRefs()

        Icon(
            modifier = Modifier
                .padding(5.dp)
                .size(24.dp)
                .constrainAs(icon) {
                    top.linkTo(parent.top, 8.dp)
                    absoluteLeft.linkTo(parent.absoluteLeft, 8.dp)
                },
            painter = painterResource(R.drawable.ic_baseline_location_on_24),
            contentDescription = stringResource(R.string.place),
            tint = Color(place.markerColor)
        )

        HeaderText(
            modifier = Modifier.constrainAs(title) {
                linkTo(
                    icon.absoluteRight,
                    navigateButton.absoluteLeft,
                    startMargin = 8.dp,
                    endMargin = 8.dp,
                    bias = 0f
                )
                top.linkTo(icon.top)
                bottom.linkTo(icon.bottom)
            },
            text = place.title
        )

        IconButton(
            modifier = Modifier.constrainAs(navigateButton) {
                top.linkTo(title.top)
                bottom.linkTo(title.bottom)
                absoluteRight.linkTo(parent.absoluteRight, 8.dp)
            },
            onClick = { /*TODO*/ }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_map_24),
                contentDescription = null,
                tint = primaryTextColor
            )
        }

        PrimaryText(
            modifier = Modifier.constrainAs(amountTitle) {
                top.linkTo(fishIcon.top)
                bottom.linkTo(fishIcon.bottom)
                absoluteLeft.linkTo(title.absoluteLeft)
            },
            text = stringResource(id = R.string.catches),
        )

        PrimaryText(
            modifier = Modifier.constrainAs(amountValue) {
                top.linkTo(fishIcon.top)
                bottom.linkTo(fishIcon.bottom)
                absoluteLeft.linkTo(navigateButton.absoluteLeft)
                absoluteRight.linkTo(navigateButton.absoluteRight)
            },
            text = "$catchesAmount ${stringResource(id = R.string.pc)}"
        )

        Icon(
            modifier = Modifier
                .size(24.dp)
                .constrainAs(fishIcon) {
                    top.linkTo(title.bottom, 8.dp)
                    absoluteLeft.linkTo(icon.absoluteLeft)
                    absoluteRight.linkTo(icon.absoluteRight)
                },
            painter = painterResource(id = R.drawable.ic_fish),
            contentDescription = stringResource(id = R.string.fish_catch),
            tint = secondaryTextColor
        )

        PlaceButtonsView(
            modifier = Modifier.constrainAs(buttons) {
                top.linkTo(fishIcon.bottom, 16.dp)
                absoluteRight.linkTo(parent.absoluteRight)
                absoluteLeft.linkTo(parent.absoluteLeft)
            },
            place = place,
            navController = navController,
            viewModel = viewModel
        )
    }
}

@Composable
fun PlaceNoteView(
    modifier: Modifier = Modifier,
    note: String,
) {
    DefaultCard(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .fillMaxWidth()
                .fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            SubtitleWithIcon(
                modifier = Modifier,
                icon = R.drawable.ic_baseline_sticky_note_2_24,
                text = stringResource(id = R.string.note)
            )
            Spacer(modifier = Modifier.size(8.dp))
            if (note.isNotBlank()) {
                PrimaryText(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = note
                )
            } else {
                SecondaryText(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = stringResource(id = R.string.no_description)
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
                            onClick = { userCatchClicked(it) }
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

//@Composable
//fun AddNewCatchFab(onClick: () -> Unit) {
//    FloatingActionButton(
//        backgroundColor = secondaryFigmaColor,
//        onClick = { onClick() }
//    ) {
//        Icon(
//            painter = painterResource(id = R.drawable.ic_add_catch),
//            tint = Color.White,
//            contentDescription = stringResource(
//                id = R.string.add_new_catch
//            )
//        )
//    }
//}

@ExperimentalMaterialApi
@Composable
fun PlaceCatchItemView(
    modifier: Modifier = Modifier,
    catch: UserCatch,
    onClick: () -> Unit
) {
    val preferences: UserPreferences = get()
    val is12hTimeFormat by preferences.use12hTimeFormat.collectAsState(initial = false)

    DefaultCardClickable(
        modifier = modifier,
        onClick = { onClick() }
    ) {
        ConstraintLayout(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            val (fishType, amount, weight, rodIcon, rod, time, photosIcon, photosCount) = createRefs()

            PrimaryTextBold(
                modifier = Modifier.constrainAs(fishType) {
                    top.linkTo(parent.top)
                    linkTo(
                        parent.start,
                        weight.start,
                        startMargin = 8.dp,
                        endMargin = 8.dp,
                        bias = 0f
                    )
                },
                text = catch.fishType
            )

            SecondaryTextSmall(
                modifier = Modifier.constrainAs(amount) {
                    top.linkTo(fishType.bottom, 2.dp)
                    absoluteLeft.linkTo(fishType.absoluteLeft)
                },
                text = "${stringResource(id = R.string.amount)}: ${catch.fishAmount}" +
                        " ${stringResource(id = R.string.pc)}"
            )

            PrimaryText(
                modifier = Modifier.constrainAs(weight) {
                    top.linkTo(fishType.top)
                    absoluteRight.linkTo(parent.absoluteRight, 8.dp)
                },
                text = "${catch.fishWeight} ${stringResource(id = R.string.kg)}"
            )

            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .constrainAs(rodIcon) {
                        absoluteLeft.linkTo(fishType.absoluteLeft)
                        top.linkTo(amount.bottom, 12.dp)
                    },
                painter = painterResource(id = R.drawable.ic_fishing_rod),
                contentDescription = stringResource(id = R.string.fish_rod)
            )

            PrimaryText(
                modifier = Modifier.constrainAs(rod) {
                    absoluteLeft.linkTo(rodIcon.absoluteRight, 4.dp)
                    top.linkTo(rodIcon.top)
                    bottom.linkTo(rodIcon.bottom)
                },
                text = if (catch.fishingRodType.isNotBlank()) {
                    catch.fishingRodType
                } else {
                    stringResource(id = R.string.no_rod)
                }
            )

            SupportText(
                modifier = Modifier.constrainAs(time) {
                    absoluteRight.linkTo(parent.absoluteRight, 12.dp)
                    top.linkTo(rod.bottom)
                },
                text = catch.date.toTime(is12hTimeFormat)
            )

            SupportText(
                modifier = Modifier.constrainAs(photosCount) {
                    top.linkTo(time.top)
                    absoluteRight.linkTo(time.absoluteLeft, 12.dp)
                },
                text = " x ${catch.downloadPhotoLinks.size}"
            )

            Icon(
                modifier = Modifier.constrainAs(photosIcon) {
                    top.linkTo(photosCount.top)
                    bottom.linkTo(photosCount.bottom)
                    absoluteRight.linkTo(photosCount.absoluteLeft)
                },
                painter = painterResource(id = R.drawable.ic_baseline_photo_24),
                contentDescription = null,
                tint = supportTextColor
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
fun ButtonWithIcon(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    text: String,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.White,
            contentColor = MaterialTheme.colors.primaryVariant
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, color = secondaryTextColor)
    ) {
        if (icon != null) {
            Icon(
                modifier = Modifier,
                painter = icon,
                contentDescription = null
            )
        }
        PrimaryText(
            modifier = Modifier.padding(horizontal = 4.dp),
            text = text,
            textColor = MaterialTheme.colors.primaryVariant
        )

    }
}


@ExperimentalPagerApi
@Composable
fun PlaceTabsView(tabs: List<TabItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    TabRow(
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
        state = pagerState, count = tabs.size
    ) { page ->
        when (page) {
            0 -> PlaceCatchesView(
                catches = catches,
                userCatchClicked = { onCatchItemClick(it, navController) }
            )
            1 -> PlaceNoteView(note = note)
        }
    }
}


@Composable
fun UserPlaceAppBar(
    navController: NavController,
    viewModel: UserPlaceViewModel,
) {
    DefaultAppBar(
        title = stringResource(id = R.string.place),
        onNavClick = { navController.popBackStack() }
    )
}

private fun newCatchClicked(navController: NavController, viewModel: UserPlaceViewModel) {
    val marker: UserMapMarker? = viewModel.marker.value
    marker?.let {
        navController.navigate(MainDestinations.NEW_CATCH_ROUTE, Arguments.PLACE to it)
    }
}

private fun onRouteClicked(context: Context, marker: UserMapMarker) {
    val uri = String.format(
        Locale.ENGLISH,
        "http://maps.google.com/maps?daddr=%f,%f (%s)",
        marker.latitude,
        marker.longitude,
        marker.title
    )
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    intent.setPackage("com.google.android.apps.maps")
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        try {
            val unrestrictedIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            context.startActivity(unrestrictedIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Please install a maps application", Toast.LENGTH_LONG)
                .show()
        }
    }
}


private fun onShareClicked(
    context: Context,
    marker: UserMapMarker
) {
    val text =
        "${marker.title}\nhttps://www.google.com/maps/search/?api=1&query=${marker.latitude}" +
                ",${marker.longitude}"
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}

private fun onCatchItemClick(catch: UserCatch, navController: NavController) {
    navController.navigate(MainDestinations.CATCH_ROUTE, Arguments.CATCH to catch)
}