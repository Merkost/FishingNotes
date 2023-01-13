package com.mobileprism.fishing.utils

import com.google.firebase.auth.FirebaseAuth


fun getCurrentUser() = FirebaseAuth.getInstance().currentUser

fun getCurrentUserId() = getCurrentUser()?.uid ?: "Anonymous"

fun checkLoginInputType(input: String) =
    when {
        input.contains("@") -> LoginInputType.Email
        else -> LoginInputType.Username
    }

sealed class LoginInputType {
    object Username : LoginInputType()
    object Email : LoginInputType()
}


