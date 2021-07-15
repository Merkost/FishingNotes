package com.joesemper.fishing.presentation.weather

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialFadeThrough
import com.joesemper.fishing.R
import com.joesemper.fishing.model.weather.WeatherForecast
import com.joesemper.fishing.view.weather.utils.getDateByMilliseconds
import kotlinx.android.synthetic.main.fragment_weather.*
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

class WeatherFragment : Fragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScope()
    private val viewModel: WeatherViewModel by viewModel()


    private lateinit var currentWeather: WeatherForecast
    private lateinit var viewPager: ViewPager2

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
        return inflater.inflate(R.layout.fragment_weather, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModel()
    }

    private fun initViewModel() {
        viewModel.subscribe().observe(this as LifecycleOwner) { renderData(it) }
    }

    private fun renderData(weatherViewState: WeatherViewState) {
        when (weatherViewState) {
            is WeatherViewState.Success -> doOnSuccess(weatherViewState.data)
            is WeatherViewState.Loading -> doOnLoading()
            is WeatherViewState.Error -> doOnError(weatherViewState.error)
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
        TabLayoutMediator(tab_layout_weather, viewPager) {tab, position ->
            val dateInMilliseconds = currentWeather.daily[position].date
            tab.text = getDateByMilliseconds(dateInMilliseconds)
        }.attach()
    }

    private inner class ScreenSlidePageAdapter(
        fm: FragmentManager,
        lifecycle: Lifecycle
    ) : FragmentStateAdapter(fm, lifecycle) {

        override fun getItemCount(): Int = currentWeather.daily.size

        override fun createFragment(position: Int): Fragment =
            WeatherFragmentInner.newInstance(currentWeather.daily[position])
    }


}