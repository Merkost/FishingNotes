package com.joesemper.fishing.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.joesemper.fishing.R
import com.joesemper.fishing.databinding.FragmentNotesBinding
import com.joesemper.fishing.domain.NotesViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import kotlinx.coroutines.flow.collect
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

class NotesFragment : Fragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScope()
    private val viewModel: NotesViewModel by viewModel()

    private lateinit var binding: FragmentNotesBinding

    private lateinit var viewPager: ViewPager2
    private lateinit var screenSlidePageAdapter: NotesFragment.ScreenSlidePageAdapter

    val markers = mutableListOf<UserMapMarker>()
    val catches = mutableListOf<UserCatch>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolbar()
        subscribeOnViewModel()
    }

    private fun initToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun subscribeOnViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.subscribe().collect { viewState ->
                when (viewState) {
                    is BaseViewState.Loading -> onLoading()
                    is BaseViewState.Success<*> -> onSuccess()
                    is BaseViewState.Error -> onError(viewState.error)
                }

            }
        }
    }

    private fun onLoading() {
        showLoading()
    }

    private fun onSuccess() {
        initViews()
        hideLoading()
    }

    private fun onError(error: Throwable) {
        hideLoading()
        showError(error)
    }

    private fun initViews() {
        initViewPager()
        initTabs()
    }

    private fun initViewPager() {
        val fragmentManager = childFragmentManager
        screenSlidePageAdapter = ScreenSlidePageAdapter(fragmentManager, lifecycle)
        viewPager = binding.viewPagerNotes
        viewPager.adapter = screenSlidePageAdapter
    }

    private fun initTabs() {
        val tabLayout = binding.tabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.places)
                1 -> tab.text = getString(R.string.catches)
            }
        }.attach()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showError(error: Throwable) {
        Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
    }


    private inner class ScreenSlidePageAdapter(
        fm: FragmentManager,
        lifecycle: Lifecycle,
    ) : FragmentStateAdapter(fm, lifecycle) {

        val numOfTabs = 2
        override fun getItemCount(): Int = numOfTabs

        val placesFragment = UserPlacesFragment.newInstance()
        val catchesFragment = UserCatchesFragment.newInstance()

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> placesFragment
                1 -> catchesFragment
                else -> placesFragment
            }

        }

    }
}