package com.mobileprism.fishing.utils

import com.google.firebase.auth.FirebaseAuth

fun getCurrentUser() = FirebaseAuth.getInstance().currentUser

fun getCurrentUserId() = getCurrentUser()?.uid ?: "Anonymous"


