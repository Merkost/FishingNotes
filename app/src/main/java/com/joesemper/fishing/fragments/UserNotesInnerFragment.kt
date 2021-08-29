package com.joesemper.fishing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.joesemper.fishing.databinding.FragmentNotesInnerBinding

class UserNotesInnerFragment: Fragment() {

    companion object {
        private const val CATCHES_ARG = "CATCHES_ARG"

        fun newInstance(): Fragment {
            val args = Bundle()
//            args.putParcelableArrayList(CATCHES_ARG, catches)
            val fragment = UserNotesInnerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: FragmentNotesInnerBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesInnerBinding.inflate(inflater, container, false)
        return binding.root
    }

}