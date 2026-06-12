package com.mobileprism.fishing.ui.home.notes

fun createNotesTabs(): List<TabItem> = listOf(
    TabItem.Places { navController -> UserPlacesScreen(navController = navController) },
    TabItem.Catches { navController -> UserCatchesScreen(navController = navController) },
    TabItem.Statistics { _ -> StatisticsScreen() }
)
