package com.joesemper.fishing.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.joesemper.fishing.databinding.FragmentPlacesBinding
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.adapters.PlaceRecyclerViewItem
import com.joesemper.fishing.ui.adapters.UserPlacesRVAdapter

class UserPlacesFragment : Fragment() {

    companion object {
        private const val TAG = "PLACES"

        fun newInstance(data: List<UserMapMarker>): Fragment {
            val args = bundleOf(TAG to data)
            val fragment = UserPlacesFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var binding: FragmentPlacesBinding

    private val places = mutableListOf<UserMapMarker>()

    private lateinit var adapter: UserPlacesRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPlacesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        initRV()
        addDataToRV()
    }

    private fun initData() {
        val data = arguments?.getParcelableArrayList<UserMapMarker>(TAG)
        if (!data.isNullOrEmpty()) {
            places.addAll(data)
        }
    }

    private fun initRV() {
        adapter = UserPlacesRVAdapter { item ->
            when (item) {
                is PlaceRecyclerViewItem.ItemAddNewPlace -> {
                    Toast.makeText(requireContext(), "Add new place\nWork in progress", Toast.LENGTH_SHORT).show()
                }
                is PlaceRecyclerViewItem.ItemUserPlace -> {
                    Toast.makeText(requireContext(), "${item.place.title}\nWork in progress", Toast.LENGTH_SHORT).show()
                }
            }

        }
        binding.rvPlaces.layoutManager = LinearLayoutManager(context)
        binding.rvPlaces.adapter = adapter
    }

    private fun addDataToRV() {
        adapter.addData(places)
    }


}