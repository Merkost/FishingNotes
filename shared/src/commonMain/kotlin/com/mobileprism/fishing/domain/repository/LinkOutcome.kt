package com.mobileprism.fishing.domain.repository

sealed interface LinkOutcome {
    data object Linked : LinkOutcome
    data class Merged(
        val catchesAdded: Int,
        val markersAdded: Int,
        val alreadyPresent: Int,
    ) : LinkOutcome
}
