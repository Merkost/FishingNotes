package com.joesemper.fishing.presentation.map.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.joesemper.fishing.databinding.FragmentCatchesInnerBinding
import com.joesemper.fishing.model.common.content.UserCatch
import com.joesemper.fishing.presentation.map.adapters.UserCatchesRVAdapter

class UserCatchesInnerFragment : Fragment() {

    companion object {
        private const val CATCHES_ARG = "CATCHES_ARG"

        fun newInstance(catches: ArrayList<UserCatch>? = arrayListOf()): Fragment {
            val args = bundleOf(CATCHES_ARG to catches)
            val fragment = UserCatchesInnerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: FragmentCatchesInnerBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var adapter: UserCatchesRVAdapter

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
    }

    private fun initRV() {
        val data = arguments?.getParcelableArrayList<UserCatch>(CATCHES_ARG)
        if (data != null) {
            adapter = UserCatchesRVAdapter(data)
            binding.rvCatches.layoutManager = LinearLayoutManager(context)
            binding.rvCatches.adapter = adapter
        }
    }
}