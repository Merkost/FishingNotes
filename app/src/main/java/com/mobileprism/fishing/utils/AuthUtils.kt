package com.mobileprism.fishing.utils

import com.google.firebase.auth.FirebaseAuth

const val LOGIN_MIN_LENGTH = 3
const val LOGIN_MAX_LENGTH = 20
const val PASSWORD_MIN_LENGTH = 6
const val PASSWORD_MAX_LENGTH = 20

fun getCurrentUser() = FirebaseAuth.getInstance().currentUser

fun getCurrentUserId() = getCurrentUser()?.uid ?: "Anonymous"

fun isLoginInputCorrect(login: String): Boolean {
    val loginPattern =
        "^(?=.{$LOGIN_MIN_LENGTH,$LOGIN_MAX_LENGTH}${'$'})(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$".toRegex()

    return loginPattern.matches(login)
}

fun isEmailInputCorrect(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun isPasswordInputCorrect(password: String): Boolean {
    val passwordPattern =
        "^[A-Za-z0-9!@#$%^&]{$PASSWORD_MIN_LENGTH,$PASSWORD_MAX_LENGTH}\$".toRegex()

    return passwordPattern.matches(password)
}


