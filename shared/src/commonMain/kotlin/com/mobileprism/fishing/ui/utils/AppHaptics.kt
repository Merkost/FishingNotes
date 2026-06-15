package com.mobileprism.fishing.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription

@Stable
class AppHaptics(private val haptics: HapticFeedback) {
    fun performClick() {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun performConfirm() {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}

@Composable
fun rememberAppHaptics(): AppHaptics {
    val haptics = LocalHapticFeedback.current
    return remember(haptics) { AppHaptics(haptics) }
}

fun Modifier.chartSemantics(summary: String): Modifier =
    this.clearAndSetSemantics { contentDescription = summary }
