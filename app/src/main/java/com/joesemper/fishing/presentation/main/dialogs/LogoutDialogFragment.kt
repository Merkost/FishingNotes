package com.joesemper.fishing.view.fragments.dialogFragments

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.joesemper.fishing.R


interface LogoutListener {
    fun onLogout()
}

class LogoutDialog : DialogFragment() {
    companion object {
        const val KEY = "LOGOUT_KEY"
        val TAG = LogoutDialog::class.java.name + "TAG"
        fun newInstance() = LogoutDialog()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(context as AppCompatActivity)
            .setTitle(R.string.logout_dialog_title)
            .setMessage(R.string.logout_dialog_message)
            .setPositiveButton(getString(R.string.ok_button)) { _, _ -> (activity as LogoutListener).onLogout() }
            .setNegativeButton(R.string.logout_dialog_cancel) { _, _ -> dismiss() }
            .create()
}


