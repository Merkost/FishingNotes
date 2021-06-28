package com.joesemper.fishing.view.fragments

import android.Manifest
import android.content.pm.PackageManager
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
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.map.UserMarker
import com.joesemper.fishing.utils.PermissionUtils.isPermissionGranted
import com.joesemper.fishing.utils.PermissionUtils.requestPermission
import com.joesemper.fishing.view.fragments.dialogFragments.AddMarkerBottomSheetDialogFragment
import com.joesemper.fishing.view.fragments.dialogFragments.AddMarkerListener
import com.joesemper.fishing.viewmodel.map.MapViewModel
import com.joesemper.fishing.viewmodel.map.MapViewState
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.getKoin
import org.koin.core.qualifier.named

class MapFragment : Fragment(), OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback, AddMarkerListener {

    private val viewModelScope = getKoin().getOrCreateScope("MapScope", named<MapFragment>())
    private val viewModel: MapViewModel = viewModelScope.get()

    private var permissionDenied = false
    private lateinit var map: GoogleMap

    private val currentMarkers = mutableListOf<Marker?>()

    private var isPlaceSelectMode = false
    private var currentMapMarker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMap()
    }

    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()
        setOnMarkersClickListener()
        subscribeOnViewModel()
        setOnFabClickListener()
    }

    override fun addMarker(marker: UserMarker) {
        viewModel.addMarker(marker)
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
            val mark = currentMarkers.first { it?.id == marker.id }
            viewModel.deleteMarker(mark?.tag.toString())
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

    private fun addMarkersOnMap(userMarkers: List<UserMarker?>) {
        for (marker in userMarkers) {
            if (marker != null) {
                val latLng = LatLng(marker.latitude, marker.longitude)
                val mapMarker =
                    map.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(marker.title)
                            .snippet(marker.description)
                    )
                mapMarker?.tag = marker.id
                currentMarkers.add(mapMarker)
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
        val alertDialog = requireActivity().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(R.string.ok) { _, _ ->
                    toggleToPlaceSelectMode()
                }
                setNegativeButton(R.string.fui_cancel) { _, _ -> }
                setTitle("Add a marker")
                setMessage("Select a place on the map")
            }
            builder.create()
        }
        alertDialog.show()
    }

    private fun onNewMarkerPlaceSelected() {
        startBottomSheetDialogAddMarker()
        toggleToNormalMode()
    }

    private fun startBottomSheetDialogAddMarker() {
        val dialog = AddMarkerBottomSheetDialogFragment.newInstance(currentMapMarker!!.position)
        dialog.show(childFragmentManager, "TAG")
    }

    private fun toggleToPlaceSelectMode() {
        isPlaceSelectMode = true
        fab_add_marker.setImageResource(R.drawable.ic_baseline_check_24)
        setOnMapClickListener()
    }

    private fun toggleToNormalMode() {
        isPlaceSelectMode = false
        fab_add_marker.setImageResource(R.drawable.ic_baseline_add_location_24)
        map.setOnMapClickListener { }
        currentMapMarker?.remove()
        currentMapMarker = null
    }

    private fun setOnMapClickListener() {
        map.setOnMapClickListener { latLng ->
            if (currentMapMarker != null) {
                currentMapMarker?.remove()
            }
            currentMapMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("My Marker")
                    .snippet("Snippet")
            )
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        map.clear()
        viewModel.unsubscribe()
        viewModelScope.close()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

}