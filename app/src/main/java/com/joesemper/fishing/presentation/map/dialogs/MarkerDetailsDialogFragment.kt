package com.joesemper.fishing.presentation.map.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.joesemper.fishing.databinding.FragmentMarkerDetailsBinding
import com.joesemper.fishing.model.common.content.UserCatch

interface DeleteMarkerListener {
    fun deleteMarker(aCatch: UserCatch)
}

class MarkerDetailsDialogFragment : BottomSheetDialogFragment() {

    companion object {
        private const val MARKERS = "MARKERS"

        fun newInstance(userCatches: List<UserCatch>): DialogFragment {
//            val bundle = Bundle()
//            bundle.putParcelableArrayList(MARKERS, (userCatches as ArrayList<UserCatch>))
            val args = bundleOf(MARKERS to (userCatches as ArrayList<UserCatch>))
            val fragment = MarkerDetailsDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: FragmentMarkerDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2

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

        initViewPager()
        initTabs()

    }

    private fun initViewPager() {
        val fragmentManager = childFragmentManager
        viewPager = binding.viewPagerMarker
        viewPager.adapter = ScreenSlidePageAdapter(fragmentManager, lifecycle)
    }

    private fun initTabs() {
        val tabLayout = binding.tabLayoutMarker
        TabLayoutMediator(tabLayout, viewPager) {tab, position ->
            when(position) {
                0 -> tab.text = "Catches"
                1 -> tab.text = "Notes"
            }
        }.attach()
    }

    private inner class ScreenSlidePageAdapter(
        fm: FragmentManager,
        lifecycle: Lifecycle
    ) : FragmentStateAdapter(fm, lifecycle) {

        val numOfTabs = 2

        val catches = arguments?.getParcelableArrayList<UserCatch>(MARKERS)

        override fun getItemCount(): Int = numOfTabs

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> UserCatchesInnerFragment.newInstance(catches)
                1 -> UserNotesInnerFragment.newInstance()
                else -> UserCatchesInnerFragment.newInstance(catches)
            }

        }

    }


}