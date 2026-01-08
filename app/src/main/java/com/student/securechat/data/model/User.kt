package com.student.securechat.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class User(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val publicKey: String = "",
    val createdAt: @RawValue Any? = null,
    val lastSeen: @RawValue Any? = null,
    val isOnline: Boolean = false
) : Parcelable
