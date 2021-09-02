package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import com.joesemper.fishing.model.repository.UserRepository

class UserViewModel(private val repository: UserRepository): ViewModel() {

    fun getCurrentUser() = repository.currentUser

    suspend fun logoutCurrentUser() = repository.logoutCurrentUser()

}