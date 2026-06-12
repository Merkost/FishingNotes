package com.mobileprism.fishing.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset

object Motion {
    const val short: Int = 150
    const val medium: Int = 250
    const val long: Int = 400

    val standardEasing: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val emphasizedEasing: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val accelerateEasing: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    fun <T> screenEnter(): FiniteAnimationSpec<T> =
        tween(durationMillis = medium, easing = emphasizedEasing)

    fun <T> screenExit(): FiniteAnimationSpec<T> =
        tween(durationMillis = short, easing = accelerateEasing)

    fun <T> enterContent(): FiniteAnimationSpec<T> =
        tween(durationMillis = medium, easing = standardEasing)

    fun navIndicatorOffset(): FiniteAnimationSpec<IntOffset> =
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)

    fun navIndicatorDp(): FiniteAnimationSpec<Dp> =
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)

    fun navIndicatorFloat(): FiniteAnimationSpec<Float> =
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
}
