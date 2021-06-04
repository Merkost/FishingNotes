package com.joesemper.fishing.utils

import android.content.Intent
import com.firebase.ui.auth.AuthUI
import com.joesemper.fishing.R

fun getLoginActivityIntent(): Intent {
    val providers = getProvidersList()
    return AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setLogo(R.drawable.ic_fishing)
        .setAvailableProviders(providers)
        .build()
}

private fun getProvidersList() = arrayListOf(
    AuthUI.IdpConfig.EmailBuilder().build(),
    AuthUI.IdpConfig.GoogleBuilder().build(),
    AuthUI.IdpConfig.AnonymousBuilder().build()
)