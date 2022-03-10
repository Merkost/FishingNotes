package com.mobileprism.fishing.ui.home.notes

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.google.accompanist.pager.*
import com.mobileprism.fishing.R
import com.mobileprism.fishing.model.datastore.NotesPreferences
import com.mobileprism.fishing.model.datastore.NotesPreferencesImpl
import com.mobileprism.fishing.ui.Arguments
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.SettingsHeader
import com.mobileprism.fishing.ui.home.views.*
import com.mobileprism.fishing.ui.theme.primaryTextColor
import com.mobileprism.fishing.ui.utils.enums.CatchesSortValues
import com.mobileprism.fishing.ui.utils.enums.PlacesSortValues
import com.mobileprism.fishing.utils.Constants.modalBottomSheetCorners
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

enum class BottomSheetScreen {
    Sort,
    Filter,
}

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@Composable
fun Notes(
    modifier: Modifier = Modifier,
    navController: NavController,
    upPress: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val notesPreferences: NotesPreferences = get()
    val tabs = remember { listOf(TabItem.Places, TabItem.Catches) }
    val pagerState = rememberPagerState(0)

    val bottomState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    var bottomSheetScreen by remember { mutableStateOf(BottomSheetScreen.Sort) }
    val shouldShowBlur = remember { mutableStateOf(false) }

    val fabState = remember { mutableStateOf(MultiFabState.COLLAPSED) }

    ModalBottomSheetLayout(
        sheetState = bottomState,
        sheetShape = modalBottomSheetCorners,
        sheetContent = {
            NotesModalBottomSheet(
                pagerState = pagerState,
                bottomSheetScreen = bottomSheetScreen,
                notesPreferences = notesPreferences,
            )
        }) {
        Scaffold(
            topBar = {
                NotesAppBar(pagerState) { newSheetState ->
                    bottomSheetScreen = newSheetState
                    coroutineScope.launch { bottomState.show() }
                }
            },
            floatingActionButton = {
                FabWithMenu(
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .zIndex(5f),
                    fabState = fabState,
                    items = listOf(
                        FabMenuItem(
                            icon = R.drawable.ic_add_catch,
                            text = stringResource(R.string.add_new_catch),
                            onClick = { onAddNewCatchClick(navController) }
                        ),
                        FabMenuItem(
                            icon = R.drawable.ic_baseline_add_location_24,
                            text = stringResource(R.string.new_place),
                            onClick = { onAddNewPlaceClick(navController) }
                        )
                    )
                )
            },
        ) {
            AnimatedVisibility(
                fabState.value == MultiFabState.EXPANDED,
                modifier = Modifier
                    .zIndex(4f)
                    .fillMaxSize(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(color = Color.Black.copy(0.6f), onClick = {
                    fabState.value = MultiFabState.COLLAPSED
                }) { }
            }
            Column() {
                Tabs(tabs = tabs, pagerState = pagerState)
                TabsContent(tabs = tabs, pagerState = pagerState, navController)
            }
        }
    }
}

@ExperimentalPagerApi
@Composable
fun NotesAppBar(
    pagerState: PagerState,
    openModalBottomSheet: (BottomSheetScreen) -> Unit
) {

    DefaultAppBar(
        title = stringResource(id = R.string.notes),
        actions = {
            Row {
                IconButton(onClick = {
                    if (!pagerState.isScrollInProgress) {
                        openModalBottomSheet(BottomSheetScreen.Sort)
                    }
                }) { Icon(Icons.Default.Sort, Icons.Default.Sort.name) }

                //TODO: Add filters
                /*IconButton(onClick = {
                    if (!pagerState.isScrollInProgress) {
                        bottomSheetScreen = BottomSheetScreen.Filter
                        coroutineScope.launch { bottomState.show() }
                    }
                }) { Icon(Icons.Default.FilterAlt, Icons.Default.FilterAlt.name) }*/
            }
        }
    )
}

@ExperimentalPagerApi
@Composable
fun NotesModalBottomSheet(
    pagerState: PagerState,
    bottomSheetScreen: BottomSheetScreen,
    notesPreferences: NotesPreferences
) {
    val currentPlacesSort = notesPreferences.getPlacesSortValue
        .collectAsState(PlacesSortValues.Default)
    val currentCatchesSort = notesPreferences.getCatchesSortValue
        .collectAsState(CatchesSortValues.Default)

    val coroutineScope = rememberCoroutineScope()

    when (pagerState.currentPage) {
        0 -> {
            when (bottomSheetScreen) {
                BottomSheetScreen.Sort -> {
                    PlacesSort(currentPlacesSort) { newValue ->
                        coroutineScope.launch {
                            notesPreferences.savePlacesSortValue(newValue)
                        }
                    }
                }
                BottomSheetScreen.Filter -> {
                    /*Text("Not yet implemented")*/
                }
            }
        }
        1 -> {
            when (bottomSheetScreen) {
                BottomSheetScreen.Sort -> {
                    CatchesSort(currentCatchesSort) { newValue ->
                        coroutineScope.launch {
                            notesPreferences.saveCatchesSortValue(newValue)
                        }
                    }
                }
                BottomSheetScreen.Filter -> {
                    /*Text("Not yet implemented")*/
                }
            }
        }
    }
}

@Composable
fun PlacesSort(
    currentSort: State<PlacesSortValues>,
    onSelectedValue: (placesSore: PlacesSortValues) -> Unit
) {
    val radioOptions = PlacesSortValues.values().asList()

    Column {
        SettingsHeader(stringResource(R.string.sort))
        ItemsSelection(
            radioOptions = radioOptions,
            currentOption = currentSort,
            onSelectedItem = onSelectedValue
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CatchesSort(
    currentSort: State<CatchesSortValues>,
    onSelectedValue: (catchesSort: CatchesSortValues) -> Unit
) {
    val radioOptions = CatchesSortValues.values().asList()

    Column {
        SettingsHeader(stringResource(R.string.sort))
        ItemsSelection(
            radioOptions = radioOptions,
            currentOption = currentSort,
            onSelectedItem = onSelectedValue
        )
    }
}

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun Tabs(tabs: List<TabItem>, pagerState: PagerState) {
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

@ExperimentalPagerApi
@Composable
fun TabsContent(tabs: List<TabItem>, pagerState: PagerState, navController: NavController) {
    HorizontalPager(
        state = pagerState, count = tabs.size
    ) { page ->
        tabs[page].screen(navController)
    }
}


private fun onAddNewCatchClick(navController: NavController) {
    navController.navigate(MainDestinations.NEW_CATCH_ROUTE)
}

private fun onAddNewPlaceClick(navController: NavController) {
    val addNewPlace = true
    navController.navigate("${MainDestinations.HOME_ROUTE}/${MainDestinations.MAP_ROUTE}?${Arguments.MAP_NEW_PLACE}=${addNewPlace}")
}





