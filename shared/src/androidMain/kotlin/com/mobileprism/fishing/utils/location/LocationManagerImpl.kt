package com.mobileprism.fishing.utils.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import org.kimplify.cedar.logging.Cedar
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.mobileprism.fishing.ui.home.map.LocationState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

class LocationManagerImpl(private val context: Context) : LocationManager {

    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val manager =
        context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager

    @SuppressLint("MissingPermission")
    override fun getCurrentLocationFlow(): Flow<LocationState> = flow {
        val locationPermissionsGiven = hasLocationPermissions(context)
        when {
            locationPermissionsGiven.not() -> {
                emit(LocationState.NoPermission)
            }
            manager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
                .not() -> {
                emit(LocationState.GpsNotEnabled)
            }
            else -> {
                val location = getCurrentLocation()
                emit(
                    if (location != null) {
                        LocationState.LocationGranted(location.latitude, location.longitude)
                    } else {
                        LocationState.Unavailable
                    }
                )
            }
        }
    }

    fun checkGPSEnabled(activity: Activity, onGpsEnabled: () -> Unit, onGpsDisabled: () -> Unit) {
        if (manager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER).not()) {
            onGpsDisabled()
            turnOnGPS(activity, onGpsEnabled)
        } else onGpsEnabled()
    }

    private fun hasLocationPermissions(context: Context): Boolean {
        val fineLocationGranted =
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted =
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        return fineLocationGranted || coarseLocationGranted
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(): android.location.Location? {
        val lastLocation = runCatching {
            fusedLocationProviderClient.lastLocation.await()
        }.getOrNull()

        if (lastLocation != null) return lastLocation

        return runCatching {
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationProviderClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()
        }.onFailure { exception ->
            Cedar.tag("MAP").d("Unable to get current location: ${exception.message}")
        }.getOrNull()
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
