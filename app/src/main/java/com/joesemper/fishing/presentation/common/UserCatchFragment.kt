package com.joesemper.fishing.presentation.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.joesemper.fishing.databinding.FragmentUserCatchBinding
import com.joesemper.fishing.model.common.content.UserCatch

class UserCatchFragment : Fragment() {

    companion object {
        private const val CATCH = "CATCH"

        fun newInstance(catch: UserCatch): Fragment {
            val args = bundleOf(CATCH to catch)
            val fragment = UserCatchFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: FragmentUserCatchBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var catch: UserCatch

    init {

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserCatchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun setInitialData() {


    }
}