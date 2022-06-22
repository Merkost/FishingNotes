package com.mobileprism.fishing.utils

import com.google.firebase.auth.FirebaseAuth
import com.mobileprism.fishing.domain.entity.common.LoginPassword

sealed class LoginPasswordCheckResult() {
    object Success : LoginPasswordCheckResult()
    object LoginError : LoginPasswordCheckResult()
    object PasswordError : LoginPasswordCheckResult()
}

fun getCurrentUser() = FirebaseAuth.getInstance().currentUser

fun getCurrentUserId() = getCurrentUser()?.uid ?: "Anonymous"

fun checkLoginPasswordCorrectInput(
    loginPassword: LoginPassword
): LoginPasswordCheckResult {

    if (!isLoginInputCorrect(loginPassword.login) && !isEmailInputCorrect(loginPassword.password)) {
        return LoginPasswordCheckResult.LoginError
    }

    if (!isPasswordInputCorrect(loginPassword.password)) {
        return LoginPasswordCheckResult.PasswordError
    }

    return LoginPasswordCheckResult.Success
}

fun isLoginInputCorrect(login: String): Boolean {
    val loginPattern = """^[a-zA-Z][a-zA-Z0-9-_\.]{1,20}${'$'}""".toRegex()

    return loginPattern.matches(login)
}

fun isEmailInputCorrect(email: String): Boolean {
    val emailPattern = """^[-\w.]+@([A-z0-9][-A-z0-9]+\.)+[A-z]{2,4}${'$'}""".toRegex()

    return emailPattern.matches(email)
}

fun isPasswordInputCorrect(password: String): Boolean {
    val passwordPattern = """^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\s).*${'$'}""".toRegex()

    return passwordPattern.matches(password)
}


