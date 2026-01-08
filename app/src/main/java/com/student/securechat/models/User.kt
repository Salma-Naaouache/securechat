package com.student.securechat.models

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val isOnline: Boolean = false
)