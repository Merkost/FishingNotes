package com.mobileprism.fishing.ui.utils.motion

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavBackStackEntry
import com.mobileprism.fishing.ui.theme.Motion

object AppNavTransitions {

    val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = tween(Motion.medium)) +
            slideInVertically(
                animationSpec = tween(Motion.medium),
                initialOffsetY = { fullHeight -> fullHeight / 12 }
            )
    }

    val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(Motion.short))
    }

    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = tween(Motion.medium))
    }

    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(Motion.short))
    }
}

fun Modifier.slideUpFadeIn(visible: Boolean = true): Modifier = composed {
    val progress = remember(visible) {
        androidx.compose.animation.core.Animatable(if (visible) 0f else 1f)
    }
    androidx.compose.runtime.LaunchedEffect(visible) {
        progress.animateTo(if (visible) 1f else 0f, tween(Motion.medium))
    }
    graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 24f
    }
}
