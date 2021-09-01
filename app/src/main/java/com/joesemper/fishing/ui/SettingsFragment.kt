package com.joesemper.fishing.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceFragmentCompat
import com.joesemper.fishing.R
import com.joesemper.fishing.databinding.FragmentSettingsBinding
import com.joesemper.fishing.utils.showToast

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var binding: FragmentSettingsBinding

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val oldLayout = super.onCreateView(inflater, container, savedInstanceState)
        val newLayout = inflater.inflate(R.layout.fragment_settings, container, false) as ViewGroup
        binding = FragmentSettingsBinding.bind(newLayout)
        newLayout.addView(oldLayout)
        return newLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setListeners()
        super.onViewCreated(view, savedInstanceState)


    }

    private fun setListeners() {
        binding.toolbarSettings.setNavigationOnClickListener { findNavController().popBackStack()
        showToast(requireContext(),"Clicked")
        }
    }
}