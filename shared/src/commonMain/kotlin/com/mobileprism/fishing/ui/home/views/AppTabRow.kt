package com.mobileprism.fishing.ui.home.views

import com.mobileprism.fishing.ui.theme.FishingTheme
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mobileprism.fishing.ui.theme.Motion

data class AppTab(
    val title: String,
)

@Composable
fun AppTabRow(
    tabs: List<AppTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    TabRow(
        modifier = modifier,
        selectedTabIndex = selectedIndex,
        containerColor = FishingTheme.colorScheme.surface,
        contentColor = FishingTheme.colorScheme.primary,
        indicator = { tabPositions ->
            if (selectedIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    color = FishingTheme.colorScheme.primary,
                )
            }
        },
        divider = {},
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            Tab(
                selected = selected,
                onClick = { onSelect(index) },
                selectedContentColor = FishingTheme.colorScheme.primary,
                unselectedContentColor = FishingTheme.colorScheme.onSurfaceVariant,
                text = { AppText(text = tab.title, style = AppTextStyle.Title) },
            )
        }
    }
}

@Composable
fun TabbedPager(
    tabs: List<AppTab>,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit = {},
    content: @Composable (page: Int) -> Unit,
) {
    Column(modifier = modifier) {
        AppTabRow(
            tabs = tabs,
            selectedIndex = pagerState.currentPage,
            onSelect = onSelect,
        )
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(),
        ) { page ->
            Crossfade(
                targetState = page,
                animationSpec = tween(Motion.medium),
                label = "TabbedPagerContent",
            ) { current -> content(current) }
        }
    }
}

@Composable
fun rememberTabbedPagerState(pageCount: Int, initialPage: Int = 0): PagerState =
    rememberPagerState(initialPage = initialPage) { pageCount }
