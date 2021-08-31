package com.joesemper.fishing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.joesemper.fishing.data.entity.weather.Daily
import com.joesemper.fishing.databinding.FragmentWeatherInnerBinding
import com.joesemper.fishing.view.weather.utils.getWeatherIconByName

class WeatherFragmentInner: Fragment() {

    private lateinit var binding: FragmentWeatherInnerBinding

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
    ): View {
        binding = FragmentWeatherInnerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        renderData()
    }

    private fun renderData() {
        val weather = arguments?.getParcelable(WEATHER_ARG) as Daily? ?: return

        val user = FirebaseAuth.getInstance().currentUser

        binding.ivWeatherIconMain.setImageResource(getWeatherIconByName(weather.weather.first().icon))
        binding.tvWeatherDescription.text = weather.weather.first().description
        binding.tvTemperature.text = "Temperature: ${weather.temperature.max}°C"
        binding.tvWindSpeed.text = "Wind Speed: ${weather.windSpeed}m/s"
        binding.tvPressure.text = "Pressure: ${weather.pressure}"
    }
}