package com.joesemper.fishing.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.joesemper.fishing.databinding.FragmentPlacesBinding
import com.joesemper.fishing.domain.UserPlacesViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.adapters.PlaceRecyclerViewItem
import com.joesemper.fishing.ui.adapters.UserPlacesRVAdapter
import kotlinx.coroutines.flow.collect
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

class UserPlacesFragment : Fragment(), AndroidScopeComponent {

    companion object {
        private const val TAG = "PLACES"

        fun newInstance(): Fragment {
            return UserPlacesFragment()
        }
    }

    override val scope: Scope by fragmentScope()
    private val viewModel: UserPlacesViewModel by viewModel()

    private lateinit var binding: FragmentPlacesBinding

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

        subscribeOnViewModel()
    }

    private fun subscribeOnViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.subscribe().collect { viewState ->
                when (viewState) {
                    is BaseViewState.Loading -> onLoading()
                    is BaseViewState.Success<*> -> onSuccess(viewState.data as List<UserMapMarker>)
                    is BaseViewState.Error -> onError(viewState.error)
                }

            }
        }
    }

    private fun onLoading() {

    }

    private fun onSuccess(places: List<UserMapMarker>) {
        initRV(places)
    }

    private fun onError(error: Throwable) {

    }

    private fun initRV(data: List<UserMapMarker>) {
        adapter = UserPlacesRVAdapter(data) { item ->
            when (item) {
                is PlaceRecyclerViewItem.ItemAddNewPlace -> {
                    Toast.makeText(
                        requireContext(),
                        "Add new place\nWork in progress",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is PlaceRecyclerViewItem.ItemUserPlace -> {
                    Toast.makeText(
                        requireContext(),
                        "${item.place.title}\nWork in progress",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
        binding.rvPlaces.layoutManager = LinearLayoutManager(context)
        binding.rvPlaces.adapter = adapter
    }


}