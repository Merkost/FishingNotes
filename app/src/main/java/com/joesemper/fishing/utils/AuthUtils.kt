package com.joesemper.fishing.utils

import android.content.Intent
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.joesemper.fishing.R

fun getCurrentUser() = FirebaseAuth.getInstance().currentUser

fun getCurrentUserId() = getCurrentUser()?.uid ?: "Anonymous"


