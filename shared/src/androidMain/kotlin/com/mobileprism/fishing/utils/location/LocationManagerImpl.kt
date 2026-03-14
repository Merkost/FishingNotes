package com.mobileprism.fishing.utils.location

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.widget.Toast
import org.kimplify.cedar.logging.Cedar
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.mobileprism.fishing.R
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import com.mobileprism.fishing.ui.home.SnackbarManager
import com.mobileprism.fishing.ui.home.map.LocationState
import com.mobileprism.fishing.ui.home.map.checkLocationPermissions
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

class LocationManagerImpl(private val context: Context) : LocationManager {

    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val manager =
        context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager

    @SuppressLint("MissingPermission")
    override fun getCurrentLocationFlow(): Flow<LocationState> = flow {
        val locationPermissionsGiven = checkLocationPermissions(context).not()
        when {
            manager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
                .not() -> {
                emit(LocationState.GpsNotEnabled)
            }
            locationPermissionsGiven -> {
                val locationResult = fusedLocationProviderClient.lastLocation.await()
                try {
                    emit(LocationState.LocationGranted(locationResult.latitude, locationResult.longitude))
                } catch (e: Exception) {
                    Cedar.tag("MAP").d("GPS is off")
                    Toast.makeText(
                        context,
                        R.string.cant_get_current_location,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> { emit(LocationState.NoPermission) }
        }
    }

    fun checkGPSEnabled(activity: Activity, onGpsEnabled: () -> Unit) {
        if (manager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER).not()) {
            SnackbarManager.showMessage(Res.string.gps_is_off)
            turnOnGPS(activity, onGpsEnabled)
        } else onGpsEnabled()
    }

    private fun turnOnGPS(activity: Activity, onGpsEnabled: () -> Unit) {
        val request = LocationRequest.create().apply {
            interval = 8000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(request)
        val client: SettingsClient = LocationServices.getSettingsClient(context)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(
                        activity,
                        12345
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }.addOnSuccessListener {
            onGpsEnabled()
        }
    }
}
