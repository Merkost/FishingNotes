package com.mobileprism.fishing.ui.home.place

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.NavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.ui.Arguments
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.navigate
import java.util.*

fun newCatchClicked(navController: NavController, place: UserMapMarker) {
        navController.navigate(MainDestinations.NEW_CATCH_ROUTE, Arguments.PLACE to place)
}

fun onRouteClicked(context: Context, marker: UserMapMarker) {
    val bundle = Bundle()
    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "marker")
    Firebase.analytics.logEvent("navigate", bundle)

    val uri = String.format(
        Locale.ENGLISH,
        "http://maps.google.com/maps?daddr=%f,%f (%s)",
        marker.latitude,
        marker.longitude,
        marker.title
    )
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    intent.setPackage("com.google.android.apps.maps")
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        try {
            val unrestrictedIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            context.startActivity(unrestrictedIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Please install a maps application", Toast.LENGTH_LONG)
                .show()
        }
    }
}


fun onShareClicked(
    context: Context,
    marker: UserMapMarker
) {
    val bundle = Bundle()
    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "marker")
    Firebase.analytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)

    val text =
        "${marker.title}\nhttps://www.google.com/maps/search/?api=1&query=${marker.latitude}" +
                ",${marker.longitude}"
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}

fun onCatchItemClick(catch: UserCatch, navController: NavController) {
    navController.navigate(MainDestinations.CATCH_ROUTE, Arguments.CATCH to catch)
}
