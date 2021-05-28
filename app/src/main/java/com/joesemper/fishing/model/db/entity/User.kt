package com.joesemper.fishing.model.db.entity

data class User(
    val userId: String,
    val userName: String? = "Anonymous"
)