package com.student.securechat.data.model

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val publicKey: String = "",
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp val lastSeen: Date? = null,
    val isOnline: Boolean = false
) : Parcelable
