package com.joesemper.fishing.presentation.map.marker

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.joesemper.fishing.databinding.FragmentMarkerDetailsBinding
import com.joesemper.fishing.model.common.content.UserCatch
import com.joesemper.fishing.model.common.content.UserMapMarker
import com.joesemper.fishing.presentation.map.MapFragmentDirections
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope
import java.util.*

class MarkerDetailsDialogFragment : BottomSheetDialogFragment(), AndroidScopeComponent {

    companion object {
        private const val MARKER = "MARKER"

        fun newInstance(marker: UserMapMarker): DialogFragment {
            val args = bundleOf(MARKER to marker)
            val fragment = MarkerDetailsDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override val scope: Scope by fragmentScope()
    private val viewModel: MarkerDetailsViewModel by viewModel()

    private var _binding: FragmentMarkerDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2

    private lateinit var marker: UserMapMarker

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarkerDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        marker = arguments?.get(MARKER) as UserMapMarker

        setData()
        subscribeOnVewModel()
        setOnNewCatchListener()

    }

    private fun setData() {
        binding.tvTitle.text = marker.title
        binding.tvDescription.text = marker.description
    }

    private fun subscribeOnVewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.subscribe(marker.id).collect { viewState ->
                when (viewState) {
                    is MarkerDetailsViewState.Loading -> {
                    }
                    is MarkerDetailsViewState.Success -> {
                        binding.progressBarMarker.visibility = View.GONE
                        initViewPager(viewState.content)
                        initTabs()
                    }
                    is MarkerDetailsViewState.Error -> {
                        val msg = viewState.error.message
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun initViewPager(catches: Flow<UserCatch>) {
        val fragmentManager = childFragmentManager
        viewPager = binding.viewPagerMarker
        viewPager.adapter = ScreenSlidePageAdapter(fragmentManager, lifecycle, catches)
    }

    private fun initTabs() {
        val tabLayout = binding.tabLayoutMarker
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Catches"
                1 -> tab.text = "Notes"
            }
        }.attach()
    }

    private fun setOnNewCatchListener() {
        binding.buttonAddNewCatch.setOnClickListener {
            val action = MapFragmentDirections.actionMapFragmentToNewCatchDialogFragment(marker)
            findNavController().navigate(action)
        }

        binding.buttonNavigate.setOnClickListener {
            startMapsActivityForNavigation()
        }
    }

    private fun startMapsActivityForNavigation() {
        val uri = String.format(
            Locale.ENGLISH,
            "http://maps.google.com/maps?daddr=%f,%f (%s)",
            marker.latitude,
            marker.longitude,
            marker.title
        )
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")
        try {
            startActivity(intent);
        } catch (e: ActivityNotFoundException) {
            try {
                val unrestrictedIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                startActivity(unrestrictedIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Please install a maps application", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private inner class ScreenSlidePageAdapter(
        fm: FragmentManager,
        lifecycle: Lifecycle,
        val catches: Flow<UserCatch>
    ) : FragmentStateAdapter(fm, lifecycle) {

        val numOfTabs = 2

        override fun getItemCount(): Int = numOfTabs

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> UserCatchesInnerFragment(catches)
                1 -> UserNotesInnerFragment.newInstance()
                else -> UserCatchesInnerFragment(catches)
            }

        }

    }


}