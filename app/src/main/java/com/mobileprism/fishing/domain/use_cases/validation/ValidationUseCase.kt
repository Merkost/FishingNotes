package com.mobileprism.fishing.domain.use_cases.validation

import android.content.Context
import android.util.Patterns
import com.mobileprism.fishing.R
import com.mobileprism.fishing.utils.*

class ValidationUseCase(private val context: Context) {

    companion object {

        const val LOGIN_MIN_LENGTH = 3
        const val LOGIN_MAX_LENGTH = 20
        const val PASSWORD_MIN_LENGTH = 8
        const val PASSWORD_MAX_LENGTH = 25
        const val OTP_LENGTH = 6


        private val loginPattern =
            "^(?=.{$LOGIN_MIN_LENGTH,$LOGIN_MAX_LENGTH}${'$'})(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$".toRegex()

        private val CharSequence?.isValidEmail: Boolean
            get() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

        private val CharSequence.containsLettersAndDigits: Boolean
            get() = any { it.isDigit() } && any { it.isLetter() }

    }

    fun validateUsername(login: String): ValidationResult {
        if (login.length < LOGIN_MIN_LENGTH) {
            return ValidationResult(
                successful = false,
                errorMessage = String.format(
                    context.getString(R.string.login_min_length_error),
                    LOGIN_MIN_LENGTH
                )
            )
        }
        if (login.length > LOGIN_MAX_LENGTH) {
            return ValidationResult(
                successful = false,
                errorMessage = String.format(
                    context.getString(R.string.login_max_length_error),
                    LOGIN_MAX_LENGTH
                )
            )
        }
        if (loginPattern.matches(login).not()) {
            return ValidationResult(
                successful = false,
                errorMessage = context.getString(R.string.login_invalid_error)
            )
        }
        return ValidationResult(
            successful = true
        )
    }

    fun validateEmail(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = context.getString(R.string.empty_email_error)
            )
        }
        if (email.isValidEmail.not()) {
            return ValidationResult(
                successful = false,
                errorMessage = context.getString(R.string.email_error)
            )
        }
        return ValidationResult(
            successful = true
        )
    }

    fun validatePassword(password: String): ValidationResult {
        if (password.length < PASSWORD_MIN_LENGTH) {
            return ValidationResult(
                successful = false,
                errorMessage = String.format(
                    context.getString(R.string.password_min_length_error),
                    PASSWORD_MIN_LENGTH
                )
            )
        }
        if (password.length > PASSWORD_MAX_LENGTH) {
            return ValidationResult(
                successful = false,
                errorMessage = String.format(
                    context.getString(R.string.password_max_length_error),
                    PASSWORD_MAX_LENGTH
                )
            )
        }
        if (password.containsLettersAndDigits.not()) {
            return ValidationResult(
                successful = false,
                errorMessage = context.getString(R.string.password_need_didgit_or_letter_error)
            )
        }
        return ValidationResult(
            successful = true
        )
    }

    fun validateRepeatedPassword(password: String, repeatedPassword: String): ValidationResult {
        if (password != repeatedPassword) {
            return ValidationResult(
                successful = false,
                errorMessage = context.getString(R.string.passwords_dont_match_error)
            )
        }
        return ValidationResult(
            successful = true
        )
    }

    fun validateOTP(otp: String): ValidationResult {
        if (otp.toIntOrNull()?.toString()?.length == OTP_LENGTH) {
            return ValidationResult(
                successful = true
            )
        }
        return ValidationResult(
            successful = false,
            errorMessage = context.getString(R.string.otp_must_contain_6_digits)
        )
    }

    fun validateTerms(terms: Boolean): ValidationResult {
        if (terms.not()) {
            return ValidationResult(
                successful = false,
                errorMessage = context.getString(R.string.terms_error)
            )
        }
        return ValidationResult(
            successful = true
        )
    }

    fun validateLogin(login: String): ValidationResult =
        when (checkLoginInputType(login)) {
            is LoginInputType.Email -> validateEmail(login)
            is LoginInputType.Username -> validateUsername(login)
        }


}