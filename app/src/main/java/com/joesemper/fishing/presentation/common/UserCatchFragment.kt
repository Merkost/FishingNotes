package com.joesemper.fishing.presentation.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.joesemper.fishing.databinding.FragmentUserCatchBinding
import com.joesemper.fishing.model.common.content.UserCatch
import com.joesemper.fishing.utils.NavigationHolder

class UserCatchFragment : Fragment() {

    private val args: UserCatchFragmentArgs by navArgs()

    private var _binding: FragmentUserCatchBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var catch: UserCatch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        catch = args.userCatch
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

        (requireActivity() as NavigationHolder).closeNav()
        setInitialData()
    }

    private fun setInitialData() {
        binding.tvTitle.text = catch.title
        if (catch.description.isNotBlank()) {
            binding.tvDescription.text = catch.description
        }
        "${catch.date} ${catch.time}".also { binding.tvDateTime.text = it }
        binding.tvFish.text = catch.fishType
        "Amount: ${catch.fishAmount} PC".also { binding.tvAmount.text = it }
        "${catch.fishWeight} kg".also { binding.tvWeight.text = it }
        if(catch.fishingRodType.isNotBlank()) {
            binding.tvRod.text = catch.fishingRodType
        }
        if(catch.fishingLure.isNotBlank()) {
            binding.tvLure.text = catch.fishingLure
        }
        if (catch.fishingBait.isNotBlank()) {
            binding.tvBait.text = catch.fishingBait
        }


    }

    override fun onDetach() {
        super.onDetach()
        (requireActivity() as NavigationHolder).showNav()
    }
}