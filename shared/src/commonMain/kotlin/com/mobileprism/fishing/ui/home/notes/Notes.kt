package com.mobileprism.fishing.ui.home.notes

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.mobileprism.fishing.model.datastore.NotesPreferences
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.home.views.*
import com.mobileprism.fishing.domain.entity.common.CatchesSortValues
import com.mobileprism.fishing.domain.entity.common.PlacesSortValues
import com.mobileprism.fishing.ui.utils.enums.stringRes
import com.mobileprism.fishing.utils.Constants.modalBottomSheetCorners
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

enum class BottomSheetScreen {
    Sort,
    Filter,
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Notes(
    modifier: Modifier = Modifier,
    navController: NavController,
    upPress: () -> Unit,
) {
    val notesPreferences: NotesPreferences = koinInject()
    val tabs = remember { createNotesTabs() }
    val pagerState = rememberPagerState(0) { tabs.size }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var bottomSheetScreen by remember { mutableStateOf(BottomSheetScreen.Sort) }

    val fabState = remember { mutableStateOf(MultiFabState.COLLAPSED) }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = bottomSheetState,
            shape = modalBottomSheetCorners,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            NotesModalBottomSheet(
                pagerState = pagerState,
                bottomSheetScreen = bottomSheetScreen,
                notesPreferences = notesPreferences,
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        topBar = {
            NotesAppBar(pagerState) { newSheetState ->
                bottomSheetScreen = newSheetState
                showBottomSheet = true
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
                        icon = Res.drawable.ic_add_catch,
                        text = stringResource(Res.string.add_new_catch),
                        onClick = { onAddNewCatchClick(navController) }
                    ),
                    FabMenuItem(
                        icon = Res.drawable.ic_baseline_add_location_24,
                        text = stringResource(Res.string.new_place),
                        onClick = { onAddNewPlaceClick(navController) }
                    )
                )
            )
        },
    ) { innerPadding ->
        AnimatedVisibility(
            fabState.value == MultiFabState.EXPANDED,
            modifier = Modifier
                .padding(innerPadding)
                .zIndex(4f)
                .fillMaxSize(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(color = MaterialTheme.colorScheme.scrim.copy(0.6f), onClick = {
                fabState.value = MultiFabState.COLLAPSED
            }) { }
        }
        Column(modifier = Modifier.padding(innerPadding)) {
            Tabs(tabs = tabs, pagerState = pagerState)
            TabsContent(tabs = tabs, pagerState = pagerState, navController)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesAppBar(
    pagerState: PagerState,
    openModalBottomSheet: (BottomSheetScreen) -> Unit
) {

    DefaultAppBar(
        title = stringResource(Res.string.notes),
        actions = {
            Row {
                if (pagerState.currentPage < 2) {
                    IconButton(onClick = {
                        if (!pagerState.isScrollInProgress) {
                            openModalBottomSheet(BottomSheetScreen.Sort)
                        }
                    }) { Icon(Icons.AutoMirrored.Filled.Sort, Icons.AutoMirrored.Filled.Sort.name) }
                }

            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
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
    val radioOptions = PlacesSortValues.entries

    Column {
        SettingsHeader(stringResource(Res.string.sort))
        ItemsSelection(
            radioOptions = radioOptions,
            currentOption = currentSort,
            labelProvider = { stringResource(it.stringRes) },
            onSelectedItem = onSelectedValue
        )
    }
}

@Composable
fun CatchesSort(
    currentSort: State<CatchesSortValues>,
    onSelectedValue: (catchesSort: CatchesSortValues) -> Unit
) {
    val radioOptions = CatchesSortValues.entries

    Column {
        SettingsHeader(stringResource(Res.string.sort))
        ItemsSelection(
            radioOptions = radioOptions,
            currentOption = currentSort,
            labelProvider = { stringResource(it.stringRes) },
            onSelectedItem = onSelectedValue
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tabs(tabs: List<TabItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme

    TabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = colorScheme.primary,
        contentColor = colorScheme.onPrimary,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                color = colorScheme.onPrimary,
                modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
            )
        }) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                icon = {
                    Icon(
                        painter = painterResource(tab.icon), contentDescription = null,
                        tint = colorScheme.onPrimary
                    )
                },
                text = {
                    Text(
                        stringResource(tab.titleRes),
                        color = colorScheme.onPrimary
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabsContent(tabs: List<TabItem>, pagerState: PagerState, navController: NavController) {
    HorizontalPager(
        state = pagerState
    ) { page ->
        tabs[page].screen(navController)
    }
}


private fun onAddNewCatchClick(navController: NavController) {
    navController.navigate(MainDestinations.NewCatch())
}

private fun onAddNewPlaceClick(navController: NavController) {
    navController.navigate(MainDestinations.Map(isAddingNewPlace = true))
}
