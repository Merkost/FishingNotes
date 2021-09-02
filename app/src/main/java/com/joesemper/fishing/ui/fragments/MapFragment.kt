package com.joesemper.fishing.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.transition.MaterialSharedAxis
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import com.joesemper.fishing.model.entity.content.Content
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.databinding.FragmentMapBinding
import com.joesemper.fishing.domain.MapViewModel
import com.joesemper.fishing.domain.viewstates.MapViewState
import com.joesemper.fishing.utils.AddNewMarkerListener
import com.joesemper.fishing.utils.Logger
import com.joesemper.fishing.utils.PermissionUtils.isPermissionGranted
import com.joesemper.fishing.utils.PermissionUtils.requestPermission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

class MapFragment : Fragment(), AndroidScopeComponent, OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback, AddNewMarkerListener {

    override val scope: Scope by fragmentScope()
    private val viewModel: MapViewModel by viewModel()

    private val logger: Logger by inject()
    private lateinit var binding: FragmentMapBinding

            private var permissionDenied = false
    private lateinit var map: GoogleMap

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var currentContent: Flow<Content>
    private val mapMarkers = mutableListOf<UserMapMarker>()

    private var isPlaceSelectMode = false

    private var currentMapMarker: Marker? = null
    private var lastKnownLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTransactions()
    }

    private fun setTransactions() {
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(layoutInflater, container, false)
        return binding.root
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

    override fun addNewMapMarker(marker: RawMapMarker) {
        viewModel.addNewMarker(marker)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
//        val geocoder = Geocoder(requireContext())
//        geocoder.getFromLocation(12.12, 12.12, 1).first().thoroughfare
        enableMyLocation()
        setOnMarkersClickListener()
        subscribeOnViewModel()
        setOnFabClickListener()
        getDeviceLocation()
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
            try {
                val userMarker = mapMarkers.first { it.id == marker.tag }
                MarkerDetailsDialogFragment.newInstance(userMarker)
                    .show(childFragmentManager, "Markers")
            } catch (e: Exception) { onError(Throwable(e)) }
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
                        onSuccess(viewState.content)
                    }
                    is MapViewState.Error -> {
                        onError(viewState.error)
                    }
                }
            }
        }
    }

    private fun onLoading() {
        binding.progressBarMap.visibility = View.VISIBLE
    }

    private suspend fun onSuccess(content: Flow<Content>?) {
        binding.progressBarMap.visibility = View.GONE
        try {
            if (content != null) {
                currentContent = content
                currentContent.collect { userContent ->
                    when (userContent) {
                        is UserMapMarker -> {
                            mapMarkers.add(userContent)
                            addMarkerOnMap(userContent)
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            Log.d("Fishing", e.message, e)
        }
    }

    private fun onError(error: Throwable) {
        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
        logger.log(error.message)
    }

    private fun addMarkerOnMap(marker: UserMapMarker) {
        val latLng = LatLng(marker.latitude, marker.longitude)
        val mapMarker = map.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(marker.title)
        )
        mapMarker?.tag = marker.id
    }

    private fun setOnFabClickListener() {
        binding.fabAddMarker.setOnClickListener {
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
        NewMarkerDialogFragment.newInstance(currentMapMarker!!.position)
            .show(childFragmentManager, "TAG")
    }

    private fun togglePlaceSelectMode() {
        if (isPlaceSelectMode) {
            isPlaceSelectMode = false
            binding.fabAddMarker.setImageResource(R.drawable.ic_baseline_add_location_24)
            map.setOnMapClickListener { }
            currentMapMarker?.remove()
            currentMapMarker = null
        } else {
            isPlaceSelectMode = true
            binding.fabAddMarker.setImageResource(R.drawable.ic_baseline_check_24)
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