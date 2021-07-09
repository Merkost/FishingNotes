package com.joesemper.fishing.view.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.transition.MaterialFadeThrough
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.map.UserMarker
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.model.repository.weather.WeatherRepository
import com.joesemper.fishing.model.repository.weather.api.WeatherApiService
import com.joesemper.fishing.utils.Logger
import com.joesemper.fishing.utils.PermissionUtils.isPermissionGranted
import com.joesemper.fishing.utils.PermissionUtils.requestPermission
import com.joesemper.fishing.view.fragments.dialogFragments.AddMarkerBottomSheetDialogFragment
import com.joesemper.fishing.view.fragments.dialogFragments.AddMarkerListener
import com.joesemper.fishing.view.fragments.dialogFragments.DeleteMarkerListener
import com.joesemper.fishing.view.fragments.dialogFragments.MarkerDetailsDialogFragment
import com.joesemper.fishing.viewmodel.map.MapViewModel
import com.joesemper.fishing.viewmodel.map.MapViewState
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.coroutines.flow.collect
import okhttp3.OkHttpClient
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapFragment : Fragment(), AndroidScopeComponent, OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback, AddMarkerListener, DeleteMarkerListener {

    override val scope: Scope by fragmentScope()
    private val viewModel: MapViewModel by viewModel()

    private val logger: Logger by inject()

    private var permissionDenied = false
    private lateinit var map: GoogleMap

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val currentMarkers = mutableListOf<UserMarker?>()

    private var isPlaceSelectMode = false

    private var currentMapMarker: Marker? = null
    private var lastKnownLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough()
        returnTransition = MaterialFadeThrough()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLocationProvider()
        initMap()
    }

    private fun initLocationProvider() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val geocoder = Geocoder(requireContext())
        geocoder.getFromLocation(12.12, 12.12, 1).first().thoroughfare
        enableMyLocation()
        setOnMarkersClickListener()
        subscribeOnViewModel()
        setOnFabClickListener()
        getDeviceLocation()
    }

    override fun addMarker(marker: UserMarker) {
        viewModel.addMarker(marker)
    }

    override fun deleteMarker(marker: UserMarker) {
        viewModel.deleteMarker(marker)
    }

    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        } else {
            requestPermission(
                requireActivity() as AppCompatActivity, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            enableMyLocation()
        } else {
            permissionDenied = true
        }
    }

    private fun setOnMarkersClickListener() {
        map.setOnMarkerClickListener { marker ->
            val userMarker = currentMarkers.first { it?.id == marker.tag }
            if (userMarker != null) {
                MarkerDetailsDialogFragment.newInstance(userMarker)
                    .show(childFragmentManager, "Markers")
            }
            true
        }
    }

    private fun subscribeOnViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.subscribe().collect { viewState ->
                when (viewState) {
                    is MapViewState.Loading -> {
                        onLoading()
                    }
                    is MapViewState.Success -> {
                        onSuccess(viewState.userMarkers)
                    }
                    is MapViewState.Error -> {
                        onError(viewState.error)
                    }
                }
            }
        }
    }

    private fun onLoading() {
        Toast.makeText(context, "Loading!!!", Toast.LENGTH_SHORT).show()
    }

    private fun onSuccess(userMarkers: List<UserMarker?>) {
        map.clear()
        currentMarkers.clear()
        if (userMarkers.isNotEmpty()) {
            addMarkersOnMap(userMarkers)
        }
    }

    private fun onError(error: Throwable) {
        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
        logger.log(error.message)
    }

    private fun addMarkersOnMap(userMarkers: List<UserMarker?>) {
        for (marker in userMarkers) {
            if (marker != null) {
                val mapMarker =
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(marker.latitude, marker.longitude))
                            .title(marker.title)
                            .snippet(marker.description)
                    )
                mapMarker?.tag = marker.id
                currentMarkers.add(marker)
            }
        }
    }

    private fun setOnFabClickListener() {
        fab_add_marker.setOnClickListener {
            if (isPlaceSelectMode) {
                if (currentMapMarker != null) {
                    onNewMarkerPlaceSelected()
                } else {
                    showToast("Select a place on the map!")
                }
            } else {
                showAddMarkerAlertDialog()
            }

        }
    }

    private fun showAddMarkerAlertDialog() {
        requireActivity().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(R.string.ok) { _, _ ->
                    togglePlaceSelectMode()
                }
                setNegativeButton(R.string.fui_cancel) { _, _ -> }
                setTitle("Add a marker")
                setMessage("Select a place on the map")
            }
            builder.create()
        }.show()
    }

    private fun onNewMarkerPlaceSelected() {
        startBottomSheetDialogAddMarker()
        togglePlaceSelectMode()
    }

    private fun startBottomSheetDialogAddMarker() {
        AddMarkerBottomSheetDialogFragment
            .newInstance(currentMapMarker!!.position)
            .show(childFragmentManager, "TAG")
    }

    private fun togglePlaceSelectMode() {
        if (isPlaceSelectMode) {
            isPlaceSelectMode = false
            fab_add_marker.setImageResource(R.drawable.ic_baseline_add_location_24)
            map.setOnMapClickListener { }
            currentMapMarker?.remove()
            currentMapMarker = null
        } else {
            isPlaceSelectMode = true
            fab_add_marker.setImageResource(R.drawable.ic_baseline_check_24)
            setOnMapClickListener()
        }

    }

    private fun setOnMapClickListener() {
        map.setOnMapClickListener { latLng ->
            if (currentMapMarker != null) {
                currentMapMarker?.remove()
            }
            currentMapMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
            )
        }
    }

    private fun getDeviceLocation() {
        if (permissionDenied) return
        try {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    lastKnownLocation = task.result
                    if (lastKnownLocation != null) {
                       moveCameraToLocation(lastKnownLocation!!)
                    }
                }
            }

        } catch (e: SecurityException) {
            logger.log(e.message)
        }
    }

    private fun moveCameraToLocation(location: Location) {
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    location.latitude,
                    location.longitude
                ), DEFAULT_ZOOM.toFloat()
            )
        )
    }

    private fun showToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val DEFAULT_ZOOM = 15
    }

}