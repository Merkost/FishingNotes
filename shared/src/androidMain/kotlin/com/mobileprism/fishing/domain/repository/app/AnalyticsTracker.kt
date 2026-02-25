package com.mobileprism.fishing.domain.repository.app

sealed interface AnalyticsEvent {
    // Auth
    data class SignInError(val errorMessage: String?) : AnalyticsEvent
    data class Login(val method: String) : AnalyticsEvent
    data class SignUp(val method: String) : AnalyticsEvent

    // Map
    data object MapSettings : AnalyticsEvent
    data object MapLayers : AnalyticsEvent

    // Markers
    data object AddMarkerNote : AnalyticsEvent
    data object EditMarkerNote : AnalyticsEvent
    data object MarkerVisibilityChange : AnalyticsEvent
    data object NewMarker : AnalyticsEvent
    data object DeleteMarker : AnalyticsEvent
    data class Navigate(val contentType: String) : AnalyticsEvent
    data class Share(val contentType: String) : AnalyticsEvent

    // Catches
    data object NewCatch : AnalyticsEvent
    data object NewCatchOffline : AnalyticsEvent

    // Photos
    data class UploadPhotos(val count: Int) : AnalyticsEvent

    // Weather / Solunar
    data object GetWeather : AnalyticsEvent
    data object GetSolunar : AnalyticsEvent
    data object GetFreeWeather : AnalyticsEvent
}

interface AnalyticsTracker {
    fun logEvent(event: AnalyticsEvent)
}
