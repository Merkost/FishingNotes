package com.mobileprism.fishing.ui.home.place

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.domain.entity.common.Note
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.catch.EditNoteDialog
import com.mobileprism.fishing.ui.home.notes.*
import com.mobileprism.fishing.ui.home.views.*
import com.mobileprism.fishing.ui.viewmodels.UserPlaceViewModel
import com.mobileprism.fishing.utils.Constants.bottomBannerPadding
import com.mobileprism.fishing.utils.time.toDateTextMonth
import kotlinx.coroutines.launch

@Composable
fun PlaceTitleView(
    modifier: Modifier = Modifier,
    place: UserMapMarker,
    catchesAmount: Int,
    navigateToMap: () -> Unit
) {

    Row(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .size(32.dp)
                .padding(start = 8.dp),
            painter = painterResource(Res.drawable.ic_baseline_location_on_24),
            contentDescription = stringResource(Res.string.place),
            tint = Color(place.markerColor)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 8.dp)
        ) {
            HeaderText(
                text = place.title
            )

            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SupportText(
                    text = place.dateOfCreation.toDateTextMonth()
                )

                ItemCounter(
                    modifier = Modifier.padding(start = 8.dp),
                    count = catchesAmount,
                    icon = Res.drawable.ic_fishing,
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        DefaultIconButton(
            modifier = Modifier
                .size(48.dp)
                .padding(end = 16.dp),
            icon = painterResource(Res.drawable.ic_place_on_map),
            onClick = { navigateToMap() }
        )
    }
}

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
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
            )
        }) {
        tabs.forEachIndexed { index, tab ->
            LeadingIconTab(
                icon = {
                    Icon(
                        painter = painterResource(tab.icon), contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                },
                text = {
                    Text(
                        stringResource(tab.titleRes),
                        color = MaterialTheme.colorScheme.onSurface
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
@ExperimentalMaterial3Api
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun PlaceTabsContentView(
    tabs: List<TabItem>,
    pagerState: PagerState,
    navController: NavController,
    catches: List<UserCatch>,
    notes: List<Note>,
    onNewCatchClick: () -> Unit,
    onNoteSelected: (Note) -> Unit
) {
    HorizontalPager(
        modifier = Modifier.fillMaxSize(),
        state = pagerState,
        verticalAlignment = Alignment.Top
    ) { page ->


        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            when (page) {
                0 -> PlaceCatchesView(
                    catches = catches,
                    onNewCatchClick = onNewCatchClick
                ) {
                    navController.navigate(MainDestinations.Catch(it))
                }
                1 -> PlaceNotes(notes) {
                    onNoteSelected(it)
                }
            }
        }

    }
}

@ExperimentalComposeUiApi
@Composable
fun NoteModalBottomSheet(
    viewModel: UserPlaceViewModel,
    onCloseBottomSheet: () -> Unit,
) {
    val currentNote by viewModel.currentNote.collectAsState()
    EditNoteDialog(
        note = currentNote ?: Note(),
        onSaveNote = viewModel::updateMarkerNotes,
        deleteOption = (currentNote ?: Note()).id.isNotEmpty(),
        onDeleteNote = viewModel::deleteMarkerNote,
        onCloseDialog = onCloseBottomSheet
    )
}

@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@Composable
fun PlaceNotes(
    notes: List<Note>?,
    onNoteSelected: (Note) -> Unit
) {
    LazyColumn(/*contentPadding = PaddingValues(8.dp)*/) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Card(onClick = { onNoteSelected(Note()) }) {
                    Icon(Icons.Default.Add, stringResource(Res.string.add_note))
                }
            }
        }
        notes?.let {
            items(notes) { note ->
                DefaultNoteView(
                    modifier = Modifier.padding(8.dp),
                    note = note,
                    onClick = { onNoteSelected(note) }
                )
            }
        } ?: item {
            DefaultNoteView(
                modifier = Modifier.padding(8.dp),
                note = Note(),
                onClick = { onNoteSelected(Note()) }
            )
        }

        item { Spacer(modifier = Modifier.size(bottomBannerPadding)) }
    }
}

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun PlaceCatchesView(
    modifier: Modifier = Modifier,
    catches: List<UserCatch>,
    onNewCatchClick: () -> Unit,
    userCatchClicked: (UserCatch) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
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
                        key = { it.id }
                    ) {
                        CatchItemView(
                            catch = it,
                            showPlace = false,
                            onClick = { userCatch -> userCatchClicked(userCatch) },
                            childModifier = Modifier
                        )
                    }

                }
                item { Spacer(modifier = Modifier.size(bottomBannerPadding)) }
            }
            catches.isEmpty() -> {
                item {
                    NoContentView(
                        modifier = Modifier.padding(top = 128.dp),
                        text = stringResource(Res.string.no_cathces_added),
                        icon = painterResource(Res.drawable.ic_fishing)
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    DefaultButtonOutlined(
                        text = stringResource(Res.string.new_catch_text),
                        onClick = { onNewCatchClick() }
                    )
                }
                item { Spacer(modifier = Modifier.size(bottomBannerPadding)) }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlaceButtonsView(
    modifier: Modifier = Modifier,
    place: UserMapMarker,
    navController: NavController,
    viewModel: UserPlaceViewModel
) {
    val analyticsTracker = com.mobileprism.fishing.ui.utils.LocalAnalytics.current

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.padding(4.dp))

        DefaultButtonOutlined(
            text = stringResource(Res.string.new_catch),
            icon = painterResource(Res.drawable.ic_add_catch),
            onClick = { newCatchClicked(navController, place) }
        )

        DefaultButtonOutlined(
            text = stringResource(Res.string.navigate),
            icon = painterResource(Res.drawable.ic_baseline_navigation_24),
            onClick = { openMapNavigation(place, analyticsTracker) }
        )

        DefaultButtonOutlined(
            text = stringResource(Res.string.share),
            icon = painterResource(Res.drawable.ic_baseline_share_24),
            onClick = { shareMarkerLocation(place, analyticsTracker) }
        )

        /*DefaultButtonOutlined(
            text = stringResource(Res.string.edit),
            icon = painterResource(Res.drawable.ic_baseline_edit_24),
            onClick = { }
        )*/

        /*DefaultButtonOutlined(
            text = stringResource(Res.string.delete),
            icon = painterResource(Res.drawable.ic_baseline_delete_24),
            onClick = { deleteDialogIsShowing = true }
        )*/

        Spacer(modifier = Modifier.padding(4.dp))
    }
}

@ExperimentalComposeUiApi
@Composable
fun DeletePlaceDialog(
    place: UserMapMarker,
    onDismiss: () -> Unit,
    onPositiveClick: () -> Unit
) {
    DefaultDialog(
        primaryText = stringResource(Res.string.delete_place_dialog).replace("%s", place.title).replace("%1\$s", place.title),
        secondaryText = stringResource(Res.string.sure_delete_place_dialog),
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

@Composable
fun PlaceTopBar(
    backPress: () -> Unit,
    viewModel: UserPlaceViewModel,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    ) {

    val isVisible by viewModel.markerVisibility.collectAsState()

    var menuOpened by remember { mutableStateOf(false) }

    val color = animateColorAsState(
        targetValue = if (isVisible == true) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(800)
    )

    DefaultAppBar(
        modifier = modifier,
        title = stringResource(Res.string.place),
        onNavClick = backPress,
        actions = {
            IconToggleButton(checked = isVisible ?: true,
                onCheckedChange = {
                    viewModel.changeVisibility(it)
                }) {
                Icon(
                    painter = painterResource(Res.drawable.ic_baseline_remove_red_eye_24),
                    contentDescription = stringResource(Res.string.toggle_visibility),
                    tint = color.value
                )
            }
            IconButton(onClick = { menuOpened  = true }) {
                Icon(Icons.Outlined.MoreVert, Icons.Outlined.MoreVert.name)
            }
            DropdownMenu(expanded = menuOpened, onDismissRequest = { menuOpened = false }) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(Res.string.delete), maxLines = 1) },
                    onClick = { menuOpened = false; onDelete() }
                )
            }

        }
    )

}