package com.joesemper.fishing.view.weather

import android.os.Bundle
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
import com.joesemper.fishing.R
import com.joesemper.fishing.model.weather.entity.WeatherForecast
import com.joesemper.fishing.model.weather.entity.WeatherState
import com.joesemper.fishing.view.weather.utils.getDateByMilliseconds
import com.joesemper.fishing.view.weather.utils.getWeatherIconByName
import com.joesemper.fishing.viewmodel.weather.WeatherViewModel
import kotlinx.android.synthetic.main.fragment_weather.*
import org.koin.android.scope.currentScope

class WeatherFragment : Fragment() {

    private lateinit var currentWeather: WeatherForecast
    private lateinit var viewPager: ViewPager2

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
        val viewModel: WeatherViewModel by currentScope.inject()
        viewModel.subscribe().observe(this as LifecycleOwner, {
            renderData(it)
        })
    }

    private fun renderData(weatherState: WeatherState) {
        when (weatherState) {
            is WeatherState.Success -> doOnSuccess(weatherState.data)
            is WeatherState.Loading -> doOnLoading()
            is WeatherState.Error -> doOnError(weatherState.error)
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