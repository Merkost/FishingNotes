package com.joesemper.fishing.ui.fragments

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.joesemper.fishing.R
import com.joesemper.fishing.databinding.FragmentMapBinding
import com.joesemper.fishing.compose.viewmodels.MapViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.content.Content
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.utils.Logger
import com.joesemper.fishing.utils.PermissionUtils.isPermissionGranted
import com.joesemper.fishing.utils.PermissionUtils.requestPermission
import com.joesemper.fishing.utils.expand
import com.joesemper.fishing.utils.hide
import com.joesemper.fishing.utils.show
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope
import java.util.*


class MapFragment : Fragment(), AndroidScopeComponent, OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val DEFAULT_ZOOM = 15
    }

    override val scope: Scope by fragmentScope()
    private val viewModel: MapViewModel by viewModel()

    private val logger: Logger by inject()
    private lateinit var binding: FragmentMapBinding

    private var permissionDenied = false
    private lateinit var map: GoogleMap

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var currentContent: Flow<Content>
    private val mapMarkers = mutableListOf<UserMapMarker>()

    private val placeSelectMode = MutableStateFlow(false)

    private var lastKnownLocation: Location? = null

    private lateinit var placeDialogBehavior: BottomSheetBehavior<ConstraintLayout>

    private lateinit var geocoder: Geocoder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMapBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBottomDialogBehaviour()
        initLocationProvider()
        initMap()
        initGeocoder()
    }

    private fun initBottomDialogBehaviour() {
        val placeDialog = binding.dialogPlace.bottomSheetPlace
        placeDialogBehavior = BottomSheetBehavior.from(placeDialog)
        placeDialogBehavior.apply {
            isHideable = true
            hide()
        }
    }

    private fun initLocationProvider() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun initGeocoder() {
        geocoder = Geocoder(requireContext())
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        subscribeOnPlaceSelectMode()
        enableMyLocation()
        subscribeOnViewModel()
        setOnLayersClickListener()
        setOnFabClickListener()
        moveCameraToCurrentLocation()
    }

    private fun subscribeOnPlaceSelectMode() {
        lifecycleScope.launchWhenStarted {
            placeSelectMode.collect { isPlaceSelectMode ->
                when (isPlaceSelectMode) {
                    true -> onPlaceSelectMode()
                    false -> onSimpleMode()
                }
            }
        }
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

    private fun setOnMarkersClickListener() {
        map.setOnMapClickListener {
            placeDialogBehavior.hide()
        }

        map.setOnMarkerClickListener { marker ->
            onMapMarkerClicked(marker)
            true
        }
    }

    private fun removeMarkerClickListener() {
        map.setOnMarkerClickListener { true }
    }

    private fun setOnLayersClickListener() {
        binding.buttonLayers.setOnClickListener {
            onSelectMapType()
        }
    }

    private fun setOnFabClickListener() {
        binding.fabAddMarker.setOnClickListener {
            if (placeSelectMode.value) {
                navigateToNewPlaceFragment()
            }
            placeSelectMode.value = !placeSelectMode.value
        }
    }

    private fun subscribeOnViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.subscribe().collect { viewState ->
                when (viewState) {
                    is BaseViewState.Loading -> {
                        onLoading()
                    }
                    is BaseViewState.Success<*> -> {
                        onSuccess(content = viewState.data as Flow<Content>?)
                    }
                    is BaseViewState.Error -> {
                        onError(viewState.error)
                    }
                }
            }
        }
    }

    private fun onLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private suspend fun onSuccess(content: Flow<Content>?) {
        binding.progressBar.visibility = View.GONE
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

    private fun onPlaceSelectMode() {
        removeMarkerClickListener()
        moveCameraToCurrentLocation()
        map.setOnCameraIdleListener {
            setViewsVisibilityOnPlaceSelectMode()
            setViewsBehaviourOnPlaceSelectMode()
        }
    }

    private fun setViewsVisibilityOnPlaceSelectMode() {
        binding.fabAddMarker.setImageResource(R.drawable.ic_baseline_check_24)
        binding.tvLocationTitle.show()
        binding.tvLocation.show()
        binding.ivPointer.show()
    }

    private fun setViewsBehaviourOnPlaceSelectMode() {
        map.setOnCameraMoveStartedListener {
            onCameraMoveStartViewsBehaviour()
        }

        map.setOnCameraIdleListener {
            onCameraStopViewsBehaviour()
            setPointerLocation()
        }
    }

    private fun onCameraMoveStartViewsBehaviour() {
        binding.ivPointer.pauseAnimation()
        binding.ivPointer.setMinAndMaxFrame(0, 20)
        binding.ivPointer.playAnimation()

        binding.tvLocation.text = getString(R.string.calculating)
    }

    private fun setPointerLocation() {
        try {
            val position = geocoder.getFromLocation(
                getCameraPosition().latitude,
                getCameraPosition().longitude,
                1
            )

            if (position.first().subAdminArea.isNullOrBlank()) {
                binding.tvLocation.text = position.first().adminArea
            } else {
                binding.tvLocation.text = position.first().subAdminArea
            }


        } catch (e: Throwable) {
            binding.tvLocation.text = getString(R.string.failed_to_determine)
            onError(e)
        }
    }

    private fun onCameraStopViewsBehaviour() {
        binding.ivPointer.pauseAnimation()
        binding.ivPointer.setMinAndMaxFrame(50, 82)
        binding.ivPointer.playAnimation()
    }

    private fun onSimpleMode() {
        setOnMarkersClickListener()
        setViewsVisibilityOnSimpleMode()
        setViewsBehaviourOnSimpleMode()
    }

    private fun setViewsVisibilityOnSimpleMode() {
        binding.tvLocationTitle.hide()
        binding.tvLocation.hide()
        binding.ivPointer.hide()
    }

    private fun setViewsBehaviourOnSimpleMode() {
        map.setOnCameraMoveStartedListener { }

        map.setOnCameraIdleListener { }
    }

    private fun navigateToNewPlaceFragment() {
        val title = binding.tvLocation.text.toString()
        val coordinates = getCameraPosition()

        val action =
            MapFragmentDirections.actionMapFragmentToNewPlaceFragment(coordinates, title)
        findNavController().navigate(action)
    }

    private fun getCameraPosition(): LatLng {
        return map.cameraPosition.target
    }

    private fun moveCameraToCurrentLocation() {
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
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
            LatLng(
                location.latitude,
                location.longitude
            ), DEFAULT_ZOOM.toFloat()
        )
        map.animateCamera(cameraUpdate)
    }

    private fun showToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    private fun onMapMarkerClicked(marker: Marker) {
        try {
            val userMarker = mapMarkers.first { it.id == marker.tag }

            with(binding.dialogPlace) {

                bottomSheetPlace.setOnClickListener {
                    val action =
                        MapFragmentDirections.actionMapFragmentToUserPlaceFragment(userMarker)
                    findNavController().navigate(action)
                }

                tvPlaceTitle.text = userMarker.title
                tvPlaceDescription.text = userMarker.description

                chipAddNewCatch.setOnClickListener {
                    val action =
                        MapFragmentDirections.actionMapFragmentToNewCatchDialogFragment(
                            userMarker
                        )
                    findNavController().navigate(action)
                }

                chipRoute.setOnClickListener {
                    startMapsActivityForNavigation(userMarker)
                }

                chipDetails.setOnClickListener {
                    val action =
                        MapFragmentDirections.actionMapFragmentToUserPlaceFragment(userMarker)
                    findNavController().navigate(action)
                }
            }
            placeDialogBehavior.expand()

        } catch (e: Exception) {
            onError(Throwable(e))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
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

    private fun onSelectMapType() {
        val singleItems = arrayOf("Normal", "Satellite", "Hybrid", "Terrain")
        val checkedItem = when (map.mapType) {
            GoogleMap.MAP_TYPE_NORMAL -> 0
            GoogleMap.MAP_TYPE_SATELLITE -> 1
            GoogleMap.MAP_TYPE_HYBRID -> 2
            GoogleMap.MAP_TYPE_TERRAIN -> 3
            else -> 1
        }

        var selected = checkedItem

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.map_types))
            .setPositiveButton(getString(R.string.ok)) { dialog, which ->
                when (selected) {
                    0 -> map.mapType = GoogleMap.MAP_TYPE_NORMAL
                    1 -> map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    2 -> map.mapType = GoogleMap.MAP_TYPE_HYBRID
                    3 -> map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.cancel()
            }
            .setSingleChoiceItems(singleItems, checkedItem) { dialog, which ->
                selected = which
            }
            .show()
    }

    private fun startMapsActivityForNavigation(mapMarker: UserMapMarker) {
        val uri = String.format(
            Locale.ENGLISH,
            "http://maps.google.com/maps?daddr=%f,%f (%s)",
            mapMarker.latitude,
            mapMarker.longitude,
            mapMarker.title
        )
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            try {
                val unrestrictedIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                startActivity(unrestrictedIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Please install a maps application", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

}