package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.mobileprism.fishing.ui.theme.BrandGradients
import com.mobileprism.fishing.ui.theme.Spacing

@Composable
fun BrandGradientCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    contentPadding: Dp = Spacing.cardPadding,
    content: @Composable BoxScope.() -> Unit,
) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
        Box(
            modifier = modifier
                .clip(shape)
                .background(BrandGradients.primaryDiagonal(MaterialTheme.colorScheme))
                .padding(contentPadding),
            content = content,
        )
    }
}

@Composable
fun BrandGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BrandGradients.primaryDiagonal(MaterialTheme.colorScheme)),
            content = content,
        )
    }
}
