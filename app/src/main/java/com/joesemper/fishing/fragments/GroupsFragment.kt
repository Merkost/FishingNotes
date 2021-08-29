package com.joesemper.fishing.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.joesemper.fishing.R
import com.joesemper.fishing.data.entity.common.User
import com.joesemper.fishing.viewmodels.GroupsViewModel
import com.joesemper.fishing.viewmodels.viewstates.GroupsViewState
import com.joesemper.fishing.SplashActivity
import kotlinx.android.synthetic.main.fragment_groups.*

class GroupsFragment : Fragment(), LogoutListener {

    private lateinit var currentViewModel: GroupsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_groups, container, false)
    }


//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.item_logout -> {
//                childFragmentManager.findFragmentByTag(LogoutDialog.TAG)
//                    ?: LogoutDialog.newInstance()
//                        .show((this).childFragmentManager, LogoutDialog.TAG)
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    override fun onLogout() {

    }

    private fun initViewModel() {
//        val viewModel: GroupsViewModel by currentScope.inject()
//        currentViewModel = viewModel
//        viewModel.subscribe().observe(this as LifecycleOwner) { renderData(it) }
    }

    private fun renderData(state: GroupsViewState) {
        when (state) {
            is GroupsViewState.Success -> doOnSuccess(state.userData)
            is GroupsViewState.Loading -> doOnLoading()
            is GroupsViewState.Error -> doOnError(state.error)
        }
    }

    private fun doOnSuccess(userData: User) {
        tv_uid.text = userData.userId
        tv_display_name.text = userData.userName
    }

    private fun doOnLoading(progress: Int? = null) {
        Toast.makeText(context, "Loading", Toast.LENGTH_SHORT).show()
    }

    private fun doOnError(error: Throwable) {
        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
        Log.d("Fishing", error.message.toString())
    }

    private fun doOnLogout() {
        startActivity(Intent(requireContext(), SplashActivity::class.java))
        activity?.finish()
    }
}