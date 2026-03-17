package com.mobileprism.fishing.model.datasource.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker

class FirebaseAnalyticsTracker : AnalyticsTracker {

    private val analytics = Firebase.analytics

    override fun logEvent(event: AnalyticsEvent) {
        when (event) {
            is AnalyticsEvent.SignInError -> {
                analytics.logEvent("signin_error", mapOf("score" to (event.errorMessage ?: "")))
            }
            is AnalyticsEvent.Login -> {
                analytics.logEvent("login", mapOf("method" to event.method))
            }
            is AnalyticsEvent.SignUp -> {
                analytics.logEvent("sign_up", mapOf("method" to event.method))
            }
            is AnalyticsEvent.MapSettings -> analytics.logEvent("map_settings")
            is AnalyticsEvent.MapLayers -> analytics.logEvent("map_layers")
            is AnalyticsEvent.AddMarkerNote -> analytics.logEvent("add_marker_note")
            is AnalyticsEvent.EditMarkerNote -> analytics.logEvent("edit_marker_note")
            is AnalyticsEvent.MarkerVisibilityChange -> analytics.logEvent("marker_visibility_change")
            is AnalyticsEvent.NewMarker -> analytics.logEvent("new_marker")
            is AnalyticsEvent.DeleteMarker -> analytics.logEvent("delete_marker")
            is AnalyticsEvent.Navigate -> {
                analytics.logEvent("navigate", mapOf("content_type" to event.contentType))
            }
            is AnalyticsEvent.Share -> {
                analytics.logEvent("share", mapOf("content_type" to event.contentType))
            }
            is AnalyticsEvent.NewCatch -> analytics.logEvent("new_catch")
            is AnalyticsEvent.NewCatchOffline -> analytics.logEvent("new_catch_offline")
            is AnalyticsEvent.UploadPhotos -> {
                analytics.logEvent("upload_photos", mapOf("score" to event.count.toString()))
            }
            is AnalyticsEvent.GetWeather -> analytics.logEvent("get_weather")
            is AnalyticsEvent.GetSolunar -> analytics.logEvent("get_solunar")
            is AnalyticsEvent.GetFreeWeather -> analytics.logEvent("get_free_weather")
        }
    }
}
