package com.mobileprism.fishing.model.datasource.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import com.mobileprism.fishing.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class FirebaseAuthRepository : AuthRepository {
    override fun getCurrentUserId(): String =
        Firebase.auth.currentUser?.uid ?: "Anonymous"

    override fun getCurrentUserIdOrNull(): String? = Firebase.auth.currentUser?.uid

    override val currentUserIdFlow: Flow<String?>
        get() = merge(Firebase.auth.authStateChanged, Firebase.auth.idTokenChanged)
            .map { it?.uid }
            .distinctUntilChanged()
}
