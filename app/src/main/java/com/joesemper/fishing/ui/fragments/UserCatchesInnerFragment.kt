package com.joesemper.fishing.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.joesemper.fishing.R
import com.joesemper.fishing.databinding.FragmentCatchesInnerBinding
import com.joesemper.fishing.domain.UserCatchesViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.adapters.CatchRecyclerViewItem
import com.joesemper.fishing.ui.adapters.UserCatchesRVAdapter
import kotlinx.coroutines.flow.collect
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

class UserCatchesInnerFragment : Fragment(), AndroidScopeComponent {

    companion object {
        private const val MARKER = "MARKER"

        fun newInstance(marker: UserMapMarker): Fragment {
            val args = bundleOf(MARKER to marker)
            val fragment = UserCatchesInnerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override val scope: Scope by fragmentScope()
    private val viewModel: UserCatchesViewModel by viewModel()

    private var _binding: FragmentCatchesInnerBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var adapter: UserCatchesRVAdapter

    private lateinit var marker: UserMapMarker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        marker = arguments?.get(MARKER) as UserMapMarker
        viewModel.loadCatchesByMarkerId(marker.id)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCatchesInnerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRV()
        subscribeOnVewModel()
    }


    private fun initRV() {
        val dividerItemDecoration = DividerItemDecoration(context, RecyclerView.VERTICAL)
        dividerItemDecoration.setDrawable(getDrawable(requireContext(), R.drawable.rv_dicoration)!!)
        binding.rvCatches.addItemDecoration(dividerItemDecoration)
        adapter = UserCatchesRVAdapter { item ->
            when (item) {
                is CatchRecyclerViewItem.ItemAddNewCatch -> {
                    val action =
                        MapFragmentDirections.actionMapFragmentToNewCatchDialogFragment(
                            marker
                        )
                    findNavController().navigate(action)
                }
                is CatchRecyclerViewItem.ItemUserCatch -> {
                    val action =
                        MapFragmentDirections.actionMapFragmentToUserCatchFragment(
                            item.catch
                        )
                    findNavController().navigate(action)
                }
            }

        }
        binding.rvCatches.layoutManager = LinearLayoutManager(context)
        binding.rvCatches.adapter = adapter
    }

    private fun subscribeOnVewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.subscribe().collect { viewState ->
                when (viewState) {
                    is BaseViewState.Loading -> {

                    }
                    is BaseViewState.Success<*> -> {
                        val catches = viewState.data
                        adapter.addData(catches as List<UserCatch>)
                    }
                    is BaseViewState.Error -> {
                        val msg = viewState.error.message
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}