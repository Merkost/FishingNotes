package com.joesemper.fishing.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.joesemper.fishing.R
import com.joesemper.fishing.databinding.FragmentCatchesBinding
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.ui.adapters.CatchRecyclerViewItem
import com.joesemper.fishing.ui.adapters.UserCatchesRVAdapter

class UserCatchesFragment : Fragment() {

    companion object {
        private const val TAG = "CATCHES"

        fun newInstance(data: List<UserCatch>): Fragment {
            val args = bundleOf(TAG to data)
            val fragment = UserCatchesFragment()
            fragment.arguments = args
            return fragment
        }
    }


    private lateinit var binding: FragmentCatchesBinding

    private val catches = mutableListOf<UserCatch>()

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

        initData()
        initRV()
        addDataToRV()
    }

    private fun initData() {
        val data = arguments?.getParcelableArrayList<UserCatch>(TAG)
        if (!data.isNullOrEmpty()) {
            catches.addAll(data)
        }
    }

    private fun initRV() {
        adapter = UserCatchesRVAdapter { item ->
            when (item) {
                is CatchRecyclerViewItem.ItemAddNewCatch -> {
                    Toast.makeText(
                        requireContext(),
                        "Add new catch\nWork in progress",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is CatchRecyclerViewItem.ItemUserCatch -> {
                    val catchFragment = UserCatchFragment.newInstance(item.catch)
                }
            }

        }
        binding.rvCatches.layoutManager = LinearLayoutManager(context)
        binding.rvCatches.adapter = adapter
    }

    private fun addDataToRV() {
        adapter.addData(catches)
    }
}