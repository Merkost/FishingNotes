package com.mobileprism.fishing.model.datasource.firebase

import com.google.firebase.auth.FirebaseAuth
import com.mobileprism.fishing.domain.repository.AuthRepository

class FirebaseAuthRepository : AuthRepository {
    override fun getCurrentUserId(): String =
        FirebaseAuth.getInstance().currentUser?.uid ?: "Anonymous"
}
