package com.joesemper.fishing.compose.ui.home.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun MyCardNoPadding(content: @Composable () -> Unit) {
    Card(
        elevation = 4.dp, shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(), content = content
    )
}

@Composable
fun MyCard(
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = RoundedCornerShape(8.dp),
    content: @Composable () -> Unit
) {
    Card(
        elevation = 8.dp, shape = shape,
        modifier = modifier, content = content
    )
}

@ExperimentalMaterialApi
@Composable
fun MyClickableCard(
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = RoundedCornerShape(8.dp),
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        elevation = 8.dp, shape = shape,
        modifier = modifier, content = content,
        onClick = onClick
    )
}

@Composable
fun DefaultCard(
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = RoundedCornerShape(6.dp),
    padding: Dp = 4.dp,
    elevation: Dp = 6.dp,
    content: @Composable () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        elevation = 6.dp,
        backgroundColor = MaterialTheme.colors.surface,
        modifier = modifier
            .zIndex(1.0f)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(padding),
        content = content
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DefaultCardClickable(
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = RoundedCornerShape(6.dp),
    padding: Dp = 4.dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        elevation = 6.dp,
        backgroundColor = MaterialTheme.colors.surface,
        onClick = onClick,
        modifier = modifier
            .zIndex(1.0f)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(padding), content = content
    )
}