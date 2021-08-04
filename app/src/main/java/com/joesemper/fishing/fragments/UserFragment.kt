package com.joesemper.fishing.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.joesemper.fishing.R
import com.joesemper.fishing.SplashActivity
import com.joesemper.fishing.data.entity.common.User
import com.joesemper.fishing.databinding.FragmentUserBinding
import com.joesemper.fishing.viewmodels.UserViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

class UserFragment : Fragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScope()
    private val viewModel: UserViewModel by viewModel()

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeOnViewModel()
        setOnLogoutButtonListener()
    }

    private fun subscribeOnViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.getCurrentUser().collect { user ->
                if (user != null) {
                    when (user.isAnonymous) {
                        true -> doOnAnonymousUser(user)
                        false -> doOnSimpleUser(user)
                    }
                } else {
                    startSplashActivity()
                }

            }
        }
    }

    private fun setOnLogoutButtonListener() {
        binding.buttonLogout.setOnClickListener {
            lifecycleScope.launch{
                viewModel.logoutCurrentUser()
            }

        }
    }

    private fun startSplashActivity() {
        val intent = Intent(requireContext(), SplashActivity::class.java)
        startActivity(intent)
    }

    private fun doOnAnonymousUser(user: User) {
        binding.ivUserPic.load(R.drawable.ic_fisher)
        binding.tvUsername.text = "Guest"
        binding.buttonLogout.text = "Login"
    }

    private fun doOnSimpleUser(user: User) {
        binding.ivUserPic.load(user.userPic) {
            placeholder(R.drawable.ic_fisher)
            transformations(CircleCropTransformation())
        }
       binding.tvUsername.text = user.userName
    }
}