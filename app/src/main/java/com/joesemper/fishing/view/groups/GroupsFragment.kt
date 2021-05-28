package com.joesemper.fishing.view.groups

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.joesemper.fishing.R
import com.joesemper.fishing.view.groups.utils.LogoutDialog
import com.joesemper.fishing.view.groups.utils.LogoutListener
import com.joesemper.fishing.view.splash.SplashActivity
import kotlinx.android.synthetic.main.fragment_groups.*

class GroupsFragment : Fragment(), LogoutListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_groups, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser

        tv_uid.text = user?.uid
        tv_display_name.text = user?.displayName
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_groups, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_logout -> {
                childFragmentManager.findFragmentByTag(LogoutDialog.TAG)
                    ?: LogoutDialog.newInstance()
                        .show((this).childFragmentManager, LogoutDialog.TAG)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onLogout() {
        FirebaseAuth.getInstance().signOut()

        AuthUI.getInstance()
            .signOut(requireContext())
            .addOnCompleteListener {
                startActivity(Intent(requireContext(), SplashActivity::class.java))
                activity?.finish()
            }
    }
}