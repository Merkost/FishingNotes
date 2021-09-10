package com.joesemper.fishing.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.joesemper.fishing.databinding.FragmentCatchesBinding
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

class UserCatchesFragment : Fragment(), AndroidScopeComponent {

    companion object {
        private const val TAG = "CATCHES"

        fun newInstance(): Fragment {
            return UserCatchesFragment()
        }
    }

    override val scope: Scope by fragmentScope()
    private val viewModel: UserCatchesViewModel by viewModel()

    private lateinit var binding: FragmentCatchesBinding

    private lateinit var adapter: UserCatchesRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCatchesBinding.inflate(inflater, container, false)
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
                    is BaseViewState.Success<*> -> onSuccess(viewState.data as List<UserCatch>)
                    is BaseViewState.Error -> onError(viewState.error)
                }

            }
        }
    }

    private fun onLoading() {

    }

    private fun onSuccess(catches: List<UserCatch>) {
        initRV(catches)
    }

    private fun onError(error: Throwable) {

    }

    private fun initRV(data: List<UserCatch>) {
        adapter = UserCatchesRVAdapter(data) { item ->
            when (item) {
                is CatchRecyclerViewItem.ItemAddNewCatch -> onAddNewCatchClick()
                is CatchRecyclerViewItem.ItemUserCatch -> onCatchItemClick(item.catch)
            }

        }
        binding.rvCatches.layoutManager = LinearLayoutManager(context)
        binding.rvCatches.adapter = adapter
    }

    private fun onAddNewCatchClick() {
        val action =
            NotesFragmentDirections.actionNotesFragmentToNewCatchDialogFragment(
                UserMapMarker()
            )
        findNavController().navigate(action)
    }

    private fun onCatchItemClick(catch: UserCatch) {
        val action =
            NotesFragmentDirections.actionNotesFragmentToUserCatchFragment(catch)
        findNavController().navigate(action)
    }


}