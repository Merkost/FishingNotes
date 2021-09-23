package com.joesemper.fishing.compose.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun Weather(
    onSnackClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
//    viewModel: CartViewModel = viewModel(factory = CartViewModel.provideFactory())
) {
    Surface(modifier = modifier.fillMaxSize(), color = Color.Red) {

    }
}