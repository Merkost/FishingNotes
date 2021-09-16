package com.joesemper.fishing.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context.INPUT_METHOD_SERVICE
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
import android.view.inputmethod.InputMethodManager
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
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.transition.MaterialSharedAxis
import com.joesemper.fishing.R
import com.joesemper.fishing.databinding.FragmentMapBinding
import com.joesemper.fishing.domain.MapViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.content.Content
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import com.joesemper.fishing.utils.*
import com.joesemper.fishing.utils.PermissionUtils.isPermissionGranted
import com.joesemper.fishing.utils.PermissionUtils.requestPermission
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

    private lateinit var addMarkerBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var buttonsCreateCancelBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var placeDialogBehavior: BottomSheetBehavior<ConstraintLayout>

    private lateinit var geocoder: Geocoder

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
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMapBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAddNewMarkerDialog()
        initLocationProvider()
        initMap()
        initGeocoder()
    }

    private fun initAddNewMarkerDialog() {
        setBottomDialogOnFocusListeners()
        initBottomDialogBehaviour()
    }

    private fun setBottomDialogOnFocusListeners() {
        binding.etNewMarkerDescription.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                addMarkerBottomSheetBehavior.expand()
            }
        }
        binding.etNewMarkerTitle.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                addMarkerBottomSheetBehavior.expand()
            }
        }
    }

    private fun initBottomDialogBehaviour() {
        binding.bottomSheetAddMarker.setOnClickListener { }

        val bottomSheet = binding.bottomSheetAddMarker
        addMarkerBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        addMarkerBottomSheetBehavior.apply {
            isHideable = true
            halfExpandedRatio = 0.4f
            hide()
        }

        val buttons = binding.dialogCreateNewPlace.bottomSheet
        buttonsCreateCancelBehavior = BottomSheetBehavior.from(buttons)
        buttonsCreateCancelBehavior.apply {
            isHideable = true
            hide()
        }

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

    private fun addNewMapMarker(marker: RawMapMarker) {
        viewModel.addNewMarker(marker)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        subscribeOnPlaceSelectMode()
        enableMyLocation()
        subscribeOnViewModel()
        setOnFabClickListener()
        moveCameraToCurrentLocation()
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

    private fun setOnMarkersClickListener() {
        map.setOnMapClickListener {
            placeDialogBehavior.hide()
        }

        map.setOnMarkerClickListener { marker ->
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
            true
        }
    }

    private fun removeMarkerClickListener() {
        map.setOnMarkerClickListener { true }
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

    private fun onPlaceSelectMode() {
        removeMarkerClickListener()
        hideNavigation()
        moveCameraToCurrentLocation()
        setViewsVisibilityOnPlaceSelectMode()
        setViewsBehaviourOnPlaceSelectMode()
        setOnNewPlaceDialogButtonsListener()
    }

    private fun setViewsVisibilityOnPlaceSelectMode() {
        binding.fabAddMarker.hide()
        binding.tvLocationTitle.show()
        binding.tvLocation.show()
        binding.ivPointer.show()
    }

    private fun setViewsBehaviourOnPlaceSelectMode() {
        addMarkerBottomSheetBehavior.isHideable = false
        addMarkerBottomSheetBehavior.setPeekHeight(235, true)
        addMarkerBottomSheetBehavior.halfExpand()
        buttonsCreateCancelBehavior.expand()



        map.setOnCameraMoveStartedListener {
            onCameraMoveStartViewsBehaviour()
        }

        map.setOnCameraIdleListener {
            onCameraStopViewsBehaviour()
        }
    }

    private fun onCameraMoveStartViewsBehaviour() {
        addMarkerBottomSheetBehavior.setPeekHeight(50, true)
        addMarkerBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        buttonsCreateCancelBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        binding.tvLocation.text = getString(R.string.calculating)
    }

    private fun onCameraStopViewsBehaviour() {
        addMarkerBottomSheetBehavior.setPeekHeight(235, true)
        addMarkerBottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        buttonsCreateCancelBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        val position = geocoder.getFromLocation(
            getCameraPosition().latitude,
            getCameraPosition().longitude,
            1
        )
        binding.tvLocation.text = position.first().subAdminArea
    }

    private fun setOnNewPlaceDialogButtonsListener() {
        binding.dialogCreateNewPlace.buttonNewCatchCreate.setOnClickListener {
            val title = binding.etNewMarkerTitle.text.toString()
            val description = binding.etNewMarkerDescription.text.toString()
            val lat = map.cameraPosition.target.latitude
            val lon = map.cameraPosition.target.longitude
            addNewMapMarker(
                RawMapMarker(
                    title = title,
                    description = description,
                    latitude = lat,
                    longitude = lon
                )
            )
            placeSelectMode.value = false
        }

        binding.dialogCreateNewPlace.buttonNewCatchCancel.setOnClickListener {
            placeSelectMode.value = false
        }
    }

    private fun onSimpleMode() {
        setOnMarkersClickListener()
        showNavigation()
        setViewsVisibilityOnSimpleMode()
        setViewsBehaviourOnSimpleMode()
    }

    private fun setViewsVisibilityOnSimpleMode() {
        binding.fabAddMarker.show()
        binding.tvLocationTitle.hide()
        binding.tvLocation.hide()
        binding.ivPointer.hide()
    }

    private fun setViewsBehaviourOnSimpleMode() {
        //        addMarkerBottomSheetBehavior.skipCollapsed = true
        addMarkerBottomSheetBehavior.isHideable = true
        addMarkerBottomSheetBehavior.hide()
        buttonsCreateCancelBehavior.hide()

        map.setOnCameraMoveStartedListener { }

        map.setOnCameraIdleListener { }
    }

    private fun setOnFabClickListener() {
        binding.fabAddMarker.setOnClickListener {
            placeSelectMode.value = true
        }
    }

    private fun getCameraPosition(): LatLng {
        return map.cameraPosition.target
    }

    private fun hideNavigation() {
        val activity = requireActivity() as NavigationHolder
        activity.hideNav()
    }

    private fun showNavigation() {
        val activity = requireActivity() as NavigationHolder
        activity.showNav()
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

    private fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
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