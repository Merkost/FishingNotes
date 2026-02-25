package com.mobileprism.fishing.model.datasource.firebase

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.mobileprism.fishing.domain.repository.app.AnalyticsEvent
import com.mobileprism.fishing.domain.repository.app.AnalyticsTracker

class FirebaseAnalyticsTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsTracker {

    override fun logEvent(event: AnalyticsEvent) {
        when (event) {
            is AnalyticsEvent.SignInError -> {
                val bundle = Bundle().apply {
                    putString(FirebaseAnalytics.Param.SCORE, event.errorMessage)
                }
                firebaseAnalytics.logEvent("signin_error", bundle)
            }

            is AnalyticsEvent.Login -> {
                val bundle = Bundle().apply {
                    putString(FirebaseAnalytics.Param.METHOD, event.method)
                }
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
            }

            is AnalyticsEvent.SignUp -> {
                val bundle = Bundle().apply {
                    putString(FirebaseAnalytics.Param.METHOD, event.method)
                }
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)
            }

            is AnalyticsEvent.MapSettings -> {
                firebaseAnalytics.logEvent("map_settings", null)
            }

            is AnalyticsEvent.MapLayers -> {
                firebaseAnalytics.logEvent("map_layers", null)
            }

            is AnalyticsEvent.AddMarkerNote -> {
                firebaseAnalytics.logEvent("add_marker_note", null)
            }

            is AnalyticsEvent.EditMarkerNote -> {
                firebaseAnalytics.logEvent("edit_marker_note", null)
            }

            is AnalyticsEvent.MarkerVisibilityChange -> {
                firebaseAnalytics.logEvent("marker_visibility_change", null)
            }

            is AnalyticsEvent.NewMarker -> {
                firebaseAnalytics.logEvent("new_marker", null)
            }

            is AnalyticsEvent.DeleteMarker -> {
                firebaseAnalytics.logEvent("delete_marker", null)
            }

            is AnalyticsEvent.Navigate -> {
                val bundle = Bundle().apply {
                    putString(FirebaseAnalytics.Param.CONTENT_TYPE, event.contentType)
                }
                firebaseAnalytics.logEvent("navigate", bundle)
            }

            is AnalyticsEvent.Share -> {
                val bundle = Bundle().apply {
                    putString(FirebaseAnalytics.Param.CONTENT_TYPE, event.contentType)
                }
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)
            }

            is AnalyticsEvent.NewCatch -> {
                firebaseAnalytics.logEvent("new_catch", null)
            }

            is AnalyticsEvent.NewCatchOffline -> {
                firebaseAnalytics.logEvent("new_catch_offline", null)
            }

            is AnalyticsEvent.UploadPhotos -> {
                val bundle = Bundle().apply {
                    putString(FirebaseAnalytics.Param.SCORE, event.count.toString())
                }
                firebaseAnalytics.logEvent("upload_photos", bundle)
            }

            is AnalyticsEvent.GetWeather -> {
                firebaseAnalytics.logEvent("get_weather", null)
            }

            is AnalyticsEvent.GetSolunar -> {
                firebaseAnalytics.logEvent("get_solunar", null)
            }

            is AnalyticsEvent.GetFreeWeather -> {
                firebaseAnalytics.logEvent("get_free_weather", null)
            }
        }
    }
}
