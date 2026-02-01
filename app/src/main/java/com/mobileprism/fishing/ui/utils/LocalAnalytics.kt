package com.mobileprism.fishing.ui.utils

import androidx.compose.runtime.staticCompositionLocalOf
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker

val LocalAnalytics = staticCompositionLocalOf<AnalyticsTracker> {
    error("No AnalyticsTracker provided")
}
