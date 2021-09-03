package com.joesemper.fishing.ui.fragments

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.joesemper.fishing.databinding.FragmentMarkerDetailsBinding
import com.joesemper.fishing.domain.MarkerDetailsViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.content.UserMapMarker
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

    private lateinit var screenSlidePageAdapter: ScreenSlidePageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        marker = arguments?.get(MARKER) as UserMapMarker
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dial ->
            val d = dial as BottomSheetDialog
            val bottomSheet =
                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.peekHeight = 1000
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMarkerDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewPager()
        initTabs()
        setData()
        subscribeOnVewModel()
        setOnClickListeners()
    }

    private fun setData() {
        binding.tvTitle.text = marker.title
        binding.tvDescription.text = marker.description
    }

    private fun subscribeOnVewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.subscribe(marker.id).collect { viewState ->
                when (viewState) {
                    is BaseViewState.Loading -> {
                        binding.progressBarMarker.visibility = View.GONE
                    }
                    is BaseViewState.Success<*> -> {
                        binding.progressBarMarker.visibility = View.GONE
                    }
                    is BaseViewState.Error -> {
                        val msg = viewState.error.message
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setOnClickListeners() {
        setOnNewCatchListener()
        setOnRouteClickListener()
        setOnShareClickListener()
    }

    private fun initViewPager() {
        val fragmentManager = childFragmentManager
        screenSlidePageAdapter = ScreenSlidePageAdapter(fragmentManager, lifecycle)
        viewPager = binding.viewPagerMarker
        viewPager.adapter = screenSlidePageAdapter
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
        binding.chipAddNewCatch.setOnClickListener {
            val action =
                MapFragmentDirections.actionMapFragmentToNewCatchDialogFragment(
                    marker
                )
            findNavController().navigate(action)
        }

    }

    private fun setOnRouteClickListener() {
        binding.chipRoute.setOnClickListener {
            startMapsActivityForNavigation()
        }

    }

    private fun setOnShareClickListener() {
        binding.chipShare.setOnClickListener {
            val text =
                "${marker.title}\nhttps://www.google.com/maps/search/?api=1&query=${marker.latitude},${marker.longitude}"

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
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
            startActivity(intent)
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
    ) : FragmentStateAdapter(fm, lifecycle) {

        val numOfTabs = 2
        override fun getItemCount(): Int = numOfTabs

        val catchesFragment = UserCatchesInnerFragment.newInstance(marker)
        val notesFragment = UserNotesInnerFragment.newInstance()

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> catchesFragment
                1 -> notesFragment
                else -> catchesFragment
            }

        }

    }


}