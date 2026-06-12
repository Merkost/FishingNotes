package com.mobileprism.fishing.ui.home.place

import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker

actual fun openMapNavigation(marker: UserMapMarker, analyticsTracker: AnalyticsTracker) { }

actual fun shareMarkerLocation(marker: UserMapMarker, analyticsTracker: AnalyticsTracker) { }
