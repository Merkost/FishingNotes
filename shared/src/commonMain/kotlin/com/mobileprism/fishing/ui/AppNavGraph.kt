package com.mobileprism.fishing.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder

expect fun NavGraphBuilder.AppNavGraph(
    navController: NavController,
    upPress: () -> Unit,
)
