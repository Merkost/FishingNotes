package com.joesemper.fishing.view.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.joesemper.fishing.utils.createUserMarker
import com.joesemper.fishing.viewmodel.map.MapViewModel
import com.joesemper.fishing.viewmodel.map.MapViewState
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.getKoin
import org.koin.core.qualifier.named

class MapFragment : Fragment(), OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private val viewModelScope = getKoin().getOrCreateScope("MapScope", named<MapFragment>())
    private val viewModel: MapViewModel = viewModelScope.get()

    private var permissionDenied = false
    private lateinit var map: GoogleMap

    private val currentMarkers = mutableListOf<Marker?>()

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
        setOnMapClickListener()
        setOnMarkersClickListener()
        subscribeOnViewModel()
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

    private fun setOnMapClickListener() {
        map.setOnMapLongClickListener { latLng ->
            viewModel.addMarker(createUserMarker(latLng))
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
            for (marker in userMarkers) {
                if (marker != null) {
                    val latLng = LatLng(marker.latitude, marker.longitude)
                    val mapMarker =
                        map.addMarker(MarkerOptions().position(latLng).title("My Marker"))
                    mapMarker?.tag = marker.id
                    currentMarkers.add(mapMarker)
                }
            }
        }
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