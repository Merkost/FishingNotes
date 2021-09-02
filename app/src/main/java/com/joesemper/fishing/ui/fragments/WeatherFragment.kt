package com.joesemper.fishing.ui.fragments

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialFadeThrough
import com.joesemper.fishing.R
import com.joesemper.fishing.databinding.FragmentWeatherBinding
import com.joesemper.fishing.domain.WeatherViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import com.joesemper.fishing.utils.Logger
import com.joesemper.fishing.view.weather.utils.getDateByMilliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

class WeatherFragment : Fragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScope()
    private val viewModel: WeatherViewModel by viewModel()
    private val logger: Logger by inject()

    private lateinit var binding: FragmentWeatherBinding

    private lateinit var currentWeather: WeatherForecast
    private lateinit var viewPager: ViewPager2

    private val markers = mutableListOf<UserMapMarker>()
    private val locations: List<String>
        get() = markers.map { it.title }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var lastKnownLocation: Location
    private var permissionDenied = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough()
        returnTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModel()
    }

    @ExperimentalCoroutinesApi
    private fun initViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.getAllMarkers().collect { userMarkers ->
                markers.addAll(userMarkers.map { it as UserMapMarker })
                initLocationSelection()
            }
        }
    }

    @ExperimentalCoroutinesApi
    @SuppressLint("MissingPermission")
    private fun initLocationSelection() {
        if (markers.isEmpty()) {
            val adapter = ArrayAdapter(requireContext(), R.layout.list_item, locations)
            binding.location.setAdapter(adapter)
            binding.location.setText("Текущее местоположение", false)
            initLocationProvider()
            lifecycleScope.launchWhenStarted {
                try {
                    getCurrentLocation().collect { location ->
                        val weather = viewModel.getWeather(location.latitude, location.longitude)
                            .collect { doOnSuccess(it) }
                    }
                } catch (e: Throwable) {
                    doOnError(e)
                    TODO("COROUTINE CANCELED")
                }
            }
        } else {
            lifecycleScope.launchWhenStarted {
                viewModel.getMarkerWeather(markers.first()).collect { doOnSuccess(it) }
            }
            val adapter = ArrayAdapter(requireContext(), R.layout.list_item, locations)
            binding.location.setAdapter(adapter)
            binding.location.setText(markers.first().title, false)
            binding.location.setOnItemClickListener { parent, view, position, id ->
                lifecycleScope.launchWhenStarted {
                    viewModel.getMarkerWeather(markers[position]).collect {
                        doOnSuccess(it)
                    }
                }
            }
        }
    }


//    @FlowPreview
//    @ExperimentalCoroutinesApi
//    fun getAllUserContent()
//    = provider.getAllUserCatches()
//        .flatMapMerge { userCatch ->
//
//        flow {
//            emit(userCatch as Content)
//            val markerId = userCatch.userMarkerId
//            if (!markers.contains(markerId) and markerId.isNotBlank()) {
//                try {
//                    markers.add(markerId)
//                    provider.getMarker(markerId).collect { marker ->
//                        emit(marker as Content)
//                    }
//                } catch (e: Throwable) {
//                    Log.d("Fishing", e.message, e)
//                }
//            }
//        }
//    }

    @ExperimentalCoroutinesApi
    @SuppressLint("MissingPermission")
    fun getCurrentLocation() = channelFlow {
        val listener: Task<Location>

        val locationResult = fusedLocationProviderClient.lastLocation
        listener = locationResult.addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                trySend(task.result)
            }
        }
        awaitClose {}
    }

    private fun initLocationProvider() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
    }


    private fun renderData(weatherViewState: BaseViewState) {
        when (weatherViewState) {
            is BaseViewState.Success<*> -> doOnSuccess(weatherViewState.data as WeatherForecast?)
            is BaseViewState.Loading -> doOnLoading()
            is BaseViewState.Error -> doOnError(weatherViewState.error)
        }
    }

    private fun doOnSuccess(weather: WeatherForecast?) {
        if (weather == null) return
        currentWeather = weather
        initViewPager()
        initTabs()
    }

    private fun doOnLoading(progress: Int? = null) {
        Toast.makeText(context, "Loading", Toast.LENGTH_SHORT).show()
    }

    private fun doOnError(error: Throwable) {
        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
        Log.d("Fishing", error.message.toString())
    }

    private fun initViewPager() {
        val fragmentManager = childFragmentManager
        viewPager = requireActivity().findViewById(R.id.view_pager_weather)
        viewPager.adapter = ScreenSlidePageAdapter(fragmentManager, lifecycle)
    }

    private fun initTabs() {
        TabLayoutMediator(binding.tabLayoutWeather, viewPager) { tab, position ->
            val dateInMilliseconds = currentWeather.daily[position].date
            tab.text = getDateByMilliseconds(dateInMilliseconds)
        }.attach()
    }

    private inner class ScreenSlidePageAdapter(
        fm: FragmentManager,
        lifecycle: Lifecycle,
    ) : FragmentStateAdapter(fm, lifecycle) {

        override fun getItemCount(): Int = currentWeather.daily.size

        override fun createFragment(position: Int): Fragment =
            WeatherFragmentInner.newInstance(currentWeather.daily[position])
    }


}