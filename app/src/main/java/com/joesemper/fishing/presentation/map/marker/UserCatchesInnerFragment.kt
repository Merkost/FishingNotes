package com.joesemper.fishing.presentation.map.marker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.joesemper.fishing.databinding.FragmentCatchesInnerBinding
import com.joesemper.fishing.model.common.content.UserCatch
import com.joesemper.fishing.presentation.map.adapters.UserCatchesRVAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

class UserCatchesInnerFragment(private val catchesFlow: Flow<UserCatch>) : Fragment() {


    private var _binding: FragmentCatchesInnerBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var adapter: UserCatchesRVAdapter

    private val catches = mutableListOf<UserCatch>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatchesInnerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRV()

        lifecycleScope.launchWhenStarted {
            catchesFlow.collect {
                catches.add(it)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun initRV() {
        adapter = UserCatchesRVAdapter(catches)
        binding.rvCatches.layoutManager = LinearLayoutManager(context)
        binding.rvCatches.adapter = adapter

    }
}