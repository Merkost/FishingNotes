package com.mobileprism.fishing.ui.utils.format

import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.error_auth_email_taken
import fishing.shared.generated.resources.error_auth_invalid
import fishing.shared.generated.resources.error_auth_user_not_found
import fishing.shared.generated.resources.error_generic
import fishing.shared.generated.resources.error_no_network
import fishing.shared.generated.resources.error_server
import fishing.shared.generated.resources.error_timeout
import org.jetbrains.compose.resources.StringResource

fun errorToMessage(throwable: Throwable?): StringResource {
    val text = (throwable?.message ?: "").lowercase()
    return when {
        text.contains("timeout") || text.contains("timed out") -> Res.string.error_timeout
        text.contains("unknownhost") || text.contains("no internet") ||
            text.contains("network") || text.contains("connection") -> Res.string.error_no_network
        text.contains("password") || text.contains("credential") ||
            text.contains("invalid-login") -> Res.string.error_auth_invalid
        text.contains("user-not-found") || text.contains("no account") -> Res.string.error_auth_user_not_found
        text.contains("email-already") || text.contains("already in use") ||
            text.contains("already registered") -> Res.string.error_auth_email_taken
        text.contains("500") || text.contains("502") || text.contains("503") ||
            text.contains("server") -> Res.string.error_server
        else -> Res.string.error_generic
    }
}
