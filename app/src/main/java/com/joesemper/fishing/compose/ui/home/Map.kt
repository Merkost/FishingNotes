package com.joesemper.fishing.compose.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun Map(
    onSnackClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
//    val snackCollections = remember { SnackRepo.getSnacks() }
//    val filters = remember { SnackRepo.getFilters() }
    Surface(modifier = modifier.fillMaxSize(), color = Color.Blue) {

    }
}