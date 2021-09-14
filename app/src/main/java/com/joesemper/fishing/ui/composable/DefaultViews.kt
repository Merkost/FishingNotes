package com.joesemper.fishing.ui.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MyCard(content: @Composable () -> Unit) {
    Card(elevation = 8.dp, modifier = Modifier.fillMaxWidth().padding(4.dp), content = content)
}