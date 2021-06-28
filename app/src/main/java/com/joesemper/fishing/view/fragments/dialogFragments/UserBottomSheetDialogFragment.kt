package com.joesemper.fishing.view.fragments.dialogFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.user.User
import kotlinx.android.synthetic.main.fragment_bottom_sheet_dialog_user.*

class UserBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        const val ARG = "USER_ARG"
        fun newInstance(user: User): BottomSheetDialogFragment {
            val args = Bundle()
            args.putParcelable(ARG, user)
            val fragment = UserBottomSheetDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet_dialog_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = arguments?.getParcelable(ARG) as User?

        if (user != null) {

            when (user.isAnonymous) {
                true -> doOnAnonymousUser(user)
                false -> doOnSimpleUser(user)
            }
        }

        button_logout.setOnClickListener {
            (activity as LogoutListener).onLogout()
            dismiss()
        }

    }

    private fun doOnAnonymousUser(user: User) {
        iv_user_pic.load(R.drawable.ic_fisher)
        tv_username.text = "Guest"
        button_logout.text = "Login"
    }

    private fun doOnSimpleUser(user: User) {
        iv_user_pic.load(user.userPic) {
            placeholder(R.drawable.ic_fisher)
            transformations(CircleCropTransformation())
        }
        tv_username.text = user.userName
    }
}