package com.joesemper.fishing.presentation.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.joesemper.fishing.R
import com.joesemper.fishing.model.weather.Daily

import com.joesemper.fishing.view.weather.utils.getWeatherIconByName
import kotlinx.android.synthetic.main.fragment_weather_inner.*

class WeatherFragmentInner: Fragment() {

    companion object {
        private const val WEATHER_ARG = "WEATHER_ARG"

        fun newInstance(weather: Daily): Fragment {
            val args = Bundle()
            args.putParcelable(WEATHER_ARG, weather)
            val fragment = WeatherFragmentInner()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_weather_inner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        renderData()
    }

    private fun renderData() {
        val weather = arguments?.getParcelable(WEATHER_ARG) as Daily? ?: return

        val user = FirebaseAuth.getInstance().currentUser

        iv_weather_icon_main.setImageResource(getWeatherIconByName(weather.weather.first().icon))
        tv_weather_description.text = weather.weather.first().description
        tv_temperature.text = "Temperature: ${weather.temperature.max}°C"
        tv_wind_speed.text = "Wind Speed: ${weather.windSpeed}m/s"
        tv_pressure.text = "Pressure: ${weather.pressure}"
    }
}