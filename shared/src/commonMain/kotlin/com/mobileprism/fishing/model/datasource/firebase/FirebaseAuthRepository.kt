package com.mobileprism.fishing.model.datasource.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import com.mobileprism.fishing.domain.repository.AuthRepository

class FirebaseAuthRepository : AuthRepository {
    override fun getCurrentUserId(): String =
        Firebase.auth.currentUser?.uid ?: "Anonymous"
}
