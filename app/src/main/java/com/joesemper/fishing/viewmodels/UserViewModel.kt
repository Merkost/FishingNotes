package com.joesemper.fishing.viewmodels

import androidx.lifecycle.ViewModel
import com.joesemper.fishing.data.repository.UserRepository

class UserViewModel(private val repository: UserRepository): ViewModel() {

    fun getCurrentUser() = repository.currentUser

    suspend fun logoutCurrentUser() = repository.logoutCurrentUser()

}