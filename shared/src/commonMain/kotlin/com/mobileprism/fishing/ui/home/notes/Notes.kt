package com.mobileprism.fishing.ui.home.notes

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.mobileprism.fishing.domain.entity.common.CatchesSortValues
import com.mobileprism.fishing.domain.entity.common.PlacesSortValues
import com.mobileprism.fishing.model.datastore.NotesPreferences
import com.mobileprism.fishing.ui.home.views.AppIconButton
import com.mobileprism.fishing.ui.home.views.AppTab
import com.mobileprism.fishing.ui.home.views.AppTopBar
import com.mobileprism.fishing.ui.home.views.FabMenuItem
import com.mobileprism.fishing.ui.home.views.FabWithMenu
import com.mobileprism.fishing.ui.home.views.ItemsSelection
import com.mobileprism.fishing.ui.home.views.MultiFabState
import com.mobileprism.fishing.ui.home.views.SettingsHeader
import com.mobileprism.fishing.ui.home.views.TabbedPager
import com.mobileprism.fishing.ui.utils.enums.stringRes
import com.mobileprism.fishing.utils.Constants.modalBottomSheetCorners
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

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
    val coroutineScope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSortSheet by remember { mutableStateOf(false) }

    val fabState = remember { mutableStateOf(MultiFabState.COLLAPSED) }

    val appTabs = tabs.map { tab -> AppTab(title = stringResource(tab.titleRes)) }

    if (showSortSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            sheetState = sheetState,
            shape = modalBottomSheetCorners,
            containerColor = FishingTheme.colorScheme.surface,
            contentColor = FishingTheme.colorScheme.onSurface,
        ) {
            NotesSortSheet(
                page = pagerState.currentPage,
                notesPreferences = notesPreferences,
            )
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = FishingTheme.colorScheme.surface,
        contentColor = FishingTheme.colorScheme.onSurface,
        topBar = {
            AppTopBar(
                title = stringResource(Res.string.notes),
                actions = {
                    if (tabs[pagerState.currentPage].hasSortAction && !pagerState.isScrollInProgress) {
                        AppIconButton(
                            onClick = { showSortSheet = true },
                            icon = rememberVectorPainter(Icons.AutoMirrored.Filled.Sort),
                            contentDescription = stringResource(Res.string.sort_options),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FabWithMenu(
                fabState = fabState,
                items = listOf(
                    FabMenuItem(
                        icon = Res.drawable.ic_add_catch,
                        text = stringResource(Res.string.add_new_catch),
                        onClick = { navController.navigate(com.mobileprism.fishing.ui.MainDestinations.NewCatch()) },
                    ),
                    FabMenuItem(
                        icon = Res.drawable.ic_baseline_add_location_24,
                        text = stringResource(Res.string.new_place),
                        onClick = { navController.navigate(com.mobileprism.fishing.ui.MainDestinations.Map(isAddingNewPlace = true)) },
                    ),
                ),
            )
        },
    ) { innerPadding ->
        AnimatedVisibility(
            visible = fabState.value == MultiFabState.EXPANDED,
            modifier = Modifier
                .padding(innerPadding)
                .zIndex(4f)
                .fillMaxSize(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Surface(
                color = FishingTheme.colorScheme.scrim.copy(alpha = 0.6f),
                onClick = { fabState.value = MultiFabState.COLLAPSED },
            ) {}
        }
        TabbedPager(
            tabs = appTabs,
            pagerState = pagerState,
            modifier = Modifier.padding(innerPadding),
            onSelect = { index -> coroutineScope.launch { pagerState.animateScrollToPage(index) } },
        ) { page ->
            tabs[page].screen(navController)
        }
    }
}

@Composable
private fun NotesSortSheet(
    page: Int,
    notesPreferences: NotesPreferences,
) {
    val coroutineScope = rememberCoroutineScope()
    when (page) {
        0 -> {
            val current: State<PlacesSortValues> = notesPreferences.getPlacesSortValue
                .collectAsState(PlacesSortValues.Default)
            PlacesSort(current) { newValue ->
                coroutineScope.launch { notesPreferences.savePlacesSortValue(newValue) }
            }
        }
        1 -> {
            val current: State<CatchesSortValues> = notesPreferences.getCatchesSortValue
                .collectAsState(CatchesSortValues.Default)
            CatchesSort(current) { newValue ->
                coroutineScope.launch { notesPreferences.saveCatchesSortValue(newValue) }
            }
        }
    }
}

@Composable
fun PlacesSort(
    currentSort: State<PlacesSortValues>,
    onSelectedValue: (PlacesSortValues) -> Unit,
) {
    Column {
        SettingsHeader(stringResource(Res.string.sort))
        ItemsSelection(
            radioOptions = PlacesSortValues.entries,
            currentOption = currentSort,
            labelProvider = { stringResource(it.stringRes) },
            onSelectedItem = onSelectedValue,
        )
    }
}

@Composable
fun CatchesSort(
    currentSort: State<CatchesSortValues>,
    onSelectedValue: (CatchesSortValues) -> Unit,
) {
    Column {
        SettingsHeader(stringResource(Res.string.sort))
        ItemsSelection(
            radioOptions = CatchesSortValues.entries,
            currentOption = currentSort,
            labelProvider = { stringResource(it.stringRes) },
            onSelectedItem = onSelectedValue,
        )
    }
}
