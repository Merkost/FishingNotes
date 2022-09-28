package com.mobileprism.fishing.domain.use_cases.validation

import android.util.Patterns
import com.mobileprism.fishing.utils.*

class ValidationUseCase {

    companion object {
        private val loginPattern =
            "^(?=.{$LOGIN_MIN_LENGTH,$LOGIN_MAX_LENGTH}${'$'})(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$".toRegex()

        private val CharSequence?.isValidEmail: Boolean
            get() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

        private val CharSequence.containsLettersAndDigits: Boolean
            get() =  any { it.isDigit() } && any { it.isLetter() }

    }

    fun validateUsername(login: String): ValidationResult {
        if(login.length < LOGIN_MIN_LENGTH) {
            return ValidationResult(
                successful = false,
                errorMessage = "The login needs to consist of at least $LOGIN_MIN_LENGTH characters"
            )
        }
        if(login.length > LOGIN_MAX_LENGTH) {
            return ValidationResult(
                successful = false,
                errorMessage = "The login's max length is $LOGIN_MAX_LENGTH characters"
            )
        }
        if(loginPattern.matches(login).not()) {
            return ValidationResult(
                successful = false,
                errorMessage = "The login contains prohibit symbols"
            )
        }
        return ValidationResult(
            successful = true
        )
    }

    fun validateEmail(email: String): ValidationResult {
        if(email.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "The email can't be blank"
            )
        }
        if(email.isValidEmail.not()) {
            return ValidationResult(
                successful = false,
                errorMessage = "That's not a valid email"
            )
        }
        return ValidationResult(
            successful = true
        )
    }

    fun validatePassword(password: String): ValidationResult {
        if(password.length < PASSWORD_MIN_LENGTH) {
            return ValidationResult(
                successful = false,
                errorMessage = "Min password length is 8 characters"
            )
        }
        if(password.length > PASSWORD_MAX_LENGTH) {
            return ValidationResult(
                successful = false,
                errorMessage = "The password's max length is 20 characters"
            )
        }
        if(password.containsLettersAndDigits.not()) {
            return ValidationResult(
                successful = false,
                errorMessage = "The password needs to contain at least one letter and digit"
            )
        }
        return ValidationResult(
            successful = true
        )
    }

    fun validateRepeatedPassword(password: String, repeatedPassword: String): ValidationResult {
        if(password != repeatedPassword) {
            return ValidationResult(
                successful = false,
                errorMessage = "The passwords don't match"
            )
        }
        return ValidationResult(
            successful = true
        )
    }

    fun validateTerms(terms: Boolean): ValidationResult {
        if(terms.not()) {
            return ValidationResult(
                successful = false,
                errorMessage = "You need to check terms"
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