package com.mobileprism.fishing.model.datasource.firebase

import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker

class IosAnalyticsTracker : AnalyticsTracker {
    override fun logEvent(event: AnalyticsEvent) { }
}
